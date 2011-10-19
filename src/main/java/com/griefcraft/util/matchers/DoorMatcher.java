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

package com.griefcraft.util.matchers;

import com.griefcraft.util.ProtectionFinder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.EnumSet;
import java.util.Set;

/**
 * Matches doors (both Iron & Wooden)
 */
public class DoorMatcher implements ProtectionFinder.Matcher {

    private static final Set<Material> PROTECTABLES_DOORS = EnumSet.of(Material.WOODEN_DOOR, Material.IRON_DOOR_BLOCK);

    public boolean matches(ProtectionFinder finder) {
        Block block = finder.getBaseBlock();

        // Get the block above the base block
        Block aboveBaseBlock = block.getRelative(BlockFace.UP);

        // Get the block above the block above the base block
        Block aboveAboveBaseBlock = aboveBaseBlock.getRelative(BlockFace.UP);

        // Match the block UNDER the door
        if(PROTECTABLES_DOORS.contains(aboveAboveBaseBlock.getType()) && PROTECTABLES_DOORS.contains(aboveBaseBlock.getType())) {
            finder.addBlock(aboveAboveBaseBlock);
            finder.addBlock(aboveBaseBlock);
        }

        // Match the bottom half of the door
        else if (PROTECTABLES_DOORS.contains(aboveBaseBlock.getType())) {
            finder.addBlock(aboveBaseBlock);
        }

        // Match the top half of the door
        else if (PROTECTABLES_DOORS.contains(block.getType())) {
            finder.addBlock(block.getRelative(BlockFace.DOWN));
        }

        // Attempt to match the door
        return finder.loadProtection() != null;
    }

}
