// Copyright 2012 - V.M. Kattenberg - vm.kattenberg@gmail.com
//
// This file is part of relic
//
// relic is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// relic is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with relic.  If not, see <http://www.gnu.org/licenses/>.

package rite.relic.resolvers.implementations;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import rite.relic.Relic;
import rite.relic.exceptions.RelicException;
import rite.relic.resolvers.RelicResolver;
import rite.relic.resolvers.ResolverDescriptor;

/**
 * GridSRMResolver
 * 
 * @author vm.kattenberg
 */
public class GridLCGResolver implements RelicResolver {
    public static final String ENVIRONMENT = "lcg";

    private OutputStream subProcessStdOut = System.out;
    private OutputStream subProcessStdErr = System.err;

    public enum DescriptorKeys {
        SRMURL("srmurl");

        private final String key;

        private DescriptorKeys(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

    }

    @Override
    public void resolveInternal(Relic r, ResolverDescriptor rd, File cacheDirectory) throws RelicException {
        File internalFile = new File(cacheDirectory, r.getFileName());
        String surl = rd.getProperty(DescriptorKeys.SRMURL.getKey());
        System.out.println("Copying in: lcg-cp srm://" + surl + " " + "file:///" + internalFile.getAbsolutePath());
        int exit = runCommand("lcg-cp srm://" + surl + " " + "file:///" + internalFile.getAbsolutePath());
        System.out.println("Copying in: file exists: " + internalFile.exists());
        if(exit != 0 && !internalFile.exists()) {
            throw new RelicException("Encountered nonzero exit code for lcg-cp to local file!");
        }      
    }

    @Override
    public void resolveExternal(Relic r, ResolverDescriptor rd, File cacheDirectory) throws RelicException {
        File internalFile = new File(cacheDirectory, r.getFileName());
        String surl = rd.getProperty(DescriptorKeys.SRMURL.getKey());
        System.out.println("Copying out: lcg-cp file:///" + internalFile.getAbsolutePath() + " srm://" + surl);
        int exit = runCommand("lcg-cp file:///" + internalFile.getAbsolutePath() + " srm://" + surl);
        if(exit != 0) {
            throw new RelicException("Encountered nonzero exit code for lcg-cp to remote! Check for existance returned: " + internalFile.exists());
        }
    }

    @Override
    public void deleteExternal(Relic r, ResolverDescriptor rd, File cacheDirectory) throws RelicException {
        String surl = rd.getProperty(DescriptorKeys.SRMURL.getKey());
        System.out.println("Removing remote: lcg-del srm://" + surl);
        int exit = runCommand("lcg-del srm://" + surl);
        if(exit != 0) {
            throw new RelicException("Encountered nonzero exit code for lcg-del!");
        }
    }

    @Override
    public void deleteInternal(Relic r, ResolverDescriptor rd, File cacheDirectory) throws RelicException {
        File internalFile = new File(cacheDirectory, r.getFileName());
        internalFile.delete();
    }

    private int runCommand(String commandLine) throws RelicException {
        try {
            StringTokenizer st = new StringTokenizer(commandLine, " ");
            ArrayList<String> argumentList = new ArrayList<String>();
            while (st.hasMoreTokens()) {
                argumentList.add(st.nextToken());
            }
            ProcessBuilder pb = new ProcessBuilder(argumentList);
            Process p;
            p = pb.start();

            // Process I/O in separate threads to avoid deadlock
            Thread subOut = null;
            if (subProcessStdOut != null) {
                // StdOut (only close streams if stdout is not used)
                if (subProcessStdOut.equals(System.out)) {
                    subOut = new Thread(new GridLCGResolverPipeThread(p.getInputStream(), subProcessStdOut, false));
                } else {
                    subOut = new Thread(new GridLCGResolverPipeThread(p.getInputStream(), subProcessStdOut, true));
                }
                subOut.setDaemon(true);
                subOut.start();
            }
            Thread subErr = null;
            if (subProcessStdErr != null) {
                // StdErr (only close streams if stderr is not used)
                if (subProcessStdErr.equals(System.err)) {
                    subErr = new Thread(new GridLCGResolverPipeThread(p.getErrorStream(), subProcessStdErr, false));
                } else {
                    subErr = new Thread(new GridLCGResolverPipeThread(p.getErrorStream(), subProcessStdErr, true));
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

            return p.exitValue();
        } catch (Exception e) {
            throw new RelicException("An error occurred while trying to run srmcp.", e);
        }
    }

}
