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

import java.io.File;

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
public class CopyInOperation extends GenericOperation {
    private static final long serialVersionUID = 4375372204584024107L;

    public enum PropertyKeys implements OperationPropertyKeys {
        RELIC("relic", "", false), CHECKEXISTS("checkexists", "false", false);

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

    public CopyInOperation() {
        super();
        OperationUtilities.initialize(this, PropertyKeys.values());
    }

    @Override
    public Operation call() throws Exception {
        try {
            FileCache fileCache = Rite.getInstance().getFileCache();
            fileCache.getFileCache().importRelic(getRelicId());
            if (checkExists()) {
                File f = fileCache.getFileCache().getRelic(getRelicId(), false);
                if (!f.exists()) {
                    this.setProperty(GenericOperation.PropertyKeys.ERROR, "The file " + f.getAbsolutePath() + " does not exist!");
                    this.fail();
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

    public void setCheckExists(boolean check) {
        setProperty(PropertyKeys.CHECKEXISTS, Boolean.toString(check));
    }

    public boolean checkExists() {
        return Boolean.parseBoolean(getProperty(PropertyKeys.CHECKEXISTS));
    }

    public void setRelicId(String relicId) {
        setProperty(PropertyKeys.RELIC, relicId);
    }

    public String getRelicId() {
        return getProperty(PropertyKeys.RELIC);
    }

}
