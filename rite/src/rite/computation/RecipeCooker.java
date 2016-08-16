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

import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import rite.exceptions.RiteException;
import rite.operations.Operation;
import rite.operations.Recipe;
import rite.operations.Step;
import rite.operations.implementations.GenericOperation;
import rite.operations.implementations.OperationUtilities;

/**
 * 
 * RecipeCooker
 *
 * @author vm.kattenberg
 */
public class RecipeCooker extends TimerTask {
    // TODO check concurrent acces to this class
    private static final Object lock = new Object();
    private Recipe recipe;
    private Step currentStep;
    private int stepIndex;
    private ComputeService computeService;
    private ArrayList<OperationPair> operations;

    public RecipeCooker() {
        this.computeService = new ComputeService();
        operations = new ArrayList<OperationPair>();
    }

    @Override
    public void run() {
        synchronized (lock) {
            tick();
            if (currentStep != null && currentStep.hasCompleted() && stepIndex < (recipe.size() - 1)) {
                stepIndex++;
                scheduleRecipe();
            }
        }
    }

    public void setRecipe(Recipe r) {
        if (recipe != null) {
            // Remove old recipe
            unscheduleRecipe();
        }
        this.recipe = r;
        stepIndex = 0;
        scheduleRecipe();
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public Recipe removeRecipe() {
        Recipe result = recipe;
        unscheduleRecipe();
        recipe = null;
        return result;
    }

    public void destroy() {
        computeService.destroy();
    }

    private void tick() {
        synchronized (lock) {
            if (currentStep != null) {
                for (OperationPair op : operations) {
                    updateStepResults(op);
                }
                recipe.set(stepIndex, currentStep);
            }
        }
    }

    private void scheduleRecipe() {
        synchronized (lock) {
            currentStep = recipe.get(stepIndex);
            for (Operation o : currentStep) {
                FutureTask<Operation> executingOperation = computeService.executeOperation(o);
                operations.add(new OperationPair(o, executingOperation));
            }
        }
    }

    private void unscheduleRecipe() {
        synchronized (lock) {
            for (OperationPair op : operations) {
                computeService.cancelOperation(op.getExecutingOperation());
                updateStepResults(op);
            }
            currentStep = null;
            recipe = null;
            operations = new ArrayList<OperationPair>();
        }
    }

    private void updateStepResults(OperationPair op) {
        synchronized (lock) {
            Operation o = null;
            if (op.getExecutingOperation().isDone() || op.getExecutingOperation().isCancelled()) {
                try {
                    o = op.getExecutingOperation().get();
                    if (o == null) {
                        o = op.getOperation();
                        o.setProperty(GenericOperation.PropertyKeys.ERROR, OperationUtilities.getStackTraceAsString(new RiteException("The operation retrieved from the FutureTask was null!")));
                        o.fail();
                        o.complete();
                    }
                } catch (InterruptedException e) {
                    o = op.getOperation();
                    o.setProperty(GenericOperation.PropertyKeys.ERROR, OperationUtilities.getStackTraceAsString(e));
                    o.fail();
                    o.complete();
                } catch (ExecutionException e) {
                    o = op.getOperation();
                    o.setProperty(GenericOperation.PropertyKeys.ERROR, OperationUtilities.getStackTraceAsString(e));
                    o.fail();
                    o.complete();
                }
                int index = 0;
                for (Operation so : currentStep) {
                    if (so.getIdentifier().equals(o.getIdentifier())) {
                        currentStep.set(index, o);
                    }
                    index++;
                }
            } else {
                o = op.getOperation();
                int index = 0;
                for (Operation so : currentStep) {
                    if (so.getIdentifier().equals(o.getIdentifier())) {
                        currentStep.set(index, o);
                    }
                    index++;
                }
            }
        }
    }

}