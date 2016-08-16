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

package rite.relic.persistence.mongo;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import rite.relic.Relic;
import rite.relic.resolvers.ResolverDescriptor;

/**
 * MongoMapper
 * 
 * @author vm.kattenberg
 */
public class MongoMapper {

    public static DBObject relicToDBObject(Relic r) {
        DBObject result = new BasicDBObject();
        DBObject relic = new BasicDBObject();
        relic.put("identifier", r.getIdentifier());
        relic.put("filename", r.getFileName());
        relic.put("description", r.getDescription());
        BasicDBList properties = new BasicDBList();
        for (Object s : r.getProperties().keySet()) {
            BasicDBObject prop = new BasicDBObject();
            String key = (String) s;
            prop.put(key, r.getProperty(key));
            properties.add(prop);
        }
        relic.put("properties", properties);
        result.put("relic", relic);
        return result;
    }

    public static Relic DBObjectToRelic(DBObject dbo) {
        DBObject innerFields = (DBObject) dbo.get("relic");
        if (innerFields == null || "".equals(innerFields)) {
            return null;
        } else {
            Relic result = new Relic((String) innerFields.get("identifier"));
            result.setFileName((String) innerFields.get("filename"));
            result.setDescription((String) innerFields.get("description"));
            BasicDBList properties = (BasicDBList) innerFields.get("properties");
            for (Object property : properties) {
                BasicDBObject prop = ((BasicDBObject) property);
                // Skip elements that are not simple key-value pairs
                if (prop.keySet().size() == 1) {
                    for (String s2 : prop.keySet()) {
                        result.setProperty(s2, (String) prop.get(s2));
                    }
                }
            }
            return result;
        }
    }

    public static DBObject resolverDescriptorToDBObject(ResolverDescriptor r) {
        DBObject result = new BasicDBObject();
        DBObject resolverDescriptor = new BasicDBObject();
        resolverDescriptor.put("identifier", r.getIdentifier());
        resolverDescriptor.put("environment", r.getEnvironment());
        BasicDBList properties = new BasicDBList();
        for (Object s : r.getProperties().keySet()) {
            BasicDBObject prop = new BasicDBObject();
            String key = (String) s;
            prop.put(key, r.getProperty(key));
            properties.add(prop);
        }
        resolverDescriptor.put("properties", properties);
        result.put("resolver", resolverDescriptor);
        return result;
    }

    public static ResolverDescriptor DBObjectToResolverDescriptor(DBObject dbo) {
        DBObject innerFields = (DBObject) dbo.get("resolver");
        if (innerFields == null || "".equals(innerFields)) {
            return null;
        } else {
            String identifier = (String) innerFields.get("identifier");
            String environment = (String) innerFields.get("environment");
            ResolverDescriptor result = new ResolverDescriptor(identifier, environment);
            BasicDBList properties = (BasicDBList) innerFields.get("properties");
            for (Object property : properties) {
                BasicDBObject prop = ((BasicDBObject) property);
                // Skip elements that are not simple key-value pairs
                if (prop.keySet().size() == 1) {
                    for (String s2 : prop.keySet()) {
                        result.setProperty(s2, (String) prop.get(s2));
                    }
                }
            }
            return result;
        }
    }

}
