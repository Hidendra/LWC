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

import java.util.EnumSet;

public abstract class ProtectionRole extends AbstractSavable implements AccessProvider {

    /**
     * Access levels for protections. ordinal values are used here meaning they must remain in a constant order. As well,
     * the enum values are ranked in power of ascending order meaning Access(4) has more power than
     * Access(1) will. This also implies that the initial implementation is complete and that adding
     * any more access levels would be a pain.
     * <p/>
     * As well, the only exception to these rules is EXPLICIT_DENY which will immediately deny access to the
     * protection. This will not always be used but may be useful in some cases.
     */
    public static enum Access {

        /**
         * Immediately reject access to the protection.
         */
        EXPLICIT_DENY,

        /**
         * User has NO access to the protection
         */
        NONE,

        /**
         * The user can view the protection but not modify it in any way. The implementation of this depends
         * on the mod and if the mod does not support preventing the inventory from being modified somehow
         * then access will just be blocked.
         */
        GUEST,

        /**
         * The user can only deposit into the protection
         */
        DEPOSITONLY,

        /**
         * User can open and access the protection but not add or remove access from the protection
         */
        MEMBER,

        /**
         * User can modify the protection (add and remove members) but not add or remove other managers.
         */
        MANAGER,

        /**
         * User has the same access as the user who created the protection. They can remove the protection,
         * add or remove ANY level to the protection (i.e other owners) but they cannot remove themself.
         */
        OWNER;

        /**
         * Access levels that normal players can set
         */
        public final static EnumSet<Access> USABLE_ACCESS_LEVELS = EnumSet.range(NONE, OWNER);

        /**
         * Match a {@link org.getlwc.ProtectionRole.Access} given a name.
         *
         * @param name
         * @return NULL if no {@link org.getlwc.ProtectionRole.Access} is matched
         */
        public static Access match(String name) {
            for (Access access : Access.values()) {
                if (access.toString().equalsIgnoreCase(name)) {
                    return access;
                }
            }

            return null;
        }

    }

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
    private final String name;

    /**
     * The access to grant to players that match this role
     */
    private Access access;

    public ProtectionRole(Engine engine, Protection protection, String roleName, Access roleAccess) {
        super(engine);
        this.engine = engine;
        this.protection = protection;
        this.name = roleName;
        this.access = roleAccess;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": name=\"" + name + "\" access=" + access.toString() + " protection=\"" + protection + "\"";
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
    public String getName() {
        return name;
    }

    /**
     * Get the {@link org.getlwc.ProtectionRole.Access} this role can provide
     *
     * @return
     */
    public Access getAccess() {
        return access;
    }

    /**
     * Set the access for the protection
     *
     * @param access
     */
    public void setProtectionAccess(Access access) {
        this.access = access;
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

        ProtectionRole o = (ProtectionRole) object;
        return getType() == o.getType() && name.equals(o.name) && access == o.access && state == o.state;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash *= 17 + getType();
        hash *= 17 + name.hashCode();
        return hash;
    }

    /**
     * Get a unique integer that will be used to represent this role
     *
     * @return
     */
    public abstract int getType();

}
