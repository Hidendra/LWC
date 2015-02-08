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
import com.griefcraft.util.SetUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.EnumSet;
import java.util.Set;

/**
 * Matches wall entities
 * TODO fix buttons and levers
 */
public class WallMatcher implements ProtectionFinder.Matcher {

    /**
     * Blocks that can be attached to the wall and be protected.
     * This assumes that the block is DESTROYED if the wall they are attached to is broken.
     */
    public static final Set<Material> PROTECTABLES_WALL = EnumSet.of(Material.WALL_SIGN);

    /**
     * Those evil levers and buttons have all different bits for directions. Gah!
     */
    public static final Set<Material> PROTECTABLES_LEVERS_ET_AL = EnumSet.of(Material.STONE_BUTTON, Material.LEVER);

    /**
     * Same as PROTECTABLE_WALL, except the facing direction is reversed,
     * such as trap doors
     */
    public static final Set<Material> PROTECTABLES_TRAP_DOORS = EnumSet.of(Material.TRAP_DOOR);

    /**
     * Possible faces around the base block that protections could be at
     */
    public static final BlockFace[] POSSIBLE_FACES = new BlockFace[]{ BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };

    static {
        SetUtil.addToSetWithoutNull(PROTECTABLES_WALL, Material.getMaterial(177)); // Wall banner
        SetUtil.addToSetWithoutNull(PROTECTABLES_TRAP_DOORS, Material.getMaterial(167)); // Iron trap door
    }

    public boolean matches(ProtectionFinder finder) {
        // The block we are working on
        Block block = finder.getBaseBlock().getBlock();

        // Match wall signs to the wall it's attached to
        for (BlockFace blockFace : POSSIBLE_FACES) {
            Block face; // the relative block

            if ((face = block.getRelative(blockFace)) != null) {
                // Try and match it
                Block matched = tryMatchBlock(face, blockFace);

                // We found something ..! Try and load the protection
                if (matched != null) {
                    finder.addBlock(matched);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Try and match a wall block
     *
     * @param block
     * @param matchingFace
     * @return
     */
    private Block tryMatchBlock(Block block, BlockFace matchingFace) {
        byte direction = block.getData();

        // Blocks such as wall signs
        if (PROTECTABLES_WALL.contains(block.getType())) {
            byte EAST = 0x05;
            byte WEST = 0x04;
            byte SOUTH = 0x03;
            byte NORTH = 0x02;

            if (matchingFace == BlockFace.EAST && (direction & EAST) == EAST) {
                return block;
            } else if (matchingFace == BlockFace.WEST && (direction & WEST) == WEST) {
                return block;
            } else if (matchingFace == BlockFace.SOUTH && (direction & SOUTH) == SOUTH) {
                return block;
            } else if (matchingFace == BlockFace.NORTH && (direction & NORTH) == NORTH) {
                return block;
            }
        }

        // Levers, buttons
        else if (PROTECTABLES_LEVERS_ET_AL.contains(block.getType())) {
            byte EAST = 0x4;
            byte WEST = 0x3;
            byte SOUTH = 0x1;
            byte NORTH = 0x2;

            if (matchingFace == BlockFace.EAST && (direction & EAST) == EAST) {
                return block;
            } else if (matchingFace == BlockFace.WEST && (direction & WEST) == WEST) {
                return block;
            } else if (matchingFace == BlockFace.SOUTH && (direction & SOUTH) == SOUTH) {
                return block;
            } else if (matchingFace == BlockFace.NORTH && (direction & NORTH) == NORTH) {
                return block;
            }
        }

        // Blocks such as trap doors
        else if (PROTECTABLES_TRAP_DOORS.contains(block.getType())) {
            byte EAST = 0x2;
            byte WEST = 0x3;
            byte SOUTH = 0x0;
            byte NORTH = 0x1;

            if (matchingFace == BlockFace.WEST && (direction & EAST) == EAST) {
                return block;
            } else if (matchingFace == BlockFace.EAST && (direction & WEST) == WEST) {
                return block;
            } else if (matchingFace == BlockFace.NORTH && (direction & SOUTH) == SOUTH) {
                return block;
            } else if (matchingFace == BlockFace.SOUTH && (direction & NORTH) == NORTH) {
                return block;
            }
        }

        return null;
    }

}
