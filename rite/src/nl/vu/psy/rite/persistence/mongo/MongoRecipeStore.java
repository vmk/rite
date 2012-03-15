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

package nl.vu.psy.rite.persistence.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;

import nl.vu.psy.rite.exceptions.RiteException;
import nl.vu.psy.rite.exceptions.RiteRuntimeException;
import nl.vu.psy.rite.operations.Recipe;
import nl.vu.psy.rite.persistence.RecipeStore;
import nl.vu.psy.rite.persistence.TimeStamp;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

/**
 * MongoRecipeStore
 * 
 * @author vm.kattenberg
 */
public class MongoRecipeStore implements RecipeStore {
    private Mongo mongo;
    private DB db;
    private DBCollection recipeCollection;

    public MongoRecipeStore(String host, int port, String dbname, String collection) throws RiteException {
        try {
            mongo = new Mongo(host, port);
            db = mongo.getDB(dbname);
            if (db.collectionExists(collection)) {
                recipeCollection = db.getCollection(collection);
            } else {
                DBObject dbo = new BasicDBObject();
                dbo.put("capped", false);
                recipeCollection = db.createCollection(collection, dbo);
            }
            recipeCollection.ensureIndex(new BasicDBObject("recipe", 1).append("unique", true));
        } catch (UnknownHostException e) {
            throw new RiteException("Unknown host for mongo connection.", e);
        } catch (MongoException e) {
            throw new RiteException("An exception from MongoDB was encountered.", e);
        }
    }

    public MongoRecipeStore(String host, int port, String dbname, String collection, String user, String pass) throws RiteException {
        try {
            mongo = new Mongo(host, port);
            db = mongo.getDB(dbname);
            if (db.authenticate(user, pass.toCharArray())) {
                if (db.collectionExists(collection)) {
                    recipeCollection = db.getCollection(collection);
                } else {
                    DBObject dbo = new BasicDBObject();
                    dbo.put("capped", false);
                    recipeCollection = db.createCollection(collection, dbo);
                }
                recipeCollection.ensureIndex(new BasicDBObject("recipe", 1).append("unique", true));
            } else {
                throw new RiteException("Unable to authenticate.");
            }
        } catch (UnknownHostException e) {
            throw new RiteException("Unknown host for mongo connection.", e);
        } catch (MongoException e) {
            throw new RiteException("An exception from MongoDB was encountered,", e);
        }
    }

    @Override
    public boolean putRecipe(Recipe r) {
        BasicDBObject bdo = new BasicDBObject();
        bdo.put("recipe", r.getIdentifier());
        WriteResult update = recipeCollection.update(bdo, MongoRecipeMapper.recipeToDBObject(r), true, false);
        return (update.getLastError().ok() && update.getN() == 1);
    }

    @Override
    public void removeRecipe(String recipeId) {
        BasicDBObject bdo = new BasicDBObject();
        bdo.put("recipe", recipeId);
        recipeCollection.remove(bdo);
    }

    @Override
    public Recipe getRecipe(String recipeId) {
        BasicDBObject bdo = new BasicDBObject();
        bdo.put("recipe", recipeId);
        DBCursor find = recipeCollection.find(bdo);
        if (find.size() == 1) {
            DBObject dbo = find.next();
            return MongoRecipeMapper.DBObjectToRecipe(dbo);
        } else if (find.size() > 1) {
            throw new RiteRuntimeException("The rite recipe collection is possibly corrupt, more than one entry was found for the identifier [" + recipeId + "].");
        } else {
            return null;
        }
    }

    @Override
    public Recipe[] getAllRecipes() {
        Recipe[] result = null;
        DBCursor find = recipeCollection.find();
        result = new Recipe[find.size()];
        int i = 0;
        while (find.hasNext()) {
            DBObject dbo = find.next();
            result[i] = MongoRecipeMapper.DBObjectToRecipe(dbo);
            i++;
        }
        return result;
    }

    @Override
    public boolean updateRecipe(Recipe oldRecipe, Recipe newRecipe) {
        // Compare and swap
        BasicDBObject bdo = new BasicDBObject();
        bdo.put("recipe", oldRecipe.getIdentifier());
        bdo.put("clientid", oldRecipe.getClientId());
        bdo.put("timestamp", TimeStamp.dateToString(oldRecipe.getTimeStamp()));
        WriteResult update = recipeCollection.update(bdo, MongoRecipeMapper.recipeToDBObject(newRecipe));
        return (update.getLastError().ok() && update.getN() == 1);
    }

    @Override
    public Recipe[] getRecipesWithoutClients(int limit) {
        Recipe[] result = null;
        DBObject query = new BasicDBObject();
        query.put("clientid", new BasicDBObject("$type", 10));
        DBCursor find = recipeCollection.find(query).limit(limit);
        ArrayList<Recipe> recipes = new ArrayList<Recipe>();
        result = new Recipe[find.size()];
        // int i = 0;
        while (find.hasNext()) {
            DBObject dbo = find.next();
            // result[i] = MongoRecipeMapper.DBObjectToRecipe(dbo);
            recipes.add(MongoRecipeMapper.DBObjectToRecipe(dbo));
            // i++;
        }
        return recipes.toArray(result);
    }

    @Override
    public Recipe[] getUncompleteRecipesWithClients() {
        Recipe[] result = null;
        DBObject query = new BasicDBObject();
        query.put("clientid", new BasicDBObject("$type", 2));
        query.put("completed", false);
        DBCursor find = recipeCollection.find(query);
        result = new Recipe[find.size()];
        int i = 0;
        while (find.hasNext()) {
            DBObject dbo = find.next();
            result[i] = MongoRecipeMapper.DBObjectToRecipe(dbo);
            i++;
        }
        return result;
    }

    @Override
    public Recipe[] getFailedRecipes() {
        Recipe[] result = null;
        DBObject query = new BasicDBObject();
        query.put("clientid", new BasicDBObject("$type", 2));
        query.put("completed", true);
        query.put("failed", true);
        DBCursor find = recipeCollection.find(query);
        result = new Recipe[find.size()];
        int i = 0;
        while (find.hasNext()) {
            DBObject dbo = find.next();
            result[i] = MongoRecipeMapper.DBObjectToRecipe(dbo);
            i++;
        }
        return result;
    }

}
