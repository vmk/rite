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

package rite.exceptions;

/**
 * RiteException
 * 
 * @author vm.kattenberg
 */
public class RiteException extends Exception {
    private static final long serialVersionUID = -645810125718338191L;

    public RiteException(String message) {
        super(message);
    }

    public RiteException(String message, Throwable t) {
        super(message, t);
    }

    public RiteException(Exception e) {
        super(e);
    }

}
