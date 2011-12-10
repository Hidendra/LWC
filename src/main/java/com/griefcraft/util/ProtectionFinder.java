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

package com.griefcraft.util;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.util.matchers.DoorMatcher;
import com.griefcraft.util.matchers.DoubleChestMatcher;
import com.griefcraft.util.matchers.GravityMatcher;
import com.griefcraft.util.matchers.WallMatcher;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Searches for blocks that can potentially be a protection
 */
public class ProtectionFinder {

    /**
     * A list of the protection matchers
     */
    public static final List<Matcher> PROTECTION_MATCHERS = new ArrayList<Matcher>();

    static {
        PROTECTION_MATCHERS.add(new DoubleChestMatcher());
        PROTECTION_MATCHERS.add(new WallMatcher());
        PROTECTION_MATCHERS.add(new DoorMatcher());
        PROTECTION_MATCHERS.add(new GravityMatcher());
    }

    /**
     * The LWC object to work with
     */
    private LWC lwc;

    /**
     * The base block to match off of
     */
    private Block baseBlock = null;

    /**
     * The matched protection if found
     */
    private Protection matchedProtection = null;

    /**
     * All of the matched blocks
     */
    private final Set<Block> blocks = new HashSet<Block>();

    /**
     * All of the blocks that are protectables
     */
    private final Set<Block> protectables = new HashSet<Block>();

    public ProtectionFinder(LWC lwc) {
        this.lwc = lwc;
    }

    /**
     * Try and match blocks using the given base block
     *
     * @param baseBlock
     * @return TRUE if a set of blocks was found
     */
    public boolean matchBlocks(Block baseBlock) {
        // Did we already find a protection?
        if (matchedProtection != null) {
            return true;
        }

        // First, reset
        this.reset();
        this.baseBlock = baseBlock;

        // If the base block is protectable, try it
        blocks.add(baseBlock);
        if (lwc.isProtectable(baseBlock)) {
            // load it
            if (tryLoadProtection(baseBlock)) {
                return true;
            }
        }

        // Now attempt to use the matchers
        for (Matcher matcher : PROTECTION_MATCHERS) {
            if (matcher.matches(this)) {
                // Wee, we matched a protection somewhere!
                return true;
            }
        }

        // No protection was found
        return false;
    }

    /**
     * Add a block to the matched blocks
     *
     * @param block
     */
    public void addBlock(Block block) {
        blocks.add(block);
    }

    /**
     * Load the protection for the calculated protectables.
     * Returns NULL if no protection was found.
     *
     * @return
     */
    public Protection loadProtection() {
        // Do we have a result already cached?
        if (matchedProtection != null) {
            return matchedProtection;
        }

        // Calculate the protectables
        calculateProtectables();

        for (Block block : protectables) {
            if (tryLoadProtection(block)) {
                return matchedProtection;
            }
        }

        return null;
    }

    /**
     * Try and load a protection for a given block. If succeded, cache it locally
     * 
     * @param block
     * @return
     */
    public boolean tryLoadProtection(Block block) {
        Protection protection = lwc.getPhysicalDatabase().loadProtection(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());

        if (protection != null) {
            // ensure it's the right block
            if (protection.getBlockId() > 0 && protection.getBlockId() == protection.getBlock().getTypeId()) {
                this.matchedProtection = protection;
            }
        }

        return this.matchedProtection != null;
    }

    /**
     * Get the finder's base block
     *
     * @return
     */
    public Block getBaseBlock() {
        return baseBlock;
    }

    /**
     * Get an immutable set of the matched blocks
     *
     * @return
     */
    public Set<Block> getBlocks() {
        return Collections.unmodifiableSet(blocks);
    }

    /**
     * Get an immutable set of the protectable blocks
     *
     * @return
     */
    public Set<Block> getProtectables() {
        return Collections.unmodifiableSet(protectables);
    }

    /**
     * Reset the matcher state
     */
    private void reset() {
        blocks.clear();
        protectables.clear();
        baseBlock = null;
    }

    /**
     * From the matched blocks calculate and store the blocks that are protectable
     */
    private void calculateProtectables() {
        // reset the matched protectables
        protectables.clear();

        // go through the blocks
        for (Block block : blocks) {
            // Is it protectable?
            if (lwc.isProtectable(block)) {
                protectables.add(block);
            }
        }
    }

    /**
     * Matches protections
     */
    public interface Matcher {

        /**
         * Check if the block matches any VALID protections.
         * 
         * @return
         */
        public boolean matches(ProtectionFinder finder);

    }

}
