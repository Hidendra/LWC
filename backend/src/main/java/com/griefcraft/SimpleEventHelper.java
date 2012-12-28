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

package com.griefcraft;

import com.griefcraft.entity.Player;
import com.griefcraft.event.EventException;
import com.griefcraft.event.PlayerEventHandler;
import com.griefcraft.event.events.BlockEvent;
import com.griefcraft.event.events.ProtectionEvent;
import com.griefcraft.model.Protection;

import static com.griefcraft.I18n._;

public class SimpleEventHelper implements EventHelper {

    /**
     * The {@link Engine} instance
     */
    private final Engine engine;

    public SimpleEventHelper(Engine engine) {
        this.engine = engine;
    }

    public boolean onBlockInteract(Player player, Block block) {

        boolean cancel; // If the event should be cancelled

        // Match the block to a protection
        Protection protection = engine.getProtectionManager().findProtection(block.getLocation()); // TODO :-)
        engine.getConsoleSender().sendMessage("Protection found: " + protection);

        // Give events the first stab
        try {

            if (protection == null) {
                cancel = player.callEvent(PlayerEventHandler.Type.PLAYER_INTERACT_BLOCK, new BlockEvent(block));
            } else {
                cancel = player.callEvent(PlayerEventHandler.Type.PLAYER_INTERACT_PROTECTION, new ProtectionEvent(protection));
            }

            // default event action
            if (!cancel && protection != null) {
                ProtectionAccess access = protection.getAccess(player);

                /// TODO distinguish between left / right click.

                // if they're the owner, return immediately
                if (access.ordinal() > ProtectionAccess.NONE.ordinal()) {
                    return false;
                }

                // they cannot access the protection o\
                // so send them a kind message
                if (access != ProtectionAccess.EXPLICIT_DENY) {
                    player.sendMessage(_("&4This protection is locked by a magical spell."));
                }

                return true;
            }
        } catch (EventException e) {
            // TODO {0}
            player.sendMessage(_("&cA severe error occurred while processing the event: {0}"
                    + "&cThe full stack trace has been printed out to the log file", e.getMessage()));
            e.printStackTrace();
            return true; // Better safe than sorry
        }

        return cancel;
    }

}
