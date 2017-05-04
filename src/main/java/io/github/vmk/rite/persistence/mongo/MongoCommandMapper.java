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

package io.github.vmk.rite.persistence.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.github.vmk.rite.persistence.ClientCommand;

/**
 * MongoCommandMapper
 * 
 * @author vm.kattenberg
 */
public class MongoCommandMapper {
    public static DBObject clientCommandToDBObject(ClientCommand co) {
        DBObject result = new BasicDBObject();
        DBObject innerFields = new BasicDBObject();
        innerFields.put("clientid", co.getClientId());
        innerFields.put("command", co.getCommand().toString());
        result.put("clientcommand", innerFields);
        return result;
    }

    public static ClientCommand DBObjectToClientCommand(DBObject dbo) {
        DBObject innerFields = (DBObject) dbo.get("clientcommand");
        if (innerFields == null || "".equals(innerFields)) {
            return null;
        } else {
            String clientId = (String) innerFields.get("clientid");
            ClientCommand.Commands command = ClientCommand.commandsFromString((String) innerFields.get("command"));
            return new ClientCommand(clientId, command);
        }
    }
}
