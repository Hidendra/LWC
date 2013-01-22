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

import org.getlwc.model.Protection;

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

    private static final class BlockNode {

        /**
         * The block at this node
         */
        Block block;

        /**
         * If the node has been checked in the database to a protection or not. Allows us to wastefully
         * reiterate the blocks to look for protections without wasting queries.
         * TODO
         */
        boolean checked = false;

        BlockNode(Block block) {
            this.block = block;
        }

    }

    /**
     * The LWC engine instance
     */
    private Engine engine;

    /**
     * The blocks that were found
     */
    private final Map<BlockType, Map<Location, BlockNode>> blocks = new HashMap<BlockType, Map<Location, BlockNode>>();

    /**
     * The resultant protection
     */
    private Protection resultant = null;

    /**
     * If the set is locked towards new changes. The object is generally locked once a protection
     * is successfully matched or not matched to prevent further changes.
     */
    private boolean locked = false;

    public ProtectionSet(Engine engine) {
        this.engine = engine;
        initialize();
    }

    /**
     * Check for protections on the available protectable blocks that haven't been checked yet.
     * This method is normally called after a block (or set of blocks) has been added to the matcher to
     * save processing time.
     *
     * @return true if a protection was matched
     */
    public boolean checkForProtections() {
        // if we've already found a protection don't bother searching
        if (resultant != null) {
            return true;
        }

        for (BlockNode node : blocks.get(BlockType.PROTECTABLE).values()) {
            // has the node been checked ?
            if (node.checked) {
                return resultant != null;
            }

            // look for a protection
            resultant = engine.getDatabase().loadProtection(node.block.getLocation());

            // mark the node as checked
            node.checked = true;

            // if we've matched the node then don't continue searching
            if (resultant != null) {
                return true;
            }
        }

        return false;
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

        Map<Location, BlockNode> blocks = this.blocks.get(type);
        blocks.put(block.getLocation(), new BlockNode(block));
    }

    /**
     * Add a block to the set
     *
     * @param block
     */
    public void add(Block block) {
        add(getBlockType(block), block);
    }

    /**
     * Get the blocks for the given block type. The list returned is NOT modifiable and can be expensive.
     * The list has to essentially be copied from a map, so O(N)
     *
     * @param type
     * @return
     */
    public List<Block> get(BlockType type) {
        List<Block> blocks = new ArrayList<Block>();

        for (BlockNode node : this.blocks.get(type).values()) {
            blocks.add(node.block);
        }

        return Collections.unmodifiableList(blocks);
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
    public void matchedResultant(Protection resultant) {
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
            blocks.put(type, new HashMap<Location, BlockNode>());
        }
    }

    /**
     * Get the BlockType that should be used for the given block
     *
     * @param block
     * @return
     */
    private ProtectionSet.BlockType getBlockType(Block block) {
        return engine.getProtectionManager().isBlockProtectable(block) ? ProtectionSet.BlockType.PROTECTABLE : ProtectionSet.BlockType.MATCHABLE;
    }

}
