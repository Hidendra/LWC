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

import com.griefcraft.lwc.LWC;
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

    public static final Set<Material> PROTECTABLES_DOORS = EnumSet.of(Material.WOODEN_DOOR, Material.IRON_DOOR_BLOCK);
    public static final Set<Material> PRESSURE_PLATES = EnumSet.of(Material.STONE_PLATE, Material.WOOD_PLATE);

    private static final BlockFace[] faces = new BlockFace[] {
            BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH
    };

    public boolean matches(ProtectionFinder finder) {
        Block block = finder.getBaseBlock();

        // Get the block above the base block
        Block aboveBaseBlock = block.getRelative(BlockFace.UP);

        // Get the block above the block above the base block
        Block aboveAboveBaseBlock = aboveBaseBlock.getRelative(BlockFace.UP);

        // look for door if they're clicking a pressure plate
        if (PRESSURE_PLATES.contains(block.getType()) || PRESSURE_PLATES.contains(aboveBaseBlock.getType())) {
            Block pressurePlate = PRESSURE_PLATES.contains(block.getType()) ? block : aboveBaseBlock;

            for (BlockFace face : faces) {
                Block relative = pressurePlate.getRelative(face);

                // only check if it's a door
                if (!PROTECTABLES_DOORS.contains(relative.getType())) {
                    continue;
                }

                // create a protection finder
                ProtectionFinder doorFinder = new ProtectionFinder(LWC.getInstance());

                // attempt to match the door
                if (doorFinder.matchBlocks(relative)) {
                    // add the blocks it matched
                    for (Block found : doorFinder.getBlocks()) {
                        finder.addBlock(found);
                    }

                    // add the pressure plate
                    finder.addBlock(pressurePlate);
                    return true;
                }
            }
        }

        // Match the block UNDER the door
        if(PROTECTABLES_DOORS.contains(aboveAboveBaseBlock.getType()) && PROTECTABLES_DOORS.contains(aboveBaseBlock.getType())) {
            finder.addBlock(aboveAboveBaseBlock);
            finder.addBlock(aboveBaseBlock);
            findPressurePlate(finder, aboveBaseBlock);
            return true;
        }

        // Match the bottom half of the door
        else if (PROTECTABLES_DOORS.contains(aboveBaseBlock.getType())) {
            finder.addBlock(aboveBaseBlock);
            findPressurePlate(finder, block);
            return true;
        }

        // Match the top half of the door
        else if (PROTECTABLES_DOORS.contains(block.getType())) {
            Block bottomHalf = block.getRelative(BlockFace.DOWN);

            finder.addBlock(bottomHalf);
            findPressurePlate(finder, bottomHalf);
            return true;
        }

        return false;
    }

    /**
     * Found a pressure plate that is around a specific block
     *
     * @param finder
     * @param block
     */
    private void findPressurePlate(ProtectionFinder finder, Block block) {
        for (BlockFace face : faces) {
            Block relative = block.getRelative(face);

            if (PRESSURE_PLATES.contains(relative.getType())) {
                finder.addBlock(relative);
            }
        }
    }

}
