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

package nl.vu.psy.relic;

import java.io.File;
import java.util.HashMap;

import nl.vu.psy.relic.exceptions.RelicException;
import nl.vu.psy.relic.persistence.RelicStore;
import nl.vu.psy.relic.resolvers.RelicResolver;
import nl.vu.psy.relic.resolvers.RelicResolverFactory;
import nl.vu.psy.relic.resolvers.ResolverDescriptor;

/**
 * RelicFileCache
 * 
 * @author vm.kattenberg
 */
public class RelicFileCache {
    // TODO add client programs: upload, download, create from csv

    private File cacheDirectory;
    private RelicStore relicConnection;
    private RelicResolver relicResolver;
    public HashMap<String, Relic> cachedRelics;
    public HashMap<String, ResolverDescriptor> cachedDescriptors;
    private final String environment;

    public RelicFileCache(String path, RelicStore connection, String environment) throws RelicException {
        cachedRelics = new HashMap<String, Relic>();
        cachedDescriptors = new HashMap<String, ResolverDescriptor>();
        this.environment = environment;
        File f = new File(path);
        f.mkdirs();
        if (f.isDirectory()) {
            cacheDirectory = f;
            relicConnection = connection;
            try {
                relicResolver = RelicResolverFactory.getInstance().getResolverForEnvironment(environment);
            } catch (RelicException e) {
                throw new RelicException("No resolver could be found for the environment [" + environment + "].", e);
            }
        } else {
            throw new RelicException("The supplied path is not a directory or a is directory that could not be created");
        }
    }

    public void importRelic(String relicId) throws RelicException {
        loadRelic(relicId, true);
    }

    private void loadRelic(String relicId, boolean resolve) throws RelicException {
        Relic r = relicConnection.getRelic(relicId);
        if (r == null) {
            throw new RelicException("Could not retrieve the relic with id [" + relicId + "].");
        } else {
            ResolverDescriptor rd = relicConnection.getResolverDescriptor(relicId, environment);
            if (rd == null) {
                throw new RelicException("Could not retrieve the resolver descriptor for id [" + relicId + "] and environment [" + environment + "].");
            } else {
                cachedRelics.put(relicId, r);
                cachedDescriptors.put(relicId, rd);
                if (resolve) {
                    relicResolver.resolveInternal(r, rd, cacheDirectory);
                }
            }
        }
    }

    public void exportRelic(String relicId) throws RelicException {
        if (!cachedRelics.containsKey(relicId) && !cachedDescriptors.containsKey(relicId)) {
            loadRelic(relicId, false);
        }
        if (cachedRelics.containsKey(relicId) && cachedDescriptors.containsKey(relicId)) {
            Relic r = cachedRelics.get(relicId);
            ResolverDescriptor rd = cachedDescriptors.get(relicId);
            relicResolver.resolveExternal(r, rd, cacheDirectory);
        } else {
            throw new RelicException("Could not export the file as it is not present in the cache.");
        }
    }

    public void clearRemote(String relicId) throws RelicException {
        if (!cachedRelics.containsKey(relicId) && !cachedDescriptors.containsKey(relicId)) {
            loadRelic(relicId, false);
        }
        if (cachedRelics.containsKey(relicId) && cachedDescriptors.containsKey(relicId)) {
            Relic r = cachedRelics.get(relicId);
            ResolverDescriptor rd = cachedDescriptors.get(relicId);
            relicResolver.deleteExternal(r, rd, cacheDirectory);
        } else {
            throw new RelicException("Could not delete the remote file as no reference is present in the cache.");
        }
    }

    public void clearLocal(String relicId) throws RelicException {
        if (!cachedRelics.containsKey(relicId) && !cachedDescriptors.containsKey(relicId)) {
            loadRelic(relicId, false);
        }
        if (cachedRelics.containsKey(relicId) && cachedDescriptors.containsKey(relicId)) {
            Relic r = cachedRelics.get(relicId);
            File result = new File(cacheDirectory, r.getFileName());
            if (result.exists()) {
                result.delete();
            } else {
                throw new RelicException("Could not delete the file as it is not present in the cache.");
            }
        } else {
            throw new RelicException("Could not delete the file as it is not present in the cache.");
        }
    }

    public void addRelic(Relic r, ResolverDescriptor rd) {
        cachedRelics.put(r.getIdentifier(), r);
        cachedDescriptors.put(r.getIdentifier(), rd);
    }

    public File getRelic(String relicId, boolean resolve) throws RelicException {
        if (!cachedRelics.containsKey(relicId)) {
            loadRelic(relicId, resolve);
            //importRelic(relicId);
        }
        Relic r = cachedRelics.get(relicId);
        File result = new File(cacheDirectory, r.getFileName());
        return result;
    }
}
