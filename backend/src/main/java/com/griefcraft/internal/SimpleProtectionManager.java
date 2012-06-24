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

import com.griefcraft.Game;
import com.griefcraft.LWC;
import com.griefcraft.ProtectionManager;
import com.griefcraft.ProtectionMatcher;
import com.griefcraft.ProtectionSet;
import com.griefcraft.model.Protection;
import com.griefcraft.world.Block;
import com.griefcraft.world.Location;

public class SimpleProtectionManager implements ProtectionManager {

    /**
     * The LWC instance
     */
    private LWC lwc;

    public SimpleProtectionManager(LWC lwc) {
        this.lwc = lwc;
    }

    public Protection findProtection(Location location) {
        ProtectionMatcher matcher;

        // select the matcher depending on the game
        Game game = lwc.getServerInfo().getServerMod().game();
        if (game == Game.MINECRAFT) {
            matcher = new MinecraftProtectionMatcher(lwc);
        } else if (game == Game.TERRARIA) {
            matcher = new TerrariaProtectionMatcher(lwc);
        } else {
            throw new UnsupportedOperationException("Game \"" + game + "\" is not implemented");
        }

        // Get the block at the location
        // this will be our base block -- or reference point -- of where the protection is matched from
        Block base = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        // attempt to match the protection set
        ProtectionSet blocks = matcher.matchProtection(base);

        // go through each of the protectable blocks
        for (Block block : blocks.get(ProtectionSet.BlockType.PROTECTABLE)) {
            // load it from the database
            Protection protection = lwc.getDatabase().loadProtection(block.getLocation());

            if (protection != null) {
                // match !
                // TODO hand the protection set over to the protection or something?
                blocks.matchedResultant(protection);
                return protection;
            }
        }

        // TODO magical things
        return null;
    }

    public Protection createProtection(Protection.Type type, String owner, Location location) {
        // TODO check arguments
        return lwc.getDatabase().createProtection(type, owner, location);
    }
}
