/**
 * Copyright (c) 2011-2014 Tyler Blair
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

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public abstract class Block {

    /**
     * Gets the block's type
     *
     * @return Block type.
     */
    public abstract BlockType getType();

    /**
     * Gets the block's data
     *
     * @return
     */
    public abstract byte getData();

    /**
     * Get the world this block is located in
     *
     * @return
     */
    public abstract World getWorld();

    /**
     * Gets the block's x coordinate
     *
     * @return
     */
    public abstract int getX();

    /**
     * Gets the block's y coordinate
     *
     * @return
     */
    public abstract int getY();

    /**
     * Gets the block's z coordinate
     *
     * @return
     */
    public abstract int getZ();

    /**
     * Set the block's type
     *
     * @param type
     */
    public abstract void setType(BlockType type);

    /**
     * Set the block's data
     *
     * @param data
     */
    public abstract void setData(byte data);

    /**
     * Check if the block has a tile entity. By default, it will just match
     * blocks that are known to have tile entities but it is recommended to
     * implement this in servers that can check if a block has a tile entity.
     *
     * @return
     */
    public boolean hasTileEntity() {
        switch (getType().getId()) {
            case "minecraft:standing_sign":
            case "minecraft:wall_sign":
            case "minecraft:chest":
            case "minecraft:trapped_chest":
            case "minecraft:furnace":
            case "minecraft:lit_furnace":
            case "minecraft:dispenser":
            case "minecraft:dropper":
            case "minecraft:hopper":
            case "minecraft:mob_spawner":
            case "minecraft:note_block":
            case "minecraft:jukebox":
            case "minecraft:brewing_stand":
            case "minecraft:skull":
            case "minecraft:command_block":
            case "minecraft:beacon":
                return true;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return String.format("Block(name=%s data=%d loc=[%d %d %d \"%s\"])", getName(), getData(), getX(), getY(), getZ(), getWorld().getName());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Block)) return false;
        Block other = (Block) o;

        return this.getX() == other.getX() && this.getY() == other.getY() && this.getZ() == other.getZ() && this.getWorld().equals(other.getWorld());
    }

    @Override
    public int hashCode() {
        return this.getY() << 24 ^ this.getX() ^ this.getZ() ^ this.getWorld().hashCode();
    }

    /**
     * Get the block's name.
     *
     * @return the block's name. If the block is unknown, "unknown" is returned
     */
    @Deprecated
    public String getName() {
        BlockType type = getType();
        return type == null ? "unknown" : type.getName();
    }

    /**
     * Get the block's current location
     *
     * @return
     */
    public Location getLocation() {
        return new Location(getWorld(), getX(), getY(), getZ());
    }

    /**
     * Check if the block matches one of the provided names
     *
     * @param names
     * @return
     */
    public boolean isOneOf(String... names) {
        for (String name : names) {
            if (getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get a block relative to this block
     *
     * @param face
     * @return
     */
    public Block getRelative(BlockFace face) {
        return getRelative(face.getDeltaX(), face.getDeltaY(), face.getDeltaZ());
    }

    /**
     * Get a block relative to this block
     *
     * @param deltaX
     * @param deltaY
     * @param deltaZ
     * @return
     */
    public Block getRelative(int deltaX, int deltaY, int deltaZ) {
        return getWorld().getBlockAt(getX() + deltaX, getY() + deltaY, getZ() + deltaZ);
    }

    /**
     * Finds a block relative to the block with one of the given names. This does not look UP or DOWN and only looks
     * on the current plane for the block (that is, the current y-level)
     *
     * @param names
     * @return the Block found. If it was not found, NULL will be returned
     */
    public Block findBlockRelativeToXZ(String... names) {
        return findBlockRelative(EnumSet.of(BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH), names);
    }

    /**
     * Finds a block relative to the block with one of the given name. This does not on the current plane
     * but looks on the planes directly above and below the block (i.e the block above and below this block)
     *
     * @param names
     * @return the Block found. If it was not found, NULL will be returned
     */
    public Block findBlockRelativeToY(String... names) {
        return findBlockRelative(EnumSet.of(BlockFace.UP, BlockFace.DOWN), names);
    }

    /**
     * Finds a block relative to this block in the given direction
     *
     * @param faces
     * @param names
     * @return
     */
    private Block findBlockRelative(EnumSet<BlockFace> faces, String... names) {
        Block block;

        Set<String> typeSet = new HashSet<>();
        Collections.addAll(typeSet, names);

        for (BlockFace face : faces) {
            if ((block = getRelative(face)) != null) {
                if (typeSet.contains(block.getName())) {
                    return block;
                }
            }
        }

        return null;
    }

}
