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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;

import rite.operations.Operation;
import rite.operations.OperationPropertyKeys;

/**
 * 
 * GenericOperation
 *
 * @author vm.kattenberg
 */
public class GenericOperation extends Properties implements Operation, Callable<Operation> {
    // TODO add addError function. Now error can be overwritten
    // TODO add typed getters for properties to Operation. E.g. getIntProperty, getLongProperty
    private static final long serialVersionUID = -8695662135304967759L;

    public enum PropertyKeys implements OperationPropertyKeys {
        ID("id", null, false), COMPLETED("completed", "false", false), ERROR("error", null, true), FAILED("failed", "false", false), CLASS("class",
                "rite.operations.implementations.GenericOperation", false);

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
            if (this == ID) {
                // The default specified at the level of the enumeration will be static (constant) for all instances.
                // If defaults should vary according to a pattern or function (as in the case for ID) it can be specified like this.
                return UUID.randomUUID().toString();
            }
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

    public GenericOperation() {
        super();
        OperationUtilities.initialize(this, PropertyKeys.values());
        this.setProperty(PropertyKeys.CLASS, this.getClass().getCanonicalName());
    }

    @Override
    public void setProperty(OperationPropertyKeys key, String value) {
        this.setProperty(key.getKey(), value);

    }

    @Override
    public String setProperty(String key, String value) {
        // An unfortunate naming clash between Properties and Operation.
        // Watch this - should the implementation of Properties ever change.
        return (String) this.put(key, value);
    }

    @Override
    public String getProperty(OperationPropertyKeys key) {
        return this.getProperty(key.getKey());
    }

    @Override
    public boolean hasProperty(OperationPropertyKeys key) {
        return this.containsKey(key.getKey());
    }

    @Override
    public void setIdentifier(String identifier) {
        setProperty(PropertyKeys.ID, identifier);
    }

    @Override
    public String getIdentifier() {
        return getProperty(PropertyKeys.ID);
    }

    @Override
    public void complete() {
        setProperty(PropertyKeys.COMPLETED, "true");
    }

    @Override
    public boolean hasCompleted() {
        return Boolean.parseBoolean(getProperty(PropertyKeys.COMPLETED));
    }

    @Override
    public void fail() {
        setProperty(PropertyKeys.FAILED, "true");
    }

    @Override
    public boolean hasFailed() {
        return Boolean.parseBoolean(getProperty(PropertyKeys.FAILED));
    }

    public OperationPropertyKeys[] getPropertyKeys() {
        return PropertyKeys.values();
    }

    @Override
    public Operation call() throws Exception {
        // TODO add reflection for CLASS property. This is now done in the compute service
        // Empty implementation: complete on call.
        complete();
        return this;
    }

    @Override
    public List<String> getCurrentOperationPropertyKeys() {
        @SuppressWarnings("unchecked")
        Enumeration<Object> propertyKeys = (Enumeration<Object>) this.propertyNames();
        ArrayList<String> result = new ArrayList<String>();
        while (propertyKeys.hasMoreElements()) {
            result.add((String) propertyKeys.nextElement());
        }
        return result;
    }

    @Override
    public void reset() {
        setProperty(PropertyKeys.COMPLETED, "false");
        setProperty(PropertyKeys.FAILED, "false");
        // setProperty(PropertyKeys.ERROR, null);
        // TODO maybe add a remove property method
        this.remove(PropertyKeys.ERROR.getKey());
    }

}
