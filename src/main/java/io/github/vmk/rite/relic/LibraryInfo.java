// Copyright 2011 - V.M. Kattenberg - vm.kattenberg@gmail.com
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

package io.github.vmk.rite.relic;

/**
 * LibraryInfo
 * 
 * @author vm.kattenberg
 */
public class LibraryInfo {

    public static String getVersion() {
        String version = LibraryInfo.class.getPackage().getImplementationVersion();
        if (version == null) {
            version = "Undetermined.";
        }
        return version;
    }
}
