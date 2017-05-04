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

package io.github.vmk.rite.operations;

import java.util.ArrayList;

/**
 * 
 * Step
 *
 * @author vm.kattenberg
 */
public class Step extends ArrayList<Operation> {
    private static final long serialVersionUID = 7391059921193015926L;

    private String identifier;

    public Step(String identifier) {
        super();
        setIdentifier(identifier);
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean hasCompleted() {
        boolean result = true;
        for (Operation o : this) {
            result &= o.hasCompleted();
        }
        return result;
    }
    
    public boolean hasFailed() {
        boolean result = false;
        for (Operation o : this) {
            result |= o.hasFailed();
        }
        return result;
    }
}
