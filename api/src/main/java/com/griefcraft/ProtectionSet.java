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

package com.griefcraft;

import com.griefcraft.model.Protection;
import com.griefcraft.world.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProtectionSet {

    public enum BlockType {

        /**
         * The block may be the block that is present in the database and can be protected
         */
        PROTECTABLE,

        /**
         * The block can match the protection, but it itself cannot be protected
         */
        MATCHABLE

    }

    /**
     * The blocks that were found
     */
    private final Map<BlockType, List<Block>> blocks = new HashMap<BlockType, List<Block>>();

    /**
     * The resultant protection
     */
    private Protection resultant = null;

    /**
     * If the set is locked towards new changes. The object is generally locked once a protection
     * is successfully matched or not matched to prevent further changes.
     */
    private boolean locked = false;

    public ProtectionSet() {
        initialize();
    }

    /**
     * Add a block to the set
     *
     * @param type
     * @param block
     */
    public void add(BlockType type, Block block) {
        if (locked) {
            throw new IllegalStateException("Result cannot be changed once locked.");
        }

        List<Block> blocks = this.blocks.get(type);
        blocks.add(block);
    }

    /**
     * Get the blocks for the given block type. The list returned is NOT modifiable
     *
     * @param type
     * @return
     */
    public List<Block> get(BlockType type) {
        return Collections.unmodifiableList(blocks.get(type));
    }

    /**
     * Get the resultant protection
     *
     * @return
     */
    public Protection getResultant() {
        return resultant;
    }

    /**
     * Set the set as matched and lock it.
     *
     * @param resultant the resultant. Can be null.
     */
    protected void matchedResultant(Protection resultant) {
        if (locked) {
            throw new IllegalStateException("Result cannot be changed once locked.");
        }

        this.resultant = resultant;
        locked = true;
    }

    /**
     * Initialize the block mapping, zeroing it out
     */
    private void initialize() {
        for (BlockType type : BlockType.values()) {
            blocks.put(type, new ArrayList<Block>());
        }
    }

}
