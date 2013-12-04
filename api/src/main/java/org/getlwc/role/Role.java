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

package org.getlwc.role;

import org.getlwc.Engine;
import org.getlwc.entity.Player;
import org.getlwc.model.AbstractSavable;

public abstract class Role extends AbstractSavable {

    /**
     * The Engine instance
     */
    protected Engine engine;

    /**
     * The role name for the player to grant access to
     */
    private String name;

    public Role(Engine engine, String roleName) {
        super(engine);
        this.engine = engine;
        this.name = roleName;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        ProtectionRole o = (ProtectionRole) object;
        return getType().equalsIgnoreCase(o.getType()) && getName().equals(o.getName());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": name=\"" + name + "\"";
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
     * Set the name of the role
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash *= 17 + getType().hashCode();
        hash *= 17 + name.hashCode();
        return hash;
    }

    /**
     * Get a unique string that will be used to represent this role
     *
     * @return
     */
    public abstract String getType();

    /**
     * Check if a player is included within this role
     *
     * @param player
     * @return
     */
    public abstract boolean included(Player player);

}
