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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import rite.persistence.ClientInfo;
import rite.persistence.TimeStamp;

/**
 * MongoInfoMapper
 * 
 * @author vm.kattenberg
 */
public class MongoInfoMapper {
    public static DBObject clientInfoToDBObject(ClientInfo ro, boolean listWorkingDir, boolean outputFiles) {
        DBObject result = new BasicDBObject();
        DBObject innerFields = new BasicDBObject();
        innerFields.put("clientid", ro.getClientId());
        innerFields.put("clientstart", TimeStamp.dateToString(ro.getClientStart()));
        innerFields.put("clienthost", ro.getClientHost());
        innerFields.put("workingdirectory", ro.getWorkingDirectory().getAbsolutePath());
        BasicDBList fileList = new BasicDBList();
        if (listWorkingDir) {
            String[] list = ro.getWorkingDirectory().list();
            if (list != null) {
                for (String f : list) {
                    fileList.add(f);
                }
            } else {
                fileList.add("File.list() returned null for directory " + ro.getWorkingDirectory().getAbsolutePath());
            }
        } else {
            fileList.add("Listing is turned off.");
        }
        innerFields.put("directorylisting", fileList);
        innerFields.put("recipeid", ro.getRecipeId());
        innerFields.put("recipestart", TimeStamp.dateToString(ro.getRecipeStart()));
        innerFields.put("recipeend", TimeStamp.dateToString(ro.getRecipeEnd()));
        if(outputFiles){
            innerFields.put("stdout", fileToString(ro.getStandardOut()));
            innerFields.put("stderr", fileToString(ro.getStandardError()));
        } else {
            innerFields.put("stdout", "Listing is turned off.");
            innerFields.put("stderr", "Listing is turned off.");
        }
        innerFields.put("recipefailed", ro.hasRecipeFailed());
        result.put("clientinfo", innerFields);
        return result;
    }

    public static String DBObjectToString(DBObject dbo) {
        DBObject innerFields = (DBObject) dbo.get("clientinfo");
        if (innerFields == null || "".equals(innerFields)) {
            return null;
        } else {
            StringBuffer result = new StringBuffer();
            result.append("clientid: " + innerFields.get("clientid") + "\n");
            result.append("clienthost: " + innerFields.get("clienthost") + "\n");
            result.append("clientstart: " + innerFields.get("clientstart") + "\n");
            result.append("workingdirectory: " + innerFields.get("workingdirectory") + "\n");
            BasicDBList files = (BasicDBList) innerFields.get("directorylisting");
            for (Object fname : files) {
                result.append("\t * " + fname + "\n");
            }
            result.append("recipeid: " + innerFields.get("recipeid") + "\n");
            result.append("recipestart: " + innerFields.get("recipestart") + "\n");
            result.append("recipeend: " + innerFields.get("recipeend") + "\n");
            result.append("recipefailed: " + innerFields.get("recipefailed") + "\n");
            result.append("standard out:\n");
            result.append(innerFields.get("stdout"));
            result.append("standard error:\n");
            result.append(innerFields.get("stderr"));

            return result.toString();
        }
    }

    private static String fileToString(File f) {
        Scanner lineScanner;
        StringBuffer result = new StringBuffer();
        try {
            lineScanner = new Scanner(new BufferedReader(new FileReader(f)));
            lineScanner.useDelimiter("\n");
            while (lineScanner.hasNext()) {
                result.append(lineScanner.next() + "\n");
            }
            lineScanner.close();
        } catch (FileNotFoundException e) {
            result.append(e.getMessage());
        }
        return result.toString();
    }

}
