// Copyright 2012 - V.M. Kattenberg - vm.kattenberg@gmail.com
//
// This file is part of operations
//
// operations is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// operations is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with operations.  If not, see <http://www.gnu.org/licenses/>.

package io.github.vmk.rite.operations.implementations.examples;

import io.github.vmk.rite.operations.Operation;
import io.github.vmk.rite.operations.OperationPropertyKeys;
import io.github.vmk.rite.operations.implementations.GenericOperation;
import io.github.vmk.rite.operations.implementations.OperationUtilities;

/**
 * ExampleOperation
 * 
 * @author vm.kattenberg
 */
public class ExampleOperation extends GenericOperation {
    private static final long serialVersionUID = 4939060859379273206L;

    public enum PropertyKeys implements OperationPropertyKeys {
        ITERATIONS("iterations", "10", false), DELAY("delay", "6000", false), MESSAGE("message", "Testing, one, two, three...", false);

        private final String key;
        private final String defaultValue;
        private final boolean nullable;

        private PropertyKeys(String key, String defaultValue, boolean nullable) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.nullable = nullable;
        }

        @Override
        public String getDefaultValue() {
            return defaultValue;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public boolean isNullable() {
            return nullable;
        }
    }

    public ExampleOperation() {
        super();
        OperationUtilities.initialize(this, PropertyKeys.values());
    }

    @Override
    public Operation call() throws Exception {
        try {
            int iterations = Integer.parseInt(getProperty(PropertyKeys.ITERATIONS));
            long delay = Long.parseLong(getProperty(PropertyKeys.DELAY));
            String message = getProperty(PropertyKeys.MESSAGE);
            for (int i = 0; i < iterations; i++) {
                System.out.println(i + ".) [" + message + "]");
                if (delay > 0) {
                    Thread.sleep(delay);
                }
            }
        } catch (Exception e) {
            this.setProperty(GenericOperation.PropertyKeys.ERROR, OperationUtilities.getStackTraceAsString(e));
            this.fail();
            this.complete();
            return this;
        }
        this.complete();
        return this;
    }

}
