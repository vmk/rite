// Copyright 2012 - V.M. Kattenberg - vm.kattenberg@gmail.com
//
// This file is part of relic
//
// relic is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// relic is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with relic.  If not, see <http://www.gnu.org/licenses/>.

package nl.vu.psy.relic.persistence.mongo;

import java.net.UnknownHostException;

import nl.vu.psy.relic.Relic;
import nl.vu.psy.relic.exceptions.RelicException;
import nl.vu.psy.relic.exceptions.RelicRuntimeException;
import nl.vu.psy.relic.persistence.RelicStore;
import nl.vu.psy.relic.resolvers.ResolverDescriptor;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

/**
 * MongoStore
 * 
 * @author vm.kattenberg
 */
public class MongoStore implements RelicStore {
	private Mongo mongo;
	private DB db;
	private DBCollection relicCollection;

	public MongoStore(String host, int port, String dbname, String collection) throws RelicException {
		try {
			mongo = new Mongo(host, port);
			db = mongo.getDB(dbname);
			if (db.collectionExists(collection)) {
				relicCollection = db.getCollection(collection);
			} else {
				DBObject dbo = new BasicDBObject();
				dbo.put("capped", false);
				relicCollection = db.createCollection(collection, dbo);
			}
			relicCollection.ensureIndex(new BasicDBObject("relic.identifier", 1).append("unique", true));
			relicCollection.ensureIndex(new BasicDBObject("resolver.identifier", 1).append("unique", true));
		} catch (UnknownHostException e) {
			throw new RelicException("Unknown host for mongo connection.", e);
		} catch (MongoException e) {
			throw new RelicException("An exception from MongoDB was encountered,", e);
		}
	}

	public MongoStore(String host, int port, String dbname, String collection, String user, String pass) throws RelicException {
		try {
			mongo = new Mongo(host, port);
			db = mongo.getDB(dbname);
			if (db.authenticate(user, pass.toCharArray())) {
				if (db.collectionExists(collection)) {
					relicCollection = db.getCollection(collection);
				} else {
					DBObject dbo = new BasicDBObject();
					dbo.put("capped", false);
					relicCollection = db.createCollection(collection, dbo);
				}
				relicCollection.ensureIndex(new BasicDBObject("relic.identifier", 1).append("unique", true));
				relicCollection.ensureIndex(new BasicDBObject("resolver.identifier", 1).append("unique", true));
			} else {
				throw new RelicException("Unable to authenticate.");
			}
		} catch (UnknownHostException e) {
			throw new RelicException("Unknown host for mongo connection.", e);
		} catch (MongoException e) {
			throw new RelicException("An exception from MongoDB was encountered,", e);
		}
	}

	@Override
	public Relic getRelic(String relicId) {
		BasicDBObject bdo = new BasicDBObject();
		bdo.put("relic.identifier", relicId);
		DBCursor find = relicCollection.find(bdo);
		if (find.size() == 1) {
			DBObject dbo = find.next();
			return MongoMapper.DBObjectToRelic(dbo);
		} else if (find.size() > 1) {
			throw new RelicRuntimeException("The relic collection is possibly corrupt, more than one entry was found for the identifier [" + relicId + "].");
		} else {
			return null;
		}
	}

	@Override
	public Relic[] getAllRelics() {
		Relic[] result = null;
		BasicDBObject bdo = new BasicDBObject();
		bdo.put("relic.identifier", new BasicDBObject("$exists", true));
		DBCursor find = relicCollection.find(bdo);
		result = new Relic[find.size()];
		int i = 0;
		while (find.hasNext()) {
			DBObject dbo = find.next();
			result[i] = MongoMapper.DBObjectToRelic(dbo);
			i++;
		}
		return result;
	}

	@Override
	public ResolverDescriptor getResolverDescriptor(String relicId, String environment) {
		BasicDBObject bdo = new BasicDBObject();
		bdo.put("resolver.identifier", relicId);
		bdo.put("resolver.environment", environment);
		DBCursor find = relicCollection.find(bdo);
		if (find.size() == 1) {
			DBObject dbo = find.next();
			return MongoMapper.DBObjectToResolverDescriptor(dbo);
		} else if (find.size() > 1) {
			throw new RelicRuntimeException("The relic collection is possibly corrupt, more than one entry was found for the identifier [" + relicId + "] and environment [" + environment + "].");
		} else {
			return null;
		}
	}

	@Override
	public boolean putRelic(Relic r) {
		BasicDBObject bdo = new BasicDBObject();
		bdo.put("relic.identifier", r.getIdentifier());
		WriteResult update = relicCollection.update(bdo, MongoMapper.relicToDBObject(r), true, false);
		return update.getLastError().ok();
	}

	@Override
	public boolean putResolverDescriptor(ResolverDescriptor rd) {
		BasicDBObject bdo = new BasicDBObject();
		bdo.put("resolver.identifier", rd.getIdentifier());
		bdo.put("resolver.environment", rd.getEnvironment());
		WriteResult update = relicCollection.update(bdo, MongoMapper.resolverDescriptorToDBObject(rd), true, false);
		return update.getLastError().ok();
	}
	
	@Override
	public void clearRelicDB(){
		String collection = relicCollection.getName();
		relicCollection.drop();
		
		if (db.collectionExists(collection)) {
			relicCollection = db.getCollection(collection);
		} else {
			DBObject dbo = new BasicDBObject();
			dbo.put("capped", false);
			relicCollection = db.createCollection(collection, dbo);
		}
		relicCollection.ensureIndex(new BasicDBObject("relic.identifier", 1).append("unique", true));
		relicCollection.ensureIndex(new BasicDBObject("resolver.identifier", 1).append("unique", true));
	}

}
