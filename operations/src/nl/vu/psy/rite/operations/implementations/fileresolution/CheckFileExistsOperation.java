package nl.vu.psy.rite.operations.implementations.fileresolution;

import java.io.File;

import nl.vu.psy.rite.operations.Operation;
import nl.vu.psy.rite.operations.OperationPropertyKeys;
import nl.vu.psy.rite.operations.implementations.GenericOperation;
import nl.vu.psy.rite.operations.implementations.OperationUtilities;

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
