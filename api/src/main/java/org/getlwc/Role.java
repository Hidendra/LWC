/*
 * Copyright (c) 2011-2013 Tyler Blair
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package org.getlwc;

import org.getlwc.model.AbstractSavable;
import org.getlwc.model.Protection;
import org.getlwc.model.State;

public abstract class Role extends AbstractSavable implements AccessProvider {

    /**
     * The Engine instance
     */
    private Engine engine;

    /**
     * The state this role is in
     */
    private State state = State.NEW;

    /**
     * The protection this role is for
     */
    private Protection protection;

    /**
     * The role name for the player to grant access to
     */
    private final String roleName;

    /**
     * The access to grant to players that match this role
     */
    private ProtectionAccess roleAccess;

    public Role(Engine engine, Protection protection, String roleName, ProtectionAccess roleAccess) {
        super(engine);
        this.engine = engine;
        this.protection = protection;
        this.roleName = roleName;
        this.roleAccess = roleAccess;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": roleName=\"" + roleName + "\" access=" + roleAccess.toString() + " protection=\"" + protection + "\"";
    }

    /**
     * Get the protection that this role is for
     *
     * @return
     */
    public Protection getProtection() {
        return protection;
    }

    /**
     * Get the name of the role this defines, e.g a player's name for a PlayerRole
     *
     * @return
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Get the {@link ProtectionAccess} this role can provide
     *
     * @return
     */
    public ProtectionAccess getRoleAccess() {
        return roleAccess;
    }

    /**
     * Set the access for the protection
     *
     * @param access
     */
    public void setProtectionAccess(ProtectionAccess access) {
        this.roleAccess = access;
    }

    /**
     * Get the state this role is in
     *
     * @return
     */
    public State getState() {
        return state;
    }

    /**
     * Change the state this role is in
     *
     * @param state
     */
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public void saveImmediately() {
        // this will update or create the role depending on the current state
        engine.getDatabase().saveOrCreateRole(this);
        state = State.UNMODIFIED;
    }

    @Override
    public boolean isSaveNeeded() {
        return state == State.MODIFIED || state == State.NEW;
    }

    @Override
    public void remove() {
        engine.getDatabase().removeRole(this);
        state = State.REMOVED;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Role o = (Role) object;
        return getType() == o.getType() && roleName.equals(o.roleName) && roleAccess == o.roleAccess && state == o.state;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash *= 17 + getType();
        hash *= 17 + roleName.hashCode();
        return hash;
    }

    /**
     * Get a unique integer that will be used to represent this role
     *
     * @return
     */
    public abstract int getType();

}
