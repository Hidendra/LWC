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
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR,
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

package org.getlwc.forge;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import org.getlwc.Block;
import org.getlwc.World;
import org.getlwc.forge.event.EntityExplodeEvent;
import org.getlwc.forge.event.PlayerBreakBlockEvent;

import java.util.ArrayList;
import java.util.List;

public class ForgeEventHelper {

    /**
     * Called when a block is broken (harvested)
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @param block
     * @param metadata
     * @param entityPlayer
     */
    public static boolean onBlockHarvested(net.minecraft.world.World world, int x, int y, int z, net.minecraft.block.Block block, int metadata, EntityPlayer entityPlayer) {
        PlayerBreakBlockEvent event = new PlayerBreakBlockEvent(world, x, y, z, block, metadata, entityPlayer);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled() || event.getResult() == Event.Result.DENY;
    }

    /**
     * Called when an explosion occurs in the world
     *
     * @param nativeWorld
     * @param explosionX
     * @param explosionY
     * @param explosionZ
     * @param explosionRadius
     * @param affectedLocations
     * @param entity
     * @return
     */
    public static boolean onExplosion(net.minecraft.world.World nativeWorld, double explosionX, double explosionY, double explosionZ, int explosionRadius, List<net.minecraft.world.ChunkPosition> affectedLocations, net.minecraft.entity.Entity entity) {
        List<Block> affectedBlocks = new ArrayList<Block>();
        World world = LWC.instance.getWorld(nativeWorld.getWorldInfo().getWorldName());

        for (net.minecraft.world.ChunkPosition loc : affectedLocations) {
            Block block = world.getBlockAt(loc.x, loc.y, loc.z);

            // Check that it isn't air (we don't need to know about air internally)
            if (block.getType() == 0) {
                continue;
            }

            affectedBlocks.add(block);
        }

        EntityExplodeEvent event = new EntityExplodeEvent(entity, (int) explosionX, (int) explosionY, (int) explosionZ, explosionRadius, affectedBlocks);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled() || event.getResult() == Event.Result.DENY;
    }

}
