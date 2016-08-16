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

package rite.operations.implementations.fileresolution;

import java.io.File;

import rite.operations.Operation;
import rite.operations.OperationPropertyKeys;
import rite.operations.implementations.GenericOperation;
import rite.operations.implementations.OperationUtilities;

public class CheckFileExistsOperation extends GenericOperation {
	private static final long serialVersionUID = 700762448204350867L;

	public enum PropertyKeys implements OperationPropertyKeys {
		PATH("path", "", false);

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

	public CheckFileExistsOperation() {
		super();
		OperationUtilities.initialize(this, PropertyKeys.values());
	}

	@Override
	public Operation call() throws Exception {
		try {
			File f = new File(getPath());
			if (!f.exists()) {
				this.setProperty(GenericOperation.PropertyKeys.ERROR, "The file " + f.getAbsolutePath() + " does not exist!");
				this.fail();
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

	public void setPath(String path) {
		setProperty(PropertyKeys.PATH, path);
	}

	public String getPath() {
		return getProperty(PropertyKeys.PATH);
	}
}
