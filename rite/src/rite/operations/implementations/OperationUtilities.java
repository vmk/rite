// Copyright 2011 - V.M. Kattenberg - vm.kattenberg@gmail.com
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

package rite.operations.implementations;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import rite.operations.OperationPropertyKeys;

/**
 * OperationUtilities
 * 
 * @author vm.kattenberg
 */
public class OperationUtilities {

    public static void initialize(Properties p, OperationPropertyKeys[] keys) {
        for (OperationPropertyKeys opk : keys) {
            if (!opk.isNullable()) {
                p.setProperty(opk.getKey(), opk.getDefaultValue());
            }
        }
    }

    public static String getStackTraceAsString(Throwable t) {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        t.printStackTrace(printWriter);
        return result.toString();
    }
}
