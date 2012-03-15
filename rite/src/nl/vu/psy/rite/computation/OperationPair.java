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

package nl.vu.psy.rite.computation;

import java.util.concurrent.FutureTask;

import nl.vu.psy.rite.operations.Operation;

/**
 * OperationPair
 * 
 * @author vm.kattenberg
 */
public class OperationPair {

    private String identifier;
    private Operation operation;
    private FutureTask<Operation> executingOperation;

    public OperationPair(Operation operation, FutureTask<Operation> executingOperation) {
        setOperation(operation);
        setIdentifier(operation.getIdentifier());
        setExecutingOperation(executingOperation);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public FutureTask<Operation> getExecutingOperation() {
        return executingOperation;
    }

    public void setExecutingOperation(FutureTask<Operation> executingOperation) {
        this.executingOperation = executingOperation;
    }

}
