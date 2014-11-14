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

public class SimpleProtectionMatcher implements ProtectionMatcher {

    /**
     * The LWC engine instance
     */
    private Engine engine;

    public SimpleProtectionMatcher(Engine engine) {
        this.engine = engine;
    }

    public ProtectionSet matchProtection(Block base) {
        ProtectionSet blocks = new ProtectionSet(engine);

        // first add the base block, as it must exist on the protection if it matches
        blocks.add(base);

        // Double chest
        if (base.isOneOf("minecraft:chest", "minecraft:trapped_chest")) {
            Block adjacentChest = base.findBlockRelativeToXZ(base.getName());

            if (adjacentChest != null) {
                blocks.add(adjacentChest);
            }
        }

        // Doors (not the block below the door)
        else if (base.isOneOf("minecraft:wooden_door", "minecraft:iron_door")) {
            Block otherDoor = base.findBlockRelativeToY("minecraft:wooden_door", "minecraft:iron_door");

            // add the other half of the door
            if (otherDoor != null) {
                blocks.add(otherDoor);
            }
        }

        // other
        else {
            Block above = base.getRelative(BlockFace.UP);

            // door above the current block
            if (above.isOneOf("minecraft:wooden_door", "minecraft:iron_door")) {
                blocks.add(above);
                blocks.add(above.getRelative(BlockFace.UP)); // top of the door
            }

            // lever that is attached to a block above
            else if (above.isOneOf("minecraft:lever") && ((above.getData() & 0x5) == 0x5 || (above.getData() & 0x6) == 0x6)) {
                blocks.add(above);
            }

            // gravity block (e.g. the block above would be destroyed if this one was removed)
            else if (above.isOneOf("minecraft:standing_sign")) {
                blocks.add(above);
            } else {

                // wall-attached blocks
                BlockFace[] POSSIBLE = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

                for (BlockFace face : POSSIBLE) {
                    Block block = base.getRelative(face);
                    byte direction = block.getData();

                    // wall sign
                    if (block.isOneOf("minecraft:wall_sign")) {
                        byte EAST = 0x05;
                        byte WEST = 0x04;
                        byte SOUTH = 0x03;
                        byte NORTH = 0x02;

                        if (face == BlockFace.EAST && (direction & EAST) == EAST) {
                            blocks.add(block);
                            break;
                        } else if (face == BlockFace.WEST && (direction & WEST) == WEST) {
                            blocks.add(block);
                            break;
                        } else if (face == BlockFace.SOUTH && (direction & SOUTH) == SOUTH) {
                            blocks.add(block);
                            break;
                        } else if (face == BlockFace.NORTH && (direction & NORTH) == NORTH) {
                            blocks.add(block);
                            break;
                        }
                    }

                    // lever, stone button, wood button
                    else if (block.isOneOf("minecraft:lever", "minecraft:stone_button", "minecraft:wooden_button")) {
                        byte EAST = 0x1;
                        byte WEST = 0x2;
                        byte SOUTH = 0x3;
                        byte NORTH = 0x4;

                        // x & 0x2 returns 0x2 when direction = 0x6 which happens to be for levers if it's a ground lever -.-
                        if (block.isOneOf("minecraft:lever") && ((direction & 0x5) == 0x5 || (direction & 0x6) == 0x6)) {
                            break;
                        }

                        if (face == BlockFace.EAST && (direction & EAST) == EAST) {
                            blocks.add(block);
                            break;
                        } else if (face == BlockFace.WEST && (direction & WEST) == WEST) {
                            blocks.add(block);
                            break;
                        } else if (face == BlockFace.SOUTH && (direction & SOUTH) == SOUTH) {
                            blocks.add(block);
                            break;
                        } else if (face == BlockFace.NORTH && (direction & NORTH) == NORTH) {
                            blocks.add(block);
                            break;
                        }
                    }

                    // trap door
                    else if (block.isOneOf("minecraft:trapdoor")) {
                        byte EAST = 0x2;
                        byte WEST = 0x3;
                        byte SOUTH = 0x0;
                        byte NORTH = 0x1;

                        if (face == BlockFace.WEST && (direction & EAST) == EAST) {
                            blocks.add(block);
                            break;
                        } else if (face == BlockFace.EAST && (direction & WEST) == WEST) {
                            blocks.add(block);
                            break;
                        } else if (face == BlockFace.NORTH && (direction & SOUTH) == SOUTH) {
                            blocks.add(block);
                            break;
                        } else if (face == BlockFace.SOUTH && (direction & NORTH) == NORTH) {
                            blocks.add(block);
                            break;
                        }
                    }

                }

            }

        }

        for (ProtectionSet.BlockType type : ProtectionSet.BlockType.values()) {
            for (Block block : blocks.get(type)) {
                engine.getConsoleSender().sendMessage(type.toString() + " => " + block.toString());
            }
        }

        // check for a protection and return
        blocks.checkForProtections();
        return blocks;
    }

}
