// Copyright 2012 - V.M. Kattenberg - vm.kattenberg@gmail.com
//
// This file is part of examples
//
// examples is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// examples is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with examples.  If not, see <http://www.gnu.org/licenses/>.

package nl.vu.psy.rite.examples;

import nl.vu.psy.relic.Relic;
import nl.vu.psy.relic.exceptions.RelicException;
import nl.vu.psy.relic.persistence.RelicStore;
import nl.vu.psy.relic.persistence.mongo.MongoStore;
import nl.vu.psy.relic.resolvers.ResolverDescriptor;
import nl.vu.psy.relic.resolvers.implementations.GridLCGResolver;
import nl.vu.psy.relic.resolvers.implementations.GridSRMResolver;
import nl.vu.psy.relic.resolvers.implementations.LocalFileSystemResolver;

/**
 * CreateRelicsExample
 * 
 * @author vm.kattenberg
 */
public class CreateRelicsExample {
    /*
     * This is a basic example on how to set up a relic store and insert some relics. Relics provide indirection for files so that identifiers can be used in recipes. These identifiers will eventually
     * map to an environment specific location and resolution for those files.
     */

    public static void main(String[] args) throws RelicException {
        // First set up a RelicStore object for connection to a persistent store. In this case a MongoDB store is used.
        RelicStore store = new MongoStore("ds031477.mongolab.com", 31477, "ritedemo", "relics", "rite", "demo");

        // Set up a relic for a file on a regular file system
        Relic r = new Relic("an_identifier");
        r.setFileName("local.file");
        r.setDescription("Some text describing the file");

        ResolverDescriptor rd = new ResolverDescriptor(r.getIdentifier(), LocalFileSystemResolver.ENVIRONMENT);
        rd.setProperty(LocalFileSystemResolver.DescriptorKeys.ABSOLUTEPATH.getKey(), "/path/to/local.file");

        // Add a second resolver for grid
        ResolverDescriptor rd2 = new ResolverDescriptor(r.getIdentifier(), GridSRMResolver.ENVIRONMENT);
        rd.setProperty(GridSRMResolver.DescriptorKeys.SRMURL.getKey(), "srm/url/to/grid.file");

        // Store the relic and resolver
        store.putRelic(r);
        store.putResolverDescriptor(rd);
        store.putResolverDescriptor(rd2);

        // Set up a relic for a file on grid storage (resolution done using dcache srm tools)
        r = new Relic("another_identifier");
        r.setFileName("grid.file");
        r.setDescription("Some text describing the file");

        rd = new ResolverDescriptor(r.getIdentifier(), GridSRMResolver.ENVIRONMENT);
        rd.setProperty(GridSRMResolver.DescriptorKeys.SRMURL.getKey(), "srm/url/to/grid.file");

        // Store the relic and resolver
        store.putRelic(r);
        store.putResolverDescriptor(rd);

        // Set up a relic for a file on grid storage (resolution done using lcg tools which are supposedly more robust)
        r = new Relic("yet_another_identifier");
        r.setFileName("grid.file2");
        r.setDescription("Some text describing the file");

        rd = new ResolverDescriptor(r.getIdentifier(), GridLCGResolver.ENVIRONMENT);
        rd.setProperty(GridLCGResolver.DescriptorKeys.SRMURL.getKey(), "srm/url/to/grid.file");

        // Store the relic and resolver
        store.putRelic(r);
        store.putResolverDescriptor(rd);

        // The relic.properties file for the rite client:
        // environment=srm (or localfs or lcg depending on where you run the client)
        // type=MongoDB
        // hostname=ds031477.mongolab.com
        // port=31477
        // dbname=ritedemo
        // auth=true
        // user=rite
        // pass=demo

    }
}
