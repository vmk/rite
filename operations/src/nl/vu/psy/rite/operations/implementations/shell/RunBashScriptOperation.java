package nl.vu.psy.rite.operations.implementations.shell;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import nl.vu.psy.rite.operations.Operation;
import nl.vu.psy.rite.operations.OperationPropertyKeys;
import nl.vu.psy.rite.operations.implementations.GenericOperation;
import nl.vu.psy.rite.operations.implementations.OperationUtilities;
import nl.vu.psy.rite.operations.implementations.commandline.BasicCommandLineOperation;

public class RunBashScriptOperation extends GenericOperation {
	private static final long serialVersionUID = 5440641729612829102L;
	

    public enum PropertyKeys implements OperationPropertyKeys {
        SCRIPT("script", "#!/bin/bash", false);

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

    public RunBashScriptOperation() {
        super();
        OperationUtilities.initialize(this, PropertyKeys.values());
    }

    @Override
    public Operation call() throws Exception {
        try {
        	File f = new File("script.sh");
        	BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        	bw.write(getScript());
        	bw.flush();
        	bw.close();
        	BasicCommandLineOperation bco = new BasicCommandLineOperation();
        	bco.setCommandLine("/bin/bash " + f.getAbsolutePath());
        	bco.setCheckExitCode(true);
        	bco.call();
        } catch (Exception e) {
            this.setProperty(GenericOperation.PropertyKeys.ERROR, OperationUtilities.getStackTraceAsString(e));
            this.fail();
            this.complete();
            return this;
        }
        this.complete();
        return this;
    }

    public String getScript() {
    	return getProperty(PropertyKeys.SCRIPT);
    }
    
    public void setScript(String script) {
    	setProperty(PropertyKeys.SCRIPT, script);
    }

}
