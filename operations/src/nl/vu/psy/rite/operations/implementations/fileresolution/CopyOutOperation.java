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

package nl.vu.psy.rite.operations.implementations.fileresolution;

import nl.vu.psy.relic.exceptions.RelicException;
import nl.vu.psy.rite.FileCache;
import nl.vu.psy.rite.Rite;
import nl.vu.psy.rite.operations.Operation;
import nl.vu.psy.rite.operations.OperationPropertyKeys;
import nl.vu.psy.rite.operations.implementations.GenericOperation;
import nl.vu.psy.rite.operations.implementations.OperationUtilities;

/**
 * CopyInOperation
 * 
 * @author vm.kattenberg
 */
public class CopyOutOperation extends GenericOperation {
    private static final long serialVersionUID = 4375372204584024107L;

    public enum PropertyKeys implements OperationPropertyKeys {
        RELIC("relic", "", false), DELETEONRESET("deleteonreset", "false", false);

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

    public CopyOutOperation() {
        super();
        OperationUtilities.initialize(this, PropertyKeys.values());
    }

    @Override
    public Operation call() throws Exception {
        try {
            FileCache fileCache = Rite.getInstance().getFileCache();
            fileCache.getFileCache().exportRelic(getRelicId());
        } catch (Exception e) {
            this.setProperty(GenericOperation.PropertyKeys.ERROR, OperationUtilities.getStackTraceAsString(e));
            this.fail();
            this.complete();
            return this;
        }
        this.complete();
        return this;
    }

    public void setRelicId(String relicId) {
        setProperty(PropertyKeys.RELIC, relicId);
    }

    public String getRelicId() {
        return getProperty(PropertyKeys.RELIC);
    }

    public void setDeleteOnReset(boolean delete) {
        setProperty(PropertyKeys.DELETEONRESET, Boolean.toString(delete));
    }

    public boolean deleteOnReset() {
        return Boolean.parseBoolean(getProperty(PropertyKeys.DELETEONRESET));
    }

    @Override
    public void reset() {
        super.reset();
        boolean delete = Boolean.parseBoolean(getProperty(PropertyKeys.DELETEONRESET));
        if (delete) {
            // Delete external copy
            FileCache fileCache = Rite.getInstance().getFileCache();
            try {
                fileCache.getFileCache().clearRemote(getProperty(PropertyKeys.RELIC));
            } catch (RelicException e) {
                // FIXME and what happens in this case? The operation is flagged as reset and will be executed again
                System.out.println("Reset copyout: could not clear remote copy - " + e.getMessage());
            }
        }
    }
}
