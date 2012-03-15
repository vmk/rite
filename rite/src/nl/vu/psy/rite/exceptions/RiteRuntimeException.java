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

package nl.vu.psy.rite.exceptions;

/**
 * RiteRuntimeException
 * 
 * @author vm.kattenberg
 */
public class RiteRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -7970110927576250170L;

    public RiteRuntimeException(String message) {
        super(message);
    }

    public RiteRuntimeException(String message, Throwable t) {
        super(message, t);
    }

    public RiteRuntimeException(Exception e) {
        super(e);
    }
}
