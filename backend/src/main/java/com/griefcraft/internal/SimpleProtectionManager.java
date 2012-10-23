/*
 * Copyright (c) 2011, 2012, Tyler Blair
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

package com.griefcraft.internal;

import com.griefcraft.Engine;
import com.griefcraft.ProtectionAccess;
import com.griefcraft.ProtectionManager;
import com.griefcraft.ProtectionMatcher;
import com.griefcraft.ProtectionSet;
import com.griefcraft.entity.Player;
import com.griefcraft.model.Protection;
import com.griefcraft.world.Block;
import com.griefcraft.world.Location;

import static com.griefcraft.I18n._;

public class SimpleProtectionManager implements ProtectionManager {

    /**
     * The LWC engine instance
     */
    private Engine engine;

    public SimpleProtectionManager(Engine engine) {
        this.engine = engine;
    }

    public Protection findProtection(Location location) {
        ProtectionMatcher matcher = new SimpleProtectionMatcher(engine);

        // Get the block at the location
        // this will be our base block -- or reference point -- of where the protection is matched from
        Block base = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        // attempt to match the protection set
        ProtectionSet blocks = matcher.matchProtection(base);

        // return the matched protection
        return blocks.getResultant();
    }

    public Protection createProtection(String owner, Location location) {
        // TODO check arguments
        return engine.getDatabase().createProtection(owner, location);
    }

    public boolean defaultPlayerInteractAction(Protection protection, Player player) {
        ProtectionAccess access = protection.getAccess(player);

        /// TODO distinguish between left / right click.

        // if they're the owner, return immediately
        if (access.ordinal() > ProtectionAccess.NONE.ordinal()) {
            return false;
        }

        // they cannot access the protection o\
        // so send them a kind message
        player.sendMessage(_("This protection is locked by a magical spell."));
        return true;
    }

}
