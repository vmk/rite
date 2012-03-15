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

import nl.vu.psy.rite.exceptions.RiteException;
import nl.vu.psy.rite.operations.Recipe;
import nl.vu.psy.rite.operations.Step;
import nl.vu.psy.rite.operations.implementations.examples.ExampleCommandlineOperation;
import nl.vu.psy.rite.persistence.RecipeStore;
import nl.vu.psy.rite.persistence.mongo.MongoRecipeStore;

/**
 * CreateRecipesExample
 * 
 * @author vm.kattenberg
 */
public class CreateRecipesExample {
    private static final int NUMMSGS = 5;

    /*
     * This is a basic example on how to set up the recipes describing the work you want to do
     */
    public static void main(String[] args) throws RiteException {
        // First set up a RecipeStore object for connection to a persistent store. In this case a MongoDB store is used.
        RecipeStore store = new MongoRecipeStore("ds031477.mongolab.com", 31477, "ritedemo", "recipes", "rite", "demo");
        for (int i = 0; i < (NUMMSGS - 1); i++) {
            Recipe r = new Recipe("Hello_" + i);
            Step s = new Step("say_hello");
            ExampleCommandlineOperation op = new ExampleCommandlineOperation();
            op.setProperty(ExampleCommandlineOperation.PropertyKeys.ITERATIONS.getKey(), "1");
            op.setProperty(ExampleCommandlineOperation.PropertyKeys.MESSAGE.getKey(), "Hello!");
            op.setProperty(ExampleCommandlineOperation.PropertyKeys.DELAY.getKey(), "0");
            s.add(op);
            r.add(s);
            store.putRecipe(r);
        }
        Recipe r = new Recipe("Goodbye");
        Step s = new Step("say_goodbye");
        ExampleCommandlineOperation op = new ExampleCommandlineOperation();
        op.setProperty(ExampleCommandlineOperation.PropertyKeys.ITERATIONS.getKey(), "1");
        op.setProperty(ExampleCommandlineOperation.PropertyKeys.MESSAGE.getKey(), "Hello!");
        op.setProperty(ExampleCommandlineOperation.PropertyKeys.DELAY.getKey(), "0");
        s.add(op);
        r.add(s);
        store.putRecipe(r);
        
        // The host.properties file for the rite client:
        // id=someid
        // type=MongoDB
        // hostname=ds031477.mongolab.com
        // port=31477
        // dbname=ritedemo
        // auth=true
        // user=rite
        // pass=demo
    }

}
