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

import java.io.FileInputStream;
import java.util.Properties;

import nl.vu.psy.relic.RelicFileCache;
import nl.vu.psy.relic.exceptions.RelicException;
import nl.vu.psy.relic.persistence.RelicStore;
import nl.vu.psy.relic.persistence.mongo.MongoStore;
import nl.vu.psy.relic.resolvers.implementations.LocalFileSystemResolver;
import nl.vu.psy.rite.exceptions.RiteException;

/**
 * FileCache
 * 
 * @author vm.kattenberg
 */
public class FileCache {
    private String type;
    private String hostname;
    private int port;
    private boolean auth;
    private String user;
    private String pass;
    private String dbname;
    private String environment;
    private RelicFileCache fileCache;

    public FileCache(String propertiesFilename) throws RiteException {
        // TODO add option to read filecache properties from jar
        // TODO hardcoded for relic and mongo store
        try {
            Properties cacheProps = new Properties();
            cacheProps.load(new FileInputStream(propertiesFilename));
            environment = cacheProps.getProperty("environment");
            type = cacheProps.getProperty("type");
            hostname = cacheProps.getProperty("hostname");
            port = Integer.parseInt(cacheProps.getProperty("port"));
            dbname = cacheProps.getProperty("dbname");
            auth = Boolean.parseBoolean(cacheProps.getProperty("auth"));
            if (auth) {
                user = cacheProps.getProperty("user");
                // TODO password should be handled in a different way. At the very least some encryption could be added.
                pass = cacheProps.getProperty("pass");
            }
        } catch (Exception e) {
            System.out.println("Could not read file cache properties: " + propertiesFilename + ". Setting host to MongoDB on localhost port 27017 using database relic, no authentication and "
                    + LocalFileSystemResolver.ENVIRONMENT + " as environment.");
            type = "MongoDB";
            hostname = "localhost";
            dbname = "relic";
            port = Integer.parseInt("27017");
            environment = LocalFileSystemResolver.ENVIRONMENT;
            auth = false;
        }
        RelicStore store = null;
        if ("MongoDB".equals(type)) {
            if (auth) {
                try {
                    store = new MongoStore(hostname, port, dbname, "relics", user, pass);
                } catch (RelicException e) {
                    throw new RiteException("Could not connect to relic store.", e);
                }
            } else {
                try {
                    store = new MongoStore(hostname, port, dbname, "relics");
                } catch (RelicException e) {
                    throw new RiteException("Could not connect to relic store.", e);
                }
            }
        }
        if (store == null) {
            throw new RiteException("Could not set up relic file cache.");
        } else {
            try {
                fileCache = new RelicFileCache(System.getProperty("user.dir"), store, environment);
            } catch (RelicException e) {
                System.out.println(System.getProperty("user.dir"));
                e.printStackTrace();
                throw new RiteException("Could not configure relic file cache.", e);
            }
        }
    }

    public RelicFileCache getFileCache() {
        return fileCache;
    }

}
