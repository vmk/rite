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

package nl.vu.psy.rite.persistence;

/**
 * ClientCommand
 * 
 * @author vm.kattenberg
 */
public class ClientCommand {
    public enum Commands {
        HALT, IDLE, RUN;
    }

    private String clientId;
    private Commands command;

    public ClientCommand(String clientId, Commands command) {
        this.clientId = clientId;
        this.command = command;
    }

    public Commands getCommand() {
        return command;
    }

    public String getClientId() {
        return clientId;
    }

    public static ClientCommand idleCommand(String clientId) {
        return new ClientCommand(clientId, Commands.IDLE);
    }

    public static ClientCommand runCommand(String clientId) {
        return new ClientCommand(clientId, Commands.RUN);
    }

    public static ClientCommand haltCommand(String clientId) {
        return new ClientCommand(clientId, Commands.HALT);
    }

    public static Commands commandsFromString(String s) {
        if (Commands.HALT.toString().equals(s)) {
            return Commands.HALT;
        } else if (Commands.IDLE.toString().equals(s)) {
            return Commands.IDLE;
        } else if (Commands.RUN.toString().equals(s)) {
            return Commands.RUN;
        } else {
            return null;
        }
    }

}
