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

import java.util.List;

public interface EventHelper {

    /**
     * Called when an entity interacts with a block in the world
     *
     * @param entity
     * @param block
     * @return true if the event is to be cancelled, false otherwise
     */
    public boolean onBlockInteract(Entity entity, Block block);

    /**
     * Called when an entity breaks a block in the world
     *
     * @param entity
     * @param block
     * @return
     */
    public boolean onBlockBreak(Entity entity, Block block);

    /**
     * Called when an entity places a block in the world
     *
     * @param entity
     * @param block
     * @return
     */
    public boolean onBlockPlace(Entity entity, Block block);

    /**
     * Called when an entity changes text on a sign
     *
     * @param entity
     * @param block
     * @return
     */
    public boolean onSignChange(Entity entity, Block block);

    /**
     * Called when an explosion occurs in the world
     *
     * @param type
     * @param blocksAffected
     * @return
     */
    public boolean onExplosion(ExplosionType type, List<Block> blocksAffected);

    /**
     * Called when a structure grows in the world (e.g. a tree sapling)
     *
     * @param location
     * @param blocks
     * @return
     */
    public boolean onStructureGrow(Location location, List<Block> blocks);

    /**
     * Called when the redstone level changes on a block
     *
     * @param block
     * @param oldLevel
     * @param newLevel
     * @return
     */
    public boolean onRedstoneChange(Block block, int oldLevel, int newLevel);

    /**
     * Called when a move item event occurs at the given location
     *
     * @param location
     * @return
     */
    public boolean onInventoryMoveItem(Location location);


    /**
     * Called when a player clicks a slot in an inventory
     *
     * @param player
     * @param location
     * @param clicked
     * @param rightClick
     * @param shiftClick
     * @param doubleClick
     * @return
     */
    public boolean onInventoryClickItem(Player player, Location location, ItemStack clicked, ItemStack cursor, int slot, int rawSlot, boolean rightClick, boolean shiftClick, boolean doubleClick);

    /**
     * Called when a piston extends
     *
     * @param piston
     * @param extending the location the piston will push
     * @return
     */
    public boolean onPistonExtend(Block piston, Location extending);

    /**
     * Called when a piston retracts
     *
     * @param piston
     * @param retracting the location the piston will retract from (or attempt to pull if sticky)
     * @return
     */
    public boolean onPistonRetract(Block piston, Location retracting);

}
