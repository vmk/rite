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

package io.github.vmk.rite.relic.resolvers;

import io.github.vmk.rite.relic.exceptions.RelicException;
import io.github.vmk.rite.relic.resolvers.implementations.GridLCGResolver;
import io.github.vmk.rite.relic.resolvers.implementations.GridSRMResolver;
import io.github.vmk.rite.relic.resolvers.implementations.LocalFileSystemResolver;
import io.github.vmk.rite.relic.resolvers.implementations.MinioResolver;

/**
 * RelicResolverFactory
 * 
 * @author vm.kattenberg
 */
public class RelicResolverFactory {
    private static RelicResolverFactory instance;

    public static RelicResolverFactory getInstance() {
        if (instance == null) {
            instance = new RelicResolverFactory();
        }
        return instance;
    }

    public RelicResolver getResolverForEnvironment(String environment) throws RelicException {
        if (LocalFileSystemResolver.ENVIRONMENT.equals(environment)) {
            return new LocalFileSystemResolver();
        } else if (GridSRMResolver.ENVIRONMENT.equals(environment)) {
            return new GridSRMResolver();
        } else if (GridLCGResolver.ENVIRONMENT.equals(environment)) {
            return new GridLCGResolver();
        } else if (MinioResolver.ENVIRONMENT.equals(environment)) {
            return new MinioResolver();
        } else {
            throw new RelicException("No resolver could be found for environment: [" + environment + "].");
        }
    }

}
