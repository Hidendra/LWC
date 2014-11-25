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

import java.util.HashSet;
import java.util.Set;

public class SimpleProtectionMatcher implements ProtectionMatcher {

    @Override
    public Set<Block> matchBlocks(Block base) {
        Set<Block> locations = new HashSet<>();

        // first add the base block, as it must exist on the protection if it matches
        locations.add(base);

        // Double chest
        if (base.isOneOf("minecraft:chest", "minecraft:trapped_chest")) {
            Block adjacentChest = base.findBlockRelativeToXZ(base.getName());

            if (adjacentChest != null) {
                locations.add(adjacentChest);
            }
        }

        // Doors (not the block below the door)
        else if (base.isOneOf("minecraft:wooden_door", "minecraft:iron_door")) {
            Block otherDoor = base.findBlockRelativeToY("minecraft:wooden_door", "minecraft:iron_door");

            // add the other half of the door
            if (otherDoor != null) {
                locations.add(otherDoor);

                // and the block below it
                Block bottomHalf = base.getY() < otherDoor.getY() ? base : otherDoor;

                locations.add(bottomHalf.getRelative(BlockFace.DOWN));
            }
        }

        // Add the block below the sign as destroying it destroys the sign
        else if (base.isOneOf("minecraft:standing_sign")) {
            locations.add(base.getRelative(BlockFace.DOWN));
        }

        // Add the block wall signs are attached to
        else if (base.isOneOf("minecraft:wall_sign")) {
            byte direction = base.getData();

            byte EAST = 0x5;
            byte WEST = 0x4;
            byte SOUTH = 0x3;
            byte NORTH = 0x2;

            if ((direction & EAST) == EAST) {
                locations.add(base.getRelative(BlockFace.WEST));
            } else if ((direction & WEST) == WEST) {
                locations.add(base.getRelative(BlockFace.EAST));
            } else if ((direction & SOUTH) == SOUTH) {
                locations.add(base.getRelative(BlockFace.NORTH));
            } else if ((direction & NORTH) == NORTH) {
                locations.add(base.getRelative(BlockFace.SOUTH));
            }
        }

        // Add block buttons/levers are attached to
        else if (base.isOneOf("minecraft:lever", "minecraft:stone_button", "minecraft:wooden_button")) {
            byte direction = base.getData();

            byte EAST = 0x1;
            byte WEST = 0x2;
            byte SOUTH = 0x3;
            byte NORTH = 0x4;

            // ground lever
            if (base.isOneOf("minecraft:lever") && ((direction & 0x5) == 0x5 || (direction & 0x6) == 0x6)) {
                locations.add(base.getRelative(BlockFace.DOWN));
            } else { // otherwise it's something on the wall
                if ((direction & EAST) == EAST) {
                    locations.add(base.getRelative(BlockFace.WEST));
                } else if ((direction & WEST) == WEST) {
                    locations.add(base.getRelative(BlockFace.EAST));
                } else if ((direction & SOUTH) == SOUTH) {
                    locations.add(base.getRelative(BlockFace.NORTH));
                } else if ((direction & NORTH) == NORTH) {
                    locations.add(base.getRelative(BlockFace.SOUTH));
                }
            }
        }

        // Add block trapdoors are attached to
        else if (base.isOneOf("minecraft:trapdoor")) {
            byte direction = base.getData();

            byte EAST = 0x2;
            byte WEST = 0x3;
            byte SOUTH = 0x0;
            byte NORTH = 0x1;

            if ((direction & EAST) == EAST) {
                locations.add(base.getRelative(BlockFace.EAST));
            } else if ((direction & WEST) == WEST) {
                locations.add(base.getRelative(BlockFace.WEST));
            } else if ((direction & SOUTH) == SOUTH) {
                locations.add(base.getRelative(BlockFace.SOUTH));
            } else if ((direction & NORTH) == NORTH) {
                locations.add(base.getRelative(BlockFace.NORTH));
            }
        }

        return locations;
    }

}
