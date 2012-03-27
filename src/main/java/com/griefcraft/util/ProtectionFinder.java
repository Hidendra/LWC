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

import com.griefcraft.cache.ProtectionCache;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.util.matchers.DoorMatcher;
import com.griefcraft.util.matchers.DoubleChestMatcher;
import com.griefcraft.util.matchers.GravityMatcher;
import com.griefcraft.util.matchers.WallMatcher;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Searches for blocks that can potentially be a protection
 */
public class ProtectionFinder {

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
     * If we already checked the database
     */
    private boolean searched = false;

    /**
     * All of the matched blocks
     */
    private final List<Block> blocks = new LinkedList<Block>();

    /**
     * All of the blocks that are protectables
     */
    private final List<Block> protectables = new LinkedList<Block>();

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
        reset();
        this.baseBlock = baseBlock;

        // Add the base block
        addBlock(baseBlock);

        // Check the base-block
        Result result;
        if ((result = tryLoadProtection(baseBlock, false)) != Result.E_NOT_FOUND) {
            return result == Result.E_FOUND;
        }

        // Now attempt to use the matchers
        for (Matcher matcher : getProtectionMatchers()) {
            boolean matches = matcher.matches(this);

            if (matches) {
                // we matched a protection somewhere!
                return true;
            }
        }

        // Blacklist each block, all of them did not return matches
        for (Block block : blocks) {
            LWC.getInstance().getProtectionCache().addNull(block);
        }

        // No matches
        searched = true;
        return false;
    }

    /**
     * Get the possible protection matchers that can match the protection
     *
     * @return
     */
    public Matcher[] getProtectionMatchers() {
        Material material = baseBlock.getType();
        
        // Double chests
        if (DoubleChestMatcher.PROTECTABLES_CHESTS.contains(material)) {
            return new Matcher[] {
                    new DoubleChestMatcher()
            };
        }
        
        // Gravity
        else if (GravityMatcher.PROTECTABLES_POSTS.contains(material)) {
            return new Matcher[] {
                    new GravityMatcher()
            };
        }
        
        // Doors
        else if (DoorMatcher.PROTECTABLES_DOORS.contains(material)) {
            return new Matcher[] {
                    new DoorMatcher()
            };
        }
        
        // Anything else
        else {
            return new Matcher[] {
                    new DoorMatcher(),
                    new GravityMatcher(),
                    new WallMatcher()
            };
        }
        
    }

    /**
     * Add a block to the matched blocks
     *
     * @param block
     */
    public void addBlock(Block block) {
        if (!blocks.contains(block)) {
            blocks.add(block);
        }
    }

    /**
     * Load the protection for the calculated protectables.
     * Returns NULL if no protection was found.
     *
     * @return
     */
    public Protection loadProtection() {
        return loadProtection(false);
    }

    /**
     * Load the protection for the calculated protectables.
     * Returns NULL if no protection was found.
     *
     * @param noAutoCache if a match is found, don't cache it to be the protection we use
     * @return
     */
    public Protection loadProtection(boolean noAutoCache) {
        // Do we have a result already cached?
        if (searched) {
            return matchedProtection;
        }

        // Calculate the protectables
        calculateProtectables();
        searched = true;

        for (Block block : protectables) {
            if (tryLoadProtection(block, noAutoCache) == Result.E_FOUND) {
                return matchedProtection;
            }
        }

        return null;
    }

    /**
     * Try and load a protection for a given block. If succeded, cache it locally
     * 
     * @param block
     * @param noAutoCache if a match is found, don't cache it to be the protection we use
     * @return
     */
    protected Result tryLoadProtection(Block block, boolean noAutoCache) {
        if (matchedProtection != null) {
            return Result.E_FOUND;
        }

        LWC lwc = LWC.getInstance();
        ProtectionCache cache = lwc.getProtectionCache();

        // Check the cache
        if ((matchedProtection = cache.getProtection(block)) != null) {
            searched = true;
            if (matchedProtection.getProtectionFinder() == null) {
                matchedProtection.setProtectionFinder(this);
                matchedProtection.removeCache();
                lwc.getProtectionCache().add(matchedProtection);
            }
            return Result.E_FOUND;
        }

        // Manual intervention is required
        if (block.getType() == Material.REDSTONE_WIRE || block.getType() == Material.REDSTONE_TORCH_OFF ||
                block.getType() == Material.REDSTONE_TORCH_ON) {
            return Result.E_ABORT;
        }

        // Null cache
        if (cache.isKnownToBeNull(block)) {
            searched = true;
            return Result.E_ABORT;
        }
        
        // don't bother trying to load it if it is not protectable
        if (!lwc.isProtectable(block)) {
            return Result.E_NOT_FOUND;
        }
        
        // Null-check
        if (block.getWorld() == null) {
            lwc.log("World is null for the block " + block);
            return Result.E_NOT_FOUND;
        }

        Protection protection = lwc.getPhysicalDatabase().loadProtection(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());

        if (protection != null) {
            if (protection.getProtectionFinder() == null) {
                protection.setProtectionFinder(this);
                protection.removeCache();
                lwc.getProtectionCache().add(protection);
            }

            // ensure it's the right block
            if (protection.getBlockId() > 0) {
                if (protection.isBlockInWorld()) {
                    if (noAutoCache) {
                        return Result.E_FOUND;
                    }

                    this.matchedProtection = protection;
                    searched = true;
                } else {
                    // Corrupted protection
                    System.out.println("Removing corrupted protection: " + protection);
                    protection.remove();
                }
            }
        }

        return this.matchedProtection != null ? Result.E_FOUND : Result.E_NOT_FOUND;
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
    public List<Block> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    /**
     * Get an immutable set of the protectable blocks
     *
     * @return
     */
    public List<Block> getProtectables() {
        return Collections.unmodifiableList(protectables);
    }

    /**
     * Reset the matcher state
     */
    private void reset() {
        blocks.clear();
        protectables.clear();
        baseBlock = null;
        searched = false;
    }

    /**
     * From the matched blocks calculate and store the blocks that are protectable
     */
    private void calculateProtectables() {
        // reset the matched protectables
        protectables.clear();
        searched = false;
        
        // if there's only 1 block it was already checked (the base block)
        int size = blocks.size();
        if (size == 1) {
            return;
        }

        // go through the blocks
        for (int index = 1; index < size; index++) {
            Block block = blocks.get(index);
            
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

    private enum Result { E_FOUND, E_ABORT, E_NOT_FOUND }

}
