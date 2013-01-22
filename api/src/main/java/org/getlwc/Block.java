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

import org.getlwc.util.ItemConfig;

import java.util.HashSet;
import java.util.Set;

public abstract class Block {

    /**
     * Gets the block's type
     *
     * @return
     */
    public abstract int getType();

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
    public abstract void setType(int type);

    /**
     * Set the block's data
     *
     * @param data
     */
    public abstract void setData(byte data);

    @Override
    public String toString() {
        return String.format("Block(type=%d data=%d loc=[%d %d %d \"%s\"])", getType(), getData(), getX(), getY(), getZ(), getWorld().getName());
    }

    /**
     * Get the block's name.
     *
     * @return the block's name. If the block is unknown, "unknown" is returned
     */
    public String getName() {
        String name = ItemConfig.getName(getType());
        return name == null ? "unknown" : name;
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
     * Check if the block matches one of the provided type ids
     *
     * @param ids
     * @return
     */
    public boolean typeMatchesOneOf(int... ids) {
        for (int id : ids) {
            if (id == getType()) {
                return true;
            }
        }

        return false;
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
     * Finds a block relative to the block with the given block type. This does not look UP or DOWN and only looks
     * on the current plane for the block (that is, the current y-level)
     *
     * @param types
     * @return the Block found. If it was not found, NULL will be returned
     */
    public Block findBlockRelativeToXZ(int... types) {
        Block block;

        // a set of integers to match
        Set<Integer> typeSet = new HashSet<Integer>();
        for (int type : types) {
            typeSet.add(type);
        }

        // First, the x plane
        if ((block = getRelative(-1, 0, 0)) != null) {
            if (typeSet.contains(block.getType())) {
                return block;
            }
        }

        if ((block = getRelative(1, 0, 0)) != null) {
            if (typeSet.contains(block.getType())) {
                return block;
            }
        }

        // now the z plane
        if ((block = getRelative(0, 0, -1)) != null) {
            if (typeSet.contains(block.getType())) {
                return block;
            }
        }

        if ((block = getRelative(0, 0, 1)) != null) {
            if (typeSet.contains(block.getType())) {
                return block;
            }
        }

        // nothing at all found QQ
        return null;
    }

    /**
     * Finds a block relative to the block with the given block type. This does not on the current plane
     * but looks on the planes directly above and below the block (i.e the block above and below this block)
     *
     * @param types
     * @return the Block found. If it was not found, NULL will be returned
     */
    public Block findBlockRelativeToY(int... types) {
        Block block;

        // a set of integers to match
        Set<Integer> typeSet = new HashSet<Integer>();
        for (int type : types) {
            typeSet.add(type);
        }

        // block above
        if ((block = getRelative(0, 1, 0)) != null) {
            if (typeSet.contains(block.getType())) {
                return block;
            }
        }

        // block below
        if ((block = getRelative(0, -1, 0)) != null) {
            if (typeSet.contains(block.getType())) {
                return block;
            }
        }

        // nothing at all found QQ
        return null;
    }

}
