// Copyright 2011 - V.M. Kattenberg - vm.kattenberg@gmail.com
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

package nl.vu.psy.relic.resolvers.implementations;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

/**
 * GridSRMResolverPipeThread
 * 
 * @author vm.kattenberg
 */
public class GridLCGResolverPipeThread implements Runnable {
    private OutputStream os;
    private InputStream is;
    private boolean closeAfterCopy;

    public GridLCGResolverPipeThread(InputStream is, OutputStream os, boolean closeAfterCopy) {
        this.is = new BufferedInputStream(is);
        this.os = new BufferedOutputStream(os);
        this.closeAfterCopy = closeAfterCopy;
    }

    public void run() {
        try {
            IOUtils.copy(is, os);
            os.flush();
            if (closeAfterCopy) {
                os.close();
                is.close();
            }
        } catch (IOException e) {
            // Absorb TODO find out why streams can be closed in grid environment
            // e.printStackTrace();
        }
    }
}
