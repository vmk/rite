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

package rite.persistence;

/**
 * ClientInfoStore
 * 
 * @author vm.kattenberg
 */
public interface ClientInfoStore {
    public abstract boolean insertClientInfo(ClientInfo info, boolean listWorkingDir, boolean outputFiles);
    
    public abstract String[] getClientInfo(String clientId);
    
    public abstract String[] getClientInfoForRecipe(String recipeId);

    public abstract void deleteClientInfo(String clientId, String recipeId);

    public abstract void deleteAllClientInfo(String clientId);

}
