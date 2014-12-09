/**
 * Copyright (c) 2011-2014 Tyler Blair
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

import org.getlwc.entity.Player;
import org.getlwc.model.Protection;

public abstract class AbstractRole implements Role {

    /**
     * The access the role has
     */
    private Protection.Access access;

    /**
     * If the access has changed
     */
    private boolean accessChanged;

    /**
     * Checks if a player is inside this role
     *
     * @param player
     * @return
     */
    public abstract boolean included(Player player);

    @Override
    public Protection.Access getAccess(Protection protection, Player player) {
        if (included(player)) {
            return access;
        } else {
            return Protection.Access.NONE;
        }
    }

    @Override
    public Protection.Access getAccess() {
        return access;
    }

    @Override
    public void setAccess(Protection.Access access) {
        this.access = access;
        accessChanged = true;
    }

    @Override
    public boolean accessChanged() {
        return accessChanged;
    }

    @Override
    public void markUnchanged() {
        accessChanged = false;
    }

}
