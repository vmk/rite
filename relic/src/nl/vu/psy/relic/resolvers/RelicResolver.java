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

package nl.vu.psy.relic.resolvers;

import java.io.File;

import nl.vu.psy.relic.Relic;
import nl.vu.psy.relic.exceptions.RelicException;

/**
 * RelicResolver
 * 
 * @author vm.kattenberg
 */
public interface RelicResolver {
    // TODO alternatively booleans could be returned instead of throwing exceptions

    public abstract void resolveInternal(Relic r, ResolverDescriptor rd, File cacheDirectory) throws RelicException;

    public abstract void resolveExternal(Relic r, ResolverDescriptor rd, File cacheDirectory) throws RelicException;
    
    public abstract void deleteExternal(Relic r, ResolverDescriptor rd, File cacheDirectory) throws RelicException;
    
    public abstract void deleteInternal(Relic r, ResolverDescriptor rd, File cacheDirectory) throws RelicException;

}
