// Copyright 2012 - V.M. Kattenberg - vm.kattenberg@gmail.com
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

package io.github.vmk.rite.persistence;

import java.io.File;
import java.util.Date;

/**
 * ClientInfo
 * 
 * @author vm.kattenberg
 */
public class ClientInfo {
    // TODO perhaps recipe completed and failed status could also be added here.
    private String clientId;
    private Date clientStart;
    private String clientHost;
    private File workingDirectory;
    private File standardOutFile;
    private File standardErrorFile;
    private String recipeId;
    private Date recipeStart;
    private Date recipeEnd;
    private boolean recipeFailed;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Date getClientStart() {
        return clientStart;
    }

    public void setClientStart(Date clientStart) {
        this.clientStart = clientStart;
    }

    public String getClientHost() {
        return clientHost;
    }

    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public File getStandardOut() {
        return standardOutFile;
    }

    public void setStandardOut(File standardOut) {
        this.standardOutFile = standardOut;
    }

    public File getStandardError() {
        return standardErrorFile;
    }

    public void setStandardError(File standardError) {
        this.standardErrorFile = standardError;
    }

    public Date getRecipeStart() {
        return recipeStart;
    }

    public void setRecipeStart(Date recipeStart) {
        this.recipeStart = recipeStart;
    }

    public Date getRecipeEnd() {
        return recipeEnd;
    }

    public void setRecipeEnd(Date recipeEnd) {
        this.recipeEnd = recipeEnd;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public boolean hasRecipeFailed() {
        return recipeFailed;
    }

    public void setRecipeFailed(boolean recipeFailed) {
        this.recipeFailed = recipeFailed;
    }

}
