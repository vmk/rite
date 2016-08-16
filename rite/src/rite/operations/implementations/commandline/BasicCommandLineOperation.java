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

package rite.operations.implementations.commandline;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import rite.operations.Operation;
import rite.operations.OperationPropertyKeys;
import rite.operations.implementations.GenericOperation;
import rite.operations.implementations.OperationUtilities;

/**
 * BasicCommandLineOperation: A very basic implementation of running a command line
 * 
 * @author vm.kattenberg
 */
public class BasicCommandLineOperation extends GenericOperation {
    private static final long serialVersionUID = -1858033936573061148L;

    public enum PropertyKeys implements OperationPropertyKeys {
        COMMANDLINE("commandline", "", false), CHECKEXIT("checkexitcode", "false", false), EXITCODE("exitcode", "0", false);

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

    // private InputStream subProcessStdIn = System.in;
    private OutputStream subProcessStdOut = System.out;
    private OutputStream subProcessStdErr = System.err;

    public BasicCommandLineOperation() {
        super();
        OperationUtilities.initialize(this, PropertyKeys.values());
    }

    public void setCommandLine(String value) {
        this.setProperty(PropertyKeys.COMMANDLINE, value);
    }

    public String getCommandLine() {
        return getProperty(PropertyKeys.COMMANDLINE);
    }

    public void setDesiredExitCode(int code) {
        this.setProperty(PropertyKeys.EXITCODE, Integer.toString(code));
    }

    public int getDesiredExitCode() {
        return Integer.parseInt(getProperty(PropertyKeys.EXITCODE));
    }

    public void setCheckExitCode(boolean check) {
        setProperty(PropertyKeys.CHECKEXIT, Boolean.toString(check));
    }

    public boolean checkExitCode() {
        return Boolean.parseBoolean(getProperty(PropertyKeys.CHECKEXIT));
    }
    
    @Override
    public Operation call() throws Exception {
        try {
            String commandLine = getCommandLine();
            System.out.println("Running commandline: " + commandLine);
            StringTokenizer st = new StringTokenizer(commandLine, " ");
            ArrayList<String> argumentList = new ArrayList<String>();
            while (st.hasMoreTokens()) {
                argumentList.add(st.nextToken());
            }
            ProcessBuilder pb = new ProcessBuilder(argumentList);
            Process p;
            p = pb.start();

            // Process output and error stream I/O in separate threads to avoid deadlock

            // StdIn (streams should always be closed)
            // Thread subIn = null;
            // if (subProcessStdIn != null) {
            // subIn = new Thread(new PipeThread(subProcessStdIn, p.getOutputStream(), true));
            // subIn.setDaemon(true);
            // subIn.start();
            // }

            Thread subOut = null;
            if (subProcessStdOut != null) {
                // StdOut (only close streams if stdout is not used)
                if (subProcessStdOut.equals(System.out)) {
                    subOut = new Thread(new PipeThread(p.getInputStream(), subProcessStdOut, false));
                } else {
                    subOut = new Thread(new PipeThread(p.getInputStream(), subProcessStdOut, true));
                }
                subOut.setDaemon(true);
                subOut.start();
            }
            Thread subErr = null;
            if (subProcessStdErr != null) {
                // StdErr (only close streams if stderr is not used)
                if (subProcessStdErr.equals(System.err)) {
                    subErr = new Thread(new PipeThread(p.getErrorStream(), subProcessStdErr, false));
                } else {
                    subErr = new Thread(new PipeThread(p.getErrorStream(), subProcessStdErr, true));
                }
                subErr.setDaemon(true);
                subErr.start();
            }
            p.waitFor();

            if (subErr != null) {
                if (subErr.isAlive()) {
                    subErr.interrupt();
                }
                subErr = null;
            }

            if (subOut != null) {
                if (subOut.isAlive()) {
                    subOut.interrupt();
                }
                subOut = null;
            }

            // if (subIn != null) {
            // if (subIn.isAlive()) {
            // subIn.interrupt();
            // }
            // subIn = null;
            // }
            int exit = p.exitValue();
            int desiredExit = Integer.parseInt(getProperty(PropertyKeys.EXITCODE));
            if (exit != desiredExit) {
                if(checkExitCode()) {
                    fail();
                }
                setProperty(GenericOperation.PropertyKeys.ERROR, "The exitcode was " + exit + " instead of " + desiredExit + ".");
            }
        } catch (Exception e) {
            setProperty(GenericOperation.PropertyKeys.ERROR, OperationUtilities.getStackTraceAsString(e));
            fail();
            complete();
            return this;
        }
        complete();
        return this;
    }

}
