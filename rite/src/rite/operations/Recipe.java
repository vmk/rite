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

package rite.operations;

import java.util.ArrayList;
import java.util.Date;

/**
 * 
 * Recipe
 *
 * @author vm.kattenberg
 */
public class Recipe extends ArrayList<Step> {
    private static final long serialVersionUID = -5948473744125900991L;
    private String identifier;
    private String clientId;
    private boolean completed = false;
    private boolean failed = false;
    private boolean resetOnTimeout = false;
    private boolean resetOnFailure = false;
    private Date timeStamp;
    private long timeout = -1;

    public Recipe(String identifier) {
        super();
        setIdentifier(identifier);
        setCompleted(false);
        setTimeStamp(new Date());
    }

    public Recipe(Recipe r) {
        // Copy-constructor
        super();
        setClientId(r.getClientId());
        setIdentifier(r.getIdentifier());
        setCompleted(r.hasCompleted());
        setTimeStamp(r.getTimeStamp());
        setTimeout(r.getTimeout());
        setFailed(r.hasFailed());
        setResetOnFailure(r.resetOnFailure());
        setResetOnTimeout(r.resetOnTimeout());
        for (Step s : r) {
            this.add(s);
        }
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean hasCompleted() {
        if (!completed) {
            boolean result = true;
            for (Step s : this) {
                result &= s.hasCompleted();
            }
            completed = result;
        }
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean hasFailed() {
        if (!failed) {
            boolean result = false;
            for (Step s : this) {
                result |= s.hasFailed();
            }
            failed = result;
        }
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void reset() {
        setClientId(null);
        setTimeStamp(new Date());
        for (Step s : this) {
            for (Operation o : s) {
                o.reset();
            }
        }
        completed = false;
        failed = false;
    }

    public boolean resetOnTimeout() {
        return resetOnTimeout;
    }

    public void setResetOnTimeout(boolean resetOnTimeout) {
        this.resetOnTimeout = resetOnTimeout;
    }

    public boolean resetOnFailure() {
        return resetOnFailure;
    }

    public void setResetOnFailure(boolean resetOnFailure) {
        this.resetOnFailure = resetOnFailure;
    }

}