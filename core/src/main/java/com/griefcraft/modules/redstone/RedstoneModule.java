/*
 * Copyright 2011 Tyler Blair. All rights reserved.
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

package com.griefcraft.modules.redstone;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Flag;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCRedstoneEvent;
import com.griefcraft.util.ProtectionFinder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

public class RedstoneModule extends JavaModule {

    @Override
    public void onRedstone(LWCRedstoneEvent event) {
        if (event.isCancelled()) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();

        // check for a player using it
        ProtectionFinder finder = protection.getProtectionFinder();

        if (finder != null) {
            for (BlockState found : finder.getBlocks()) {
                if (found.getType() == Material.STONE_PLATE || found.getType() == Material.WOOD_PLATE) {
                    // find a player that is using it
                    int x = found.getX();
                    int y = found.getY();
                    int z = found.getZ();
                    Player player = lwc.findPlayer(x - 1, x + 1, y, y + 1, z - 1, z + 1);

                    if (player != null) {
                        if (!lwc.canAccessProtection(player, protection)) {
                            event.setCancelled(true);
                        } else {
                            // bypass the denyRedstone/REDSTONE flag check
                            return;
                        }
                    }
                }
            }
        }

        boolean hasFlag = protection.hasFlag(Flag.Type.REDSTONE);
        boolean denyRedstone = lwc.getConfiguration().getBoolean("protections.denyRedstone", false);

        if ((!hasFlag && denyRedstone) || (hasFlag && !denyRedstone)) {
            event.setCancelled(true);
        }
    }

}
