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
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.io.IOUtils;

import rite.relic.Relic;
import rite.relic.exceptions.RelicException;
import rite.relic.resolvers.RelicResolver;
import rite.relic.resolvers.ResolverDescriptor;

/**
 * LocalFileSystemResolver
 * 
 * @author vm.kattenberg
 */
public class LocalFileSystemResolver implements RelicResolver {
    public static final String ENVIRONMENT = "localfs";

    public enum DescriptorKeys {
        ABSOLUTEPATH("absolutepath");

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
        File externalFile = new File(rd.getProperty(DescriptorKeys.ABSOLUTEPATH.getKey()));
        try {
            FileInputStream fis = new FileInputStream(externalFile);
            FileOutputStream fos = new FileOutputStream(internalFile);
            IOUtils.copyLarge(fis, fos);
            fis.close();
            fos.flush();
            fos.close();
        } catch (Exception e) {
            throw new RelicException("Could not retrieve the file to the working directory", e);
        }
    }

    @Override
    public void resolveExternal(Relic r, ResolverDescriptor rd, File cacheDirectory) throws RelicException {
        File internalFile = new File(cacheDirectory, r.getFileName());
        File externalFile = new File(rd.getProperty(DescriptorKeys.ABSOLUTEPATH.getKey()));
        try {
            FileInputStream fis = new FileInputStream(internalFile);
            FileOutputStream fos = new FileOutputStream(externalFile);
            IOUtils.copyLarge(fis, fos);
            fis.close();
            fos.flush();
            fos.close();
        } catch (Exception e) {
            throw new RelicException("Could not copy the file to the external directory", e);
        }
    }

    @Override
    public void deleteExternal(Relic r, ResolverDescriptor rd, File cacheDirectory) throws RelicException {
        File externalFile = new File(rd.getProperty(DescriptorKeys.ABSOLUTEPATH.getKey()));
        externalFile.delete();
    }

    @Override
    public void deleteInternal(Relic r, ResolverDescriptor rd, File cacheDirectory) throws RelicException {
        File internalFile = new File(cacheDirectory, r.getFileName());
        internalFile.delete();
    }
}
