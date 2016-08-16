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

package rite.computation;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import rite.operations.Operation;
import rite.operations.implementations.GenericOperation;

/**
 * 
 * ComputeService
 * 
 * @author vm.kattenberg
 */
public class ComputeService {
    private ExecutorService threadPool;

    public ComputeService() {
        threadPool = Executors.newCachedThreadPool();
    }

    @SuppressWarnings("unchecked")
    public FutureTask<Operation> executeOperation(Operation o) {
        if (o.getProperty(GenericOperation.PropertyKeys.CLASS) != null || o.getProperty(GenericOperation.PropertyKeys.CLASS) != "") {
            Operation ro = null;
            Class<?> c;
            try {
                c = Class.forName(o.getProperty(GenericOperation.PropertyKeys.CLASS));
                ro = (Operation) c.newInstance();
            } catch (ClassNotFoundException e) {
                return null;
            } catch (InstantiationException e) {
                return null;
            } catch (IllegalAccessException e) {
                return null;
            }
            for (String key : o.getCurrentOperationPropertyKeys()) {
                ro.setProperty(key, o.getProperty(key));
            }
            FutureTask<Operation> ft = new FutureTask<Operation>((Callable<Operation>) ro);
            threadPool.execute(ft);
            return ft;
        } else {
            return null;
        }
    }

    public void cancelOperation(FutureTask<Operation> oft) {
        if (!oft.isDone()) {
            oft.cancel(true);
        }
    }

    public void destroy() {
        threadPool.shutdownNow();
    }

}
