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

import java.util.List;

import nl.vu.psy.rite.operations.Operation;
import nl.vu.psy.rite.operations.Recipe;
import nl.vu.psy.rite.operations.Step;
import nl.vu.psy.rite.operations.implementations.GenericOperation;
import nl.vu.psy.rite.persistence.TimeStamp;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * MongoRecipeMapper
 * 
 * @author vm.kattenberg
 */
public class MongoRecipeMapper {

    public static DBObject recipeToDBObject(Recipe r) {
        DBObject result = new BasicDBObject();
        result.put("recipe", r.getIdentifier());
        result.put("clientid", r.getClientId());
        result.put("timestamp", TimeStamp.dateToString(r.getTimeStamp()));
        result.put("timeout", r.getTimeout());
        result.put("completed", r.hasCompleted());
        result.put("failed", r.hasFailed());
        result.put("resetontimeout", r.resetOnTimeout());
        result.put("resetonfailure", r.resetOnFailure());

        BasicDBList steps = new BasicDBList();
        for (Step s : r) {
            steps.add(stepToDBObject(s));
        }
        result.put("steps", steps);
        return result;
    }

    public static Recipe DBObjectToRecipe(DBObject dbo) {
        if (dbo.get("recipe") == null || dbo.get("recipe").equals("")) {
            return null;
        } else {
            // FIXME the casting is a recipe for exceptions when data is modified by other mongo clients (e.g. mongohub seems to change the timeout to integers)
            Recipe r = new Recipe((String) dbo.get("recipe"));
            r.setClientId((String) dbo.get("clientid"));
            r.setTimeStamp(TimeStamp.stringToDate((String) dbo.get("timestamp")));
            r.setCompleted((Boolean) dbo.get("completed"));
            r.setFailed((Boolean) dbo.get("failed"));
            r.setTimeout(((Long) dbo.get("timeout")));
            r.setResetOnFailure((Boolean) dbo.get("resetonfailure"));
            r.setResetOnTimeout((Boolean) dbo.get("resetontimeout"));

            BasicDBList steps = (BasicDBList) dbo.get("steps");
            for (Object step : steps) {
                Step s = DBObjectToStep((DBObject) step);
                r.add(s);
            }
            return r;
        }
    }

    private static Step DBObjectToStep(DBObject dbo) {
        if (dbo.get("step") == null || dbo.get("step").equals("")) {
            return null;
        } else {
            Step s = new Step((String) dbo.get("step"));
            BasicDBList operations = (BasicDBList) dbo.get("operations");
            for (Object operation : operations) {
                Operation o = DBObjectToOperation((DBObject) operation);
                s.add(o);
            }
            return s;
        }
    }

    private static Operation DBObjectToOperation(DBObject dbo) {
        if (dbo.get("operation") == null || dbo.get("operation").equals("")) {
            return null;
        } else {
            BasicDBObject innerFields = (BasicDBObject) dbo.get("operation");
            Operation o = new GenericOperation();
            for (String key : innerFields.keySet()) {
                o.setProperty(key, innerFields.getString(key));
            }

            // FIXME move all this reflection garbage to the GenericOperation class (also see the ComputeService class)  
            Operation ro = null;
            if (o.getProperty(GenericOperation.PropertyKeys.CLASS) != null || o.getProperty(GenericOperation.PropertyKeys.CLASS) != "") {
                Class<?> c;
                try {
                    c = Class.forName(o.getProperty(GenericOperation.PropertyKeys.CLASS));
                    ro = (Operation) c.newInstance();
                    for (String key : o.getCurrentOperationPropertyKeys()) {
                        ro.setProperty(key, o.getProperty(key));
                    }
                } catch (Exception e) {
                    System.out.println("Could not instantiate the operation class " + o.getProperty(GenericOperation.PropertyKeys.CLASS) + ". Returning GenericOperation.");
                }
            }
            return (ro == null) ? o : ro;
        }
    }

    private static DBObject stepToDBObject(Step s) {
        DBObject result = new BasicDBObject();
        result.put("step", s.getIdentifier());
        BasicDBList operations = new BasicDBList();
        for (Operation o : s) {
            operations.add(operationToDBObject(o));
        }
        result.put("operations", operations);
        return result;
    }

    private static DBObject operationToDBObject(Operation o) {
        BasicDBObject result = new BasicDBObject();
        BasicDBObject innerFields = new BasicDBObject();
        List<String> operationKeys = o.getCurrentOperationPropertyKeys();
        for (String key : operationKeys) {
            innerFields.put(key, o.getProperty(key));
        }
        result.put("operation", innerFields);
        return result;
    }

}
