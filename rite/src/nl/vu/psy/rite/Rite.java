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

package nl.vu.psy.rite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.UUID;

import nl.vu.psy.rite.computation.RecipeCooker;
import nl.vu.psy.rite.exceptions.RiteException;
import nl.vu.psy.rite.operations.Recipe;
import nl.vu.psy.rite.persistence.ClientCommand;
import nl.vu.psy.rite.persistence.ClientInfo;
import nl.vu.psy.rite.persistence.TimeStamp;

import org.apache.commons.io.output.TeeOutputStream;

/**
 * Rite
 *
 * @author vm.kattenberg
 */
public class Rite {
    // TODO check sanity of default settings for properties
    // TODO add client programs for creating and uploading recipes, basic monitoring tool
    private static String PROPERTIES = "rite.properties";
    private static Rite instance;
    private static String version;

    public enum PropertyKeys {
        ID("identifier", null), TICK("tickrate", "30000"), HOST("hostfile", "host.properties"), LIFETIME("lifetime", "86400000"), INTERVAL("interval", "60000"), RELIC("relicfile", "relic.properties"), LISTFILES(
                "listfiles", "true"), SCRUBDELAY("scrubdelay", "120000"), IDLEDELAY("idledelay", "120000"), MAXFAILURES("maxfailures", "3"), MAXSCRUBS("maxscrubs", "20"), MAXRECIPES("maxrecipes", "-1");

        private final String key;
        private final String defaultValue;

        private PropertyKeys(String key, String defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public String getDefaultValue() {
            if (this == ID) {
                // The default specified at the level of the enumeration will be static (constant) for all instances.
                // If defaults should vary according to a pattern or function (as in the case for ID) it can be specified like this.
                return UUID.randomUUID().toString();
            }
            return defaultValue;
        }

        public String getKey() {
            return key;
        }

        public String getProperty(Properties properties) {
            return properties.getProperty(this.getKey(), this.getDefaultValue());
        }

    }

    private String identifier;
    private Timer recipeTimer;
    private RecipeCooker recipeCooker;
    private Properties properties;
    private Date startDate;
    private Date endDate;
    private FileCache fileCache;
    private Host rh;
    private boolean run = true;
    private boolean idle = false;
    private boolean halt = false;
    private int failures = 0;
    private int scrubs = 0;
    private int recipes = 0;

    public static Rite getInstance() {
        if (instance == null) {
            instance = new Rite();
        }
        return instance;
    }

    private Rite() {

        version = Rite.class.getPackage().getImplementationVersion();
        StringTokenizer st = null;
        if (version != null) {
            st = new StringTokenizer(version, "_");
        } else {
            st = new StringTokenizer("");
            version = "undetermined";
        }
        System.out.println();
        System.out.println("+-++-++-++-+");
        System.out.print("|R||i||t||e|");
        if (version != null && st.countTokens() >= 2) {
            System.out.print(" version: " + st.nextToken() + " build " + st.nextToken() + "\n");
        } else {
            System.out.print(" version: " + version + "\n");
        }
        System.out.println("+-++-++-++-+");
        System.out.println();
        properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(PROPERTIES)));
        } catch (Exception e1) {
            // Absorb
            System.out.println("Could not read properties files. Assuming programmed default values for client settings.");

        }

        identifier = getProperty(PropertyKeys.ID);
        System.out.println("Rite identifier: " + identifier);

        startDate = new Date();
        long appLife = Long.parseLong(getProperty(PropertyKeys.LIFETIME));
        endDate = new Date(startDate.getTime() + appLife);

        System.out.println("Application start time: " + TimeStamp.dateToString(startDate));
        System.out.println("Configured application end time: " + TimeStamp.dateToString(endDate));

        System.out.println("Setting up file cache...");
        // TODO make file cache optional
        // Set up file cache
        try {
            fileCache = new FileCache(getProperty(PropertyKeys.RELIC));
        } catch (RiteException e) {
            System.out.println("Could not set up file cache: " + e.getMessage());
            run = false;
        }

        // Initialize info struct
        System.out.println("Initializing client info...");
        ClientInfo info = new ClientInfo();
        info.setClientId(identifier);
        try {
            InetAddress addr = InetAddress.getLocalHost();
            String host = addr.getHostName();
            info.setClientHost(host);
        } catch (UnknownHostException e) {
            info.setClientHost("Unknown: " + e.getMessage());
        }
        info.setClientStart(startDate);
        info.setWorkingDirectory(new File(System.getProperty("user.dir")));

        System.out.println("Configuring host connection...");
        rh = null;
        try {
            boolean listfiles = Boolean.parseBoolean(getProperty(PropertyKeys.LISTFILES));
            rh = new Host(getProperty(PropertyKeys.HOST), info, listfiles);
        } catch (RiteException e) {
            System.out.println("Could not connect to the rite host: " + e.getMessage());
            run = false;
        }
        // Set up recipecooker
        recipeTimer = new Timer("recipeTimer", false);
        recipeCooker = new RecipeCooker();
        long tickRate = Long.parseLong(getProperty(PropertyKeys.TICK));
        recipeTimer.scheduleAtFixedRate(recipeCooker, 1000, tickRate);
    }

    public void run() {
        if (run) {
            Recipe r = null;
            try {
                System.out.println("Starting work cycle..");
                long sleepTime = Long.parseLong(getProperty(PropertyKeys.INTERVAL));
                long scrubDelay = Long.parseLong(getProperty(PropertyKeys.SCRUBDELAY));
                long idleDelay = Long.parseLong(getProperty(PropertyKeys.IDLEDELAY));
                int maxFailures = Integer.parseInt(getProperty(PropertyKeys.MAXFAILURES));
                int maxScrubs = Integer.parseInt(getProperty(PropertyKeys.MAXSCRUBS));
                int maxRecipes = Integer.parseInt(getProperty(PropertyKeys.MAXRECIPES));
                while (!lifeTimeExceeded() && !halt) {
                    // Check commands
                    ClientCommand co = rh.getClientCommand(identifier);
                    if (co == null) {
                        if (idle) {
                            System.out.println("-------------------------------------------------------------------------------");
                            System.out.println("Idle.");
                            System.out.println("Time: " + TimeStamp.dateToString(new Date()));
                            System.out.println("-------------------------------------------------------------------------------");
                            try {
                                Thread.sleep(idleDelay);
                            } catch (InterruptedException e) {
                                System.out.println("The work cycle was interrupted: " + e.getMessage());
                                System.out.println("=== MARK: " + TimeStamp.dateToString(new Date()) + " ===");
                                return;
                            }
                            System.out.println("=== MARK: " + TimeStamp.dateToString(new Date()) + " ===");
                        } else {
                            r = rh.lockRecipe(); // Lock and retrieve recipe
                            if (r == null) {
                                System.out.println("-------------------------------------------------------------------------------");
                                System.out.println("No recipe. Scrubbing host.");
                                System.out.println("Time: " + TimeStamp.dateToString(new Date()));
                                System.out.println("-------------------------------------------------------------------------------");
                                rh.scrubHost();
                                scrubs++;
                                if(scrubs > maxScrubs) {
                                    System.out.println("The maximum number of scrubs has been reached. This client will shutdown...");
                                    halt = true;
                                }
                                System.out.println("=== MARK: " + TimeStamp.dateToString(new Date()) + " ===");
                                try {
                                    Thread.sleep(scrubDelay);
                                } catch (InterruptedException e) {
                                    System.out.println("The work cycle was interrupted: " + e.getMessage());
                                    System.out.println("=== MARK: " + TimeStamp.dateToString(new Date()) + " ===");
                                    return;
                                }
                            } else {
                                // Reset scrub counter
                                scrubs = 0;
                                // Set up streams for recipe output
                                PrintStream out = null;
                                PrintStream err = null;

                                try {
                                    out = new PrintStream(new FileOutputStream("recipe." + r.getIdentifier() + ".stdout"));
                                    PrintStream teeOut = new PrintStream(new TeeOutputStream(System.out, out));
                                    System.setOut(teeOut);
                                    err = new PrintStream(new FileOutputStream("recipe." + r.getIdentifier() + ".stderr"));
                                    PrintStream teeErr = new PrintStream(new TeeOutputStream(System.err, err));
                                    System.setErr(teeErr);
                                } catch (FileNotFoundException e) {
                                    // Absorb
                                    System.out.println("Could not tee output streams to file. Outputting to main application streams only.");
                                }

                                System.out.println("-------------------------------------------------------------------------------");
                                System.out.println("Starting recipe: " + r.getIdentifier() + ".");
                                System.out.println("Time: " + TimeStamp.dateToString(new Date()));
                                System.out.println("-------------------------------------------------------------------------------");

                                recipeCooker.setRecipe(r); // Run recipe
                                // Wait for completion
                                while (!r.hasCompleted()) {
                                    try {
                                        Thread.sleep(sleepTime);
                                    } catch (InterruptedException e) {
                                        System.out.println("The work cycle was interrupted: " + e.getMessage());
                                        System.out.println("Attempting release of: " + r.getIdentifier());
                                        r = recipeCooker.getRecipe();
                                        recipeCooker.removeRecipe();
                                        rh.releaseRecipe(r);
                                        System.out.println("=== MARK: " + TimeStamp.dateToString(new Date()) + " ===");
                                        return;
                                    }
                                }
                                r = recipeCooker.getRecipe();
                                recipeCooker.removeRecipe();
                                rh.releaseRecipe(r);
                                recipes++;
                                if(r.hasFailed()) {
                                    failures ++;
                                }
                                if(failures >= maxFailures) {
                                    System.out.println("The maximum number of recipe failures has been reached. This client will shutdown...");
                                    halt = true;
                                }
                                if(maxRecipes > -1 && recipes >= maxRecipes) {
                                	System.out.println("The maximum number of completed recipes has been reached. This client will shutdown...");
                                    halt = true;
                                }

                                System.out.println("=== MARK: " + TimeStamp.dateToString(new Date()) + " ===");
                                System.setOut(System.out);
                                System.setErr(System.err);
                                if (out != null) {
                                    out.close();
                                    out = null;
                                }
                                if (err != null) {
                                    err.close();
                                    err = null;
                                }
                            }
                        }
                    } else {
                        // Handle command
                        // FIXME not too fond of all these flags
                        System.out.println("-------------------------------------------------------------------------------");
                        System.out.println("Got command: " + co.getCommand());
                        System.out.println("Time: " + TimeStamp.dateToString(new Date()));
                        System.out.println("-------------------------------------------------------------------------------");
                        switch (co.getCommand()) {
                        case HALT:
                            System.out.println("Halting client...");
                            halt = true;
                            break;
                        case IDLE:
                            System.out.println("Setting client to idle...");
                            idle = true;
                            break;
                        case RUN:
                            System.out.println("Setting client to run...");
                            idle = false;
                            break;
                        default:
                            break;
                        }
                        System.out.println("=== MARK: " + TimeStamp.dateToString(new Date()) + " ===");
                    }
                }
                //System.out.println("Shutting down...");
                if (rh.hasLock()) {
                    System.out.println("Attempting release of: " + r.getIdentifier());
                    r = recipeCooker.getRecipe();
                    recipeCooker.removeRecipe();
                    rh.releaseRecipe(r);
                    System.out.println("=== MARK: " + TimeStamp.dateToString(new Date()) + " ===");
                    return;
                }
            } catch (Exception e) {
                System.out.println("An exception was encountered while running: " + e.getMessage());
                System.out.println("Exiting!");
                return;
            }
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public void exit() {
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("Ending application.");
        System.out.println("Time: " + TimeStamp.dateToString(new Date()));
        System.out.println("-------------------------------------------------------------------------------");
        recipeTimer.cancel();
        recipeCooker.destroy();
        System.out.println("=== FINAL MARK: " + TimeStamp.dateToString(new Date()) + " ===");
        System.out.println("-------------------------------------------------------------------------------");

    }

    public String getProperty(PropertyKeys prop) {
        return prop.getProperty(properties);
    }

    public FileCache getFileCache() {
        return fileCache;
    }

    private boolean lifeTimeExceeded() {
        return (endDate.compareTo(startDate) > 0) ? false : true;
    }

    public static void main(String[] args) {
        Rite r = Rite.getInstance();
        r.run();
        r.exit();
    }
}
