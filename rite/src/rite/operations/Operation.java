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

package rite.operations;

import java.util.List;

/**
 * 
 * Operation
 *
 * @author vm.kattenberg
 */
public interface Operation {
    public abstract void setIdentifier(String identifier);

    public abstract String getIdentifier();

    public abstract void setProperty(OperationPropertyKeys key, String value);

    public abstract String setProperty(String key, String value);

    public abstract String getProperty(OperationPropertyKeys key);

    public abstract String getProperty(String key);

    public abstract boolean hasProperty(OperationPropertyKeys key);

    public abstract void complete();

    public abstract boolean hasCompleted();
    
    public abstract void fail();

    public abstract boolean hasFailed();

    public abstract OperationPropertyKeys[] getPropertyKeys();
    
    public abstract List<String> getCurrentOperationPropertyKeys();
    
    public abstract void reset();
}
