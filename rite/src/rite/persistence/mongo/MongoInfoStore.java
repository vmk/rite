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

package rite.persistence.mongo;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

import rite.exceptions.RiteException;
import rite.persistence.ClientInfo;
import rite.persistence.ClientInfoStore;

/**
 * MongoInfoStore
 * 
 * @author vm.kattenberg
 */
public class MongoInfoStore implements ClientInfoStore {
    private Mongo mongo;
    private DB db;
    private DBCollection infoCollection;

    public MongoInfoStore(String host, int port, String dbname, String collection) throws RiteException {
        try {
            mongo = new Mongo(host, port);
            db = mongo.getDB(dbname);
            if (db.collectionExists(collection)) {
                infoCollection = db.getCollection(collection);
            } else {
                DBObject dbo = new BasicDBObject();
                dbo.put("capped", false);
                infoCollection = db.createCollection(collection, dbo);
            }
            infoCollection.ensureIndex(new BasicDBObject("clientinfo.clientid", 1).append("unique", true));
        } catch (UnknownHostException e) {
            throw new RiteException("Unknown host for mongo connection.", e);
        } catch (MongoException e) {
            throw new RiteException("An exception from MongoDB was encountered.", e);
        }
    }

    public MongoInfoStore(String host, int port, String dbname, String collection, String user, String pass) throws RiteException {
        try {
            mongo = new Mongo(host, port);
            db = mongo.getDB(dbname);
            if (db.authenticate(user, pass.toCharArray())) {
                if (db.collectionExists(collection)) {
                    infoCollection = db.getCollection(collection);
                } else {
                    DBObject dbo = new BasicDBObject();
                    dbo.put("capped", false);
                    infoCollection = db.createCollection(collection, dbo);
                }
                infoCollection.ensureIndex(new BasicDBObject("clientinfo.clientid", 1).append("unique", true));
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
    public boolean insertClientInfo(ClientInfo info, boolean listWorkingDir, boolean outputFiles) {
        // Changed to upsert
        BasicDBObject query = new BasicDBObject();
        query.put("clientinfo.clientid", info.getClientId());
        query.put("clientinfo.recipeid", info.getRecipeId());
        DBObject object = MongoInfoMapper.clientInfoToDBObject(info, listWorkingDir, outputFiles);
        WriteResult update = infoCollection.update(query, object, true, false);
        return (update.getLastError().ok() && update.getN() == 1);
        
        //DBObject bdo = MongoInfoMapper.clientInfoToDBObject(info, listWorkingDir, outputFiles);
        //WriteResult insert = infoCollection.insert(bdo);
        //return (insert.getLastError().ok() && insert.getN() == 1);
    }

    @Override
    public void deleteClientInfo(String clientId, String recipeId) {
        BasicDBObject bdo = new BasicDBObject();
        bdo.put("clientinfo.clientid", clientId);
        bdo.put("clientinfo.recipeid", recipeId);
        infoCollection.remove(bdo);
    }

    @Override
    public void deleteAllClientInfo(String clientId) {
        BasicDBObject bdo = new BasicDBObject();
        bdo.put("clientinfo.clientid", clientId);
        infoCollection.remove(bdo);
    }

    @Override
    public String[] getClientInfo(String clientId) {
        BasicDBObject query = new BasicDBObject();
        query.put("clientinfo.clientid", clientId);
        DBCursor find = infoCollection.find(query);
        String[] result = new String[find.size()];
        int i = 0;
        while (find.hasNext()) {
            result[i] = MongoInfoMapper.DBObjectToString(find.next());
            i++;
        }
        return result;
    }

    @Override
    public String[] getClientInfoForRecipe(String recipeId) {
        BasicDBObject query = new BasicDBObject();
        query.put("clientinfo.recipeid", recipeId);
        DBCursor find = infoCollection.find(query);
        String[] result = new String[find.size()];
        int i = 0;
        while (find.hasNext()) {
            result[i] = MongoInfoMapper.DBObjectToString(find.next());
            i++;
        }
        return result;
    }

}
