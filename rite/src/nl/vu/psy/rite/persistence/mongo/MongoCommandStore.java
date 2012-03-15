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

import nl.vu.psy.rite.exceptions.RiteException;
import nl.vu.psy.rite.exceptions.RiteRuntimeException;
import nl.vu.psy.rite.persistence.ClientCommand;
import nl.vu.psy.rite.persistence.ClientCommandStore;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

/**
 * MongoCommandStore
 * 
 * @author vm.kattenberg
 */
public class MongoCommandStore implements ClientCommandStore {
    private Mongo mongo;
    private DB db;
    private DBCollection commandCollection;

    public MongoCommandStore(String host, int port, String dbname, String collection) throws RiteException {
        try {
            mongo = new Mongo(host, port);
            db = mongo.getDB(dbname);
            if (db.collectionExists(collection)) {
                commandCollection = db.getCollection(collection);
            } else {
                DBObject dbo = new BasicDBObject();
                dbo.put("capped", false);
                commandCollection = db.createCollection(collection, dbo);
            }
            commandCollection.ensureIndex(new BasicDBObject("clientcommand.clientid", 1).append("unique", true));
        } catch (UnknownHostException e) {
            throw new RiteException("Unknown host for mongo connection.", e);
        } catch (MongoException e) {
            throw new RiteException("An exception from MongoDB was encountered.", e);
        }
    }

    public MongoCommandStore(String host, int port, String dbname, String collection, String user, String pass) throws RiteException {
        try {
            mongo = new Mongo(host, port);
            db = mongo.getDB(dbname);
            if (db.authenticate(user, pass.toCharArray())) {
                if (db.collectionExists(collection)) {
                    commandCollection = db.getCollection(collection);
                } else {
                    DBObject dbo = new BasicDBObject();
                    dbo.put("capped", false);
                    commandCollection = db.createCollection(collection, dbo);
                }
                commandCollection.ensureIndex(new BasicDBObject("clientcommand.clientid", 1).append("unique", true));
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
    public boolean putClientCommand(ClientCommand command) {
        BasicDBObject query = new BasicDBObject();
        query.put("clientcommand.clientid", command.getClientId());
        DBObject object = MongoCommandMapper.clientCommandToDBObject(command);
        WriteResult update = commandCollection.update(query, object, true, false);
        return (update.getLastError().ok() && update.getN() == 1);
    }

    @Override
    public ClientCommand consumeClientCommand(String clientId) {
        BasicDBObject query = new BasicDBObject();
        query.put("clientcommand.clientid", clientId);
        DBCursor find = commandCollection.find(query);
        if (find.size() == 1) {
            DBObject dbo = find.next();
            ClientCommand co = MongoCommandMapper.DBObjectToClientCommand(dbo);
            commandCollection.remove(query);
            return co;
        } else if (find.size() > 1) {
            throw new RiteRuntimeException("The rite command collection is possibly corrupt, more than one entry was found for the identifier [" + clientId + "].");
        } else {
            return null;
        }
    }

}
