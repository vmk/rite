package io.github.vmk.rite.operations.implementations.fileresolution;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import io.github.vmk.rite.Rite;
import io.github.vmk.rite.operations.Operation;
import io.github.vmk.rite.operations.OperationPropertyKeys;
import io.github.vmk.rite.operations.implementations.GenericOperation;
import io.github.vmk.rite.operations.implementations.OperationUtilities;

public class CopyInMongoFile extends GenericOperation {
	private static final long serialVersionUID = 426425234812416095L;

	public enum PropertyKeys implements OperationPropertyKeys {
		FILENAME("filename", "", false);

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
			String ritePropertiesFilename = Rite.getInstance().getProperty(Rite.PropertyKeys.HOST);
			Properties hostProps = new Properties();
			hostProps.load(new FileInputStream(ritePropertiesFilename));
			String hostname = hostProps.getProperty("hostname");
			int port = Integer.parseInt(hostProps.getProperty("port"));
			String dbname = hostProps.getProperty("dbname");
			boolean auth = Boolean.parseBoolean(hostProps.getProperty("auth"));
			Mongo mongo = new Mongo(hostname, port);
			DB db = mongo.getDB(dbname);
			if (auth) {
				String user = hostProps.getProperty("user");
				String pass = hostProps.getProperty("pass");
				db.authenticate(user, pass.toCharArray());
			}
			GridFS gfs = new GridFS(db);
			String filename = getFileName();
			GridFSDBFile file = gfs.findOne(filename);
			file.writeTo(filename);
			File f = new File(filename);
			if (!f.exists()) {
				throw new Exception("The file " + filename + " does not exist locally!");
			}
			mongo.close();
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

}
