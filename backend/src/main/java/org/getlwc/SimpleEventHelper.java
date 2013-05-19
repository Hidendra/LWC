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

import org.getlwc.entity.Entity;
import org.getlwc.entity.Player;
import org.getlwc.event.EventException;
import org.getlwc.event.PlayerEventHandler;
import org.getlwc.event.events.BlockEvent;
import org.getlwc.event.events.ProtectionEvent;
import org.getlwc.model.Protection;

import java.util.List;

import static org.getlwc.I18n._;

public class SimpleEventHelper implements EventHelper {

    /**
     * The {@link Engine} instance
     */
    private final Engine engine;

    public SimpleEventHelper(Engine engine) {
        this.engine = engine;
    }

    /**
     * A generic method that checks if an entity is allowed to interact with the given block.
     *
     * @param entity
     * @param block
     * @return true if the given entity can access the block (i.e. no protection there OR they can access the protection)
     */
    private boolean silentAccessCheck(Entity entity, Block block) {
        Protection protection = engine.getProtectionManager().findProtection(block.getLocation());

        if (protection == null) {
            return true;
        }

        if (entity instanceof Player) {
            Player player = (Player) entity;

            ProtectionAccess access = protection.getAccess(player);

            // if they're the owner, return immediately
            if (access.ordinal() > ProtectionAccess.NONE.ordinal()) {
                return true;
            }

            return false;
        } else {
            throw new UnsupportedOperationException("Unsupported Entity: " + entity.getClass().getSimpleName());
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean onBlockInteract(Entity entity, Block block) {
        boolean cancel = false;

        // Match the block to a protection
        Protection protection = engine.getProtectionManager().findProtection(block.getLocation()); // TODO :-)
        engine.getConsoleSender().sendMessage("Protection found: " + protection);

        if (entity instanceof Player) {
            Player player = (Player) entity;

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

                    // check if they can access the protection
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
                player.sendMessage(_("&cA severe error occurred while processing the event: {0}"
                        + "&cThe full stack trace has been printed out to the log file", e.getMessage()));
                e.printStackTrace();
                return true; // Better safe than sorry
            }
        } else {
            throw new UnsupportedOperationException("Unsupported Entity: " + entity.getClass().getSimpleName());
        }

        return cancel;
    }

    /**
     * {@inheritDoc}
     */
    public boolean onBlockBreak(Entity entity, Block block) {
        return !silentAccessCheck(entity, block);
    }

    /**
     * {@inheritDoc}
     */
    public boolean onBlockPlace(Entity entity, Block block) {
        System.out.println("[internal] Block placed @ " + block);

        // Nothing yet.
        // - blacklisting blocks
        // - match neighbouring protection (e.g. double chest). if there is one there don't do anything
        // - auto protect
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean onSignChange(Entity entity, Block block) {
        System.out.println("[internal] Sign changed @ " + block);

        return !silentAccessCheck(entity, block);
    }

    /**
     * {@inheritDoc}
     */
    public boolean onExplosion(ExplosionType type, List<Block> blocksAffected) {
        System.out.println("[internal] Explosion occurred! type=" + type + " blocks affected=" + blocksAffected.size());
        // TODO

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean onStructureGrow(Location location, List<Block> blocks) {
        System.out.println("[internal] onStructureGrow loc=" + location + " blocks.size()=" + blocks.size());

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean onRedstoneChange(Block block, int oldLevel, int newLevel) {
        ProtectionManager manager = engine.getProtectionManager();

        if (!manager.isBlockProtectable(block)) {
            return false;
        }

        Protection protection = manager.findProtection(block.getLocation());

        /**
         * TODO globally disable redstone on protections for now
         */
        if (protection != null) {
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean onInventoryMoveItem(Location location) {
        System.out.println("[internal] onInventoryMoveItem @ " + location);
        return false;
    }

}
