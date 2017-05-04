// Copyright 2012 - V.M. Kattenberg - vm.kattenberg@gmail.com
//
// This file is part of rite
//
// rite is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// rite is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with rite.  If not, see <http://www.gnu.org/licenses/>.

package io.github.vmk.rite;

import io.github.vmk.rite.exceptions.RiteException;
import io.github.vmk.rite.operations.Recipe;
import io.github.vmk.rite.persistence.*;
import io.github.vmk.rite.persistence.mongo.MongoCommandStore;
import io.github.vmk.rite.persistence.mongo.MongoInfoStore;
import io.github.vmk.rite.persistence.mongo.MongoRecipeStore;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

/**
 * Host
 * 
 * @author vm.kattenberg
 */
public class Host {
    // TODO this class is becoming a swiss army knife...
    @SuppressWarnings("unused")
    private String id;
    private String type;
    private String hostname;
    private int port;
    private boolean auth;
    private String user;
    private String pass;
    private String dbname;
    private String collection;
    private RecipeStore recipeStore;
    private ClientInfoStore infoStore;
    private ClientCommandStore commandStore;
    private ClientInfo info;
    private Recipe compareRecipe;
    private boolean reportWorkingDirectory;

    public Host(String propertiesFilename, ClientInfo initialInfo, boolean reportWorkingDirectory) throws RiteException {
        // TODO add intermediate updating of host (e.g. for progress reporting)
        this.info = initialInfo;
        this.reportWorkingDirectory = reportWorkingDirectory;
        // TODO add option to read host from jar
        try {
            Properties hostProps = new Properties();
            hostProps.load(new FileInputStream(propertiesFilename));
            id = hostProps.getProperty("id");
            type = hostProps.getProperty("type");
            hostname = hostProps.getProperty("hostname");
            port = Integer.parseInt(hostProps.getProperty("port"));
            dbname = hostProps.getProperty("dbname");
            auth = Boolean.parseBoolean(hostProps.getProperty("auth"));
            collection = hostProps.getProperty("collection");
            if (auth) {
                user = hostProps.getProperty("user");
                // TODO password should be handled in a different way. At the very least some encryption should be added.
                pass = hostProps.getProperty("pass");
            }
        } catch (Exception e) {
            System.out.println("Could not read host file: " + propertiesFilename + ". Setting host to MongoDB on localhost port 27017 using database rite and no authentication.");
            id = "localhost";
            type = "MongoDB";
            hostname = "localhost";
            dbname = "rite";
            port = Integer.parseInt("27017");
            auth = false;
        }
        if ("MongoDB".equals(type)) {
            if (auth) {
                recipeStore = new MongoRecipeStore(hostname, port, dbname, collection, user, pass);
                infoStore = new MongoInfoStore(hostname, port, dbname, "clientinfo", user, pass);
                commandStore = new MongoCommandStore(hostname, port, dbname, "clientcommands", user, pass);
            } else {
                recipeStore = new MongoRecipeStore(hostname, port, dbname, collection);
                infoStore = new MongoInfoStore(hostname, port, dbname, "clientinfo");
                commandStore = new MongoCommandStore(hostname, port, dbname, "clientcommands");
            }
        }
    }

    public Recipe lockRecipe() {
        // Find unlocked recipes
        // TODO make limit an option.
        Recipe[] recipesWithoutClients = recipeStore.getRecipesWithoutClients(10);
        // TODO array is shuffled to reduce lock contention. Check if this works and is necessary.
        Collections.shuffle(Arrays.asList(recipesWithoutClients));
        Recipe result = null;
        for (int i = 0; i < recipesWithoutClients.length; i++) {
            result = lock(recipesWithoutClients[i]);
            if (result != null) {
                compareRecipe = new Recipe(result);
                break;
            }
        }
        return result;
    }

    private Recipe lock(Recipe r) {
        Date lockDate = new Date();
        Recipe rc = new Recipe(r);
        r.setClientId(info.getClientId());
        r.setTimeStamp(lockDate);
        if (recipeStore.updateRecipe(rc, r)) {
            info.setRecipeId(r.getIdentifier());
            info.setRecipeStart(lockDate);
            info.setRecipeEnd(null);
            info.setRecipeFailed(false);
            infoStore.insertClientInfo(info, reportWorkingDirectory, false);
            return r;
        } else {
            return null;
        }
    }

    public void releaseRecipe(Recipe r) {
        if (compareRecipe == null) {
            // TODO perhaps set client info?
            return;
        } else {
            if (r.getIdentifier().equals(compareRecipe.getIdentifier())) {
                // Attempt unlock
                Recipe rs = unlock(compareRecipe, r);
                if (rs == null) {
                    System.out.println("Could not unlock: " + r.getIdentifier() + " for client: " + compareRecipe.getClientId());
                }
                compareRecipe = null;
            } else {
                // In trouble!
                System.err.println("Recipe identifier mismatch between lock and unlock!");
                System.err.println("Attempting to unlock old recipe...");
                Recipe rs = unlock(compareRecipe, compareRecipe);
                if (rs == null) {
                    System.err.println("Could not unlock the old recipe.");
                }
                compareRecipe = null;
            }
        }
    }

    private Recipe unlock(Recipe rc, Recipe r) {
        Date unlockDate = new Date();
        r.setClientId(info.getClientId());
        r.setTimeStamp(unlockDate);
        r.hasCompleted();
        if (recipeStore.updateRecipe(rc, r)) {
            info.setRecipeId(r.getIdentifier());
            info.setRecipeEnd(unlockDate);
            info.setRecipeFailed(r.hasFailed());
            // FIXME setting of output files should not be done like this!
            info.setStandardOut(new File("recipe." + r.getIdentifier() + ".stdout"));
            info.setStandardError(new File("recipe." + r.getIdentifier() + ".stderr"));
            // FIXME outputsetting should be part of application settings.
            infoStore.insertClientInfo(info, reportWorkingDirectory, true);
            return r;
        } else {
            return null;
        }
    }

    public void scrubHost() {
        // Check recipes for timeout -> if timed out; unlock and return
        Recipe[] recipes = recipeStore.getUncompleteRecipesWithClients();
        for (Recipe r : recipes) {
            if (r.getTimeStamp() != null && r.getTimeout() != -1 && r.resetOnTimeout()) {
                Date now = new Date();
                Date expiry = new Date(r.getTimeStamp().getTime() + r.getTimeout());
                if (now.compareTo(expiry) > 0) {
                    Recipe cs = new Recipe(r);
                    System.out.println("Resetting recipe: " + r.getIdentifier() + " with client id: " + r.getClientId() + " due to timeout of recipe...");
                    r.reset();
                    if (recipeStore.updateRecipe(cs, r)) {
                        System.out.println("Reset succesful.");
                    } else {
                        System.out.println("Reset failed.");
                    }
                }
            }
        }

        // Check failed recipes, if they can be reset
        Recipe[] moreRecipes = recipeStore.getFailedRecipes();
        for (Recipe r : moreRecipes) {
            if (r.resetOnFailure()) {
                Recipe cs = new Recipe(r);
                System.out.println("Resetting recipe: " + r.getIdentifier() + " with client id: " + r.getClientId() + " due to failure of recipe...");
                r.reset();
                if (recipeStore.updateRecipe(cs, r)) {
                    System.out.println("Reset succesful.");
                } else {
                    System.out.println("Reset failed.");
                }
            }
        }
    }

    public boolean hasLock() {
        return (compareRecipe != null);
    }

    public ClientCommand getClientCommand(String clientId) {
        ClientCommand co = commandStore.consumeClientCommand(clientId);
        if(co == null) {
            return null;
        } else {
            Date commandDate = new Date();
            info.setRecipeId(co.getCommand().toString() + "-" + TimeStamp.dateToString(commandDate));
            info.setRecipeStart(commandDate);
            info.setRecipeEnd(commandDate);
            info.setStandardOut(null);
            info.setStandardError(null);
            infoStore.insertClientInfo(info, reportWorkingDirectory, false);
            return co;
        }
    }

}
