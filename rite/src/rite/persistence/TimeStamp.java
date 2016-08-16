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

package rite.persistence;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TimeStamp
 * 
 * @author vm.kattenberg
 */
public class TimeStamp {
    // TODO add clock synchronization (e.g. via NTP)
    private static SimpleDateFormat timeStampFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.S");

    public static String dateToString(Date d) {
        if (d == null) {
            return null;
        } else {
            return timeStampFormat.format(d);
        }
    }

    public static Date stringToDate(String s) {
        try {
            return timeStampFormat.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }
}
