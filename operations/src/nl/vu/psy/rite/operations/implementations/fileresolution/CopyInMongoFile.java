package nl.vu.psy.rite.operations.implementations.fileresolution;

import java.io.File;

import nl.vu.psy.rite.operations.Operation;
import nl.vu.psy.rite.operations.OperationPropertyKeys;
import nl.vu.psy.rite.operations.implementations.GenericOperation;
import nl.vu.psy.rite.operations.implementations.OperationUtilities;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

public class CopyInMongoFile extends GenericOperation {
	private static final long serialVersionUID = 426425234812416095L;

	public enum PropertyKeys implements OperationPropertyKeys {
		FILENAME("filename", "", false), HOST("hostname", "", false), PORT("port", "", false), DBNAME("dbname", "", false), AUTH("auth", "false", false), USER("user", null, true), PASS("pass", null, true);

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

	public CopyInMongoFile() {
		super();
		OperationUtilities.initialize(this, PropertyKeys.values());
	}

	@Override
	public Operation call() throws Exception {
		try {
			Mongo mongo = new Mongo(getHostname(), getPort());
			DB db = mongo.getDB(getDbName());
			if (shouldAuth()) {
				db.authenticate(getUserName(), getPassword().toCharArray());
			}
			GridFS gfs = new GridFS(db);
			String filename = getFileName();
			GridFSDBFile file = gfs.findOne(filename);
			file.writeTo(filename);
			File f = new File(filename);
			if (!f.exists()) {
				throw new Exception("The file " + filename + " does not exist locally!");
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

	public void setFileName(String name) {
		setProperty(PropertyKeys.FILENAME, name);
	}

	public String getFileName() {
		return getProperty(PropertyKeys.FILENAME);
	}

	public void setHostname(String host) {
		setProperty(PropertyKeys.HOST, host);
	}

	public String getHostname() {
		return getProperty(PropertyKeys.HOST);
	}

	public void setPort(int port) {
		setProperty(PropertyKeys.PORT, Integer.toString(port));
	}

	public int getPort() {
		return Integer.parseInt(getProperty(PropertyKeys.PORT));
	}

	public void setDbName(String dbName) {
		setProperty(PropertyKeys.DBNAME, dbName);
	}

	public String getDbName() {
		return getProperty(PropertyKeys.DBNAME);
	}

	public void setAuthCredentials(String user, String pass) {
		setProperty(PropertyKeys.AUTH, Boolean.toString(true));
		setProperty(PropertyKeys.USER, user);
		setProperty(PropertyKeys.PASS, pass);
	}

	public boolean shouldAuth() {
		return Boolean.parseBoolean(getProperty(PropertyKeys.AUTH));
	}

	private String getUserName() {
		return getProperty(PropertyKeys.USER);
	}

	private String getPassword() {
		return getProperty(PropertyKeys.PASS);
	}
}
