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

package com.griefcraft.event;

import com.griefcraft.Engine;
import com.griefcraft.model.Protection;
import com.griefcraft.event.events.BlockEvent;
import com.griefcraft.event.events.ProtectionEvent;
import com.griefcraft.entity.Player;
import com.griefcraft.world.Block;

import static com.griefcraft.I18n._;

/**
 * Used to provide handling for any event in one central location. This takes the burden of having to deal
 * with event specific code in each server's plugin version, so we can then focus on implementing the bare
 * minimum required on the server side.
 */
public final class PlayerEventDelegate {

    /**
     * The LWC engine
     */
    private final Engine engine;

    /**
     * The player this delegate sends requests to
     */
    private final Player player;

    public PlayerEventDelegate(Engine engine, Player player) {
        this.engine = engine;
        this.player = player;
    }

    /**
     * Called when the player interacts with a block. Returns TRUE to cancel this event.
     *
     * @param block
     * @return true to cancel the event
     */
    public boolean onPlayerInteract(Block block) {
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
                cancel = engine.getProtectionManager().defaultPlayerInteractAction(protection, player);
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
