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

package com.griefcraft.cache;


import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class ProtectionCache {

    /**
     * The amount the cache is increased by each time a high-intensity area requests it if needed
     */
    private final static int ADAPTIVE_CACHE_TICK = 10;

    /**
     * The max number of protections the adaptive cache will add
     */
    private final static int ADAPTIVE_CACHE_MAX = 100000;

    /**
     * The LWC instance this set belongs to
     */
    private final LWC lwc;

    /**
     * Hard references to protections still cached
     */
    private final LRUCache<Protection, Object> references;

    /**
     * Weak references to protections and their cache key (protection.getCacheKey())
     */
    private final WeakLRUCache<String, Protection> byCacheKey;

    /**
     * Weak references to protections and their protection id
     */
    private final WeakLRUCache<Integer, Protection> byId;

    /**
     * A block that isn't the protected block itself but matches it in a protection matcher
     */
    private final WeakLRUCache<String, Protection> byKnownBlock;

    /**
     * A cache of blocks that are known to not have a protection
     */
    private final WeakLRUCache<String, Object> byKnownNulls;

    /**
     * The capacity of the cache
     */
    private int capacity;

    /**
     * The number of protections that were added via adaptive cache
     */
    private int adaptiveCapacity = 0;

    /**
     * Used for byKnownNulls
     */
    private final static Object FAKE_VALUE = new Object();

    public ProtectionCache(LWC lwc) {
        this.lwc = lwc;
        this.capacity = lwc.getConfiguration().getInt("core.cacheSize", 10000);

        this.references = new LRUCache<Protection, Object>(capacity);
        this.byCacheKey = new WeakLRUCache<String, Protection>(capacity);
        this.byId = new WeakLRUCache<Integer, Protection>(capacity);
        this.byKnownBlock = new WeakLRUCache<String, Protection>(capacity);
        this.byKnownNulls = new WeakLRUCache<String, Object>(Math.max(10000, capacity)); // enforce a min size so we have a known buffer
    }

    /**
     * Called from specific potentially high-intensity access areas. These areas preferably need(!) free space in the
     * cache and otherwise could cause "lag" or other oddities.
     */
    public void increaseIfNecessary() {
        if (isFull() && adaptiveCapacity < ADAPTIVE_CACHE_MAX) {
            adaptiveCapacity += ADAPTIVE_CACHE_TICK;
            adjustCacheSizes();
        }
    }

    /**
     * Gets the direct reference of the references cache
     *
     * @return
     */
    public LRUCache<Protection, Object> getReferences() {
        return references;
    }

    /**
     * Gets the total amount of reads performed on the cache
     *
     * @return
     */
    public long getReads() {
        return byCacheKey.getReads() + byId.getReads() + byKnownBlock.getReads();
    }

    /**
     * Gets the total amount of writes performed on the cache
     *
     * @return
     */
    public long getWrites() {
        return byCacheKey.getWrites() + byId.getWrites() + byKnownBlock.getWrites();
    }

    /**
     * Gets the default capacity of the cache
     *
     * @return
     */
    public int capacity() {
        return capacity;
    }

    /**
     * Gets the adaptive capacity of the cache
     *
     * @return
     */
    public int adaptiveCapacity() {
        return adaptiveCapacity;
    }

    /**
     * Gets the total capacity (default + adaptive) of the cache
     *
     * @return
     */
    public int totalCapacity() {
        return capacity + adaptiveCapacity;
    }

    /**
     * Clears the entire protection cache
     */
    public void clear() {
        // remove hard refs
        references.clear();

        // remove weak refs
        byCacheKey.clear();
        byId.clear();
        byKnownBlock.clear();
        byKnownNulls.clear();
    }

    /**
     * Check if the cache is full
     *
     * @return
     */
    public boolean isFull() {
        return references.size() >= totalCapacity();
    }

    /**
     * Gets the amount of protections that are cached
     *
     * @return
     */
    public int size() {
        return references.size();
    }

    /**
     * Cache a protection
     *
     * @param protection
     */
    public void add(Protection protection) {
        if (protection == null) {
            return;
        }

        // Add the hard reference
        references.put(protection, null);

        // Add weak references which are used to lookup protections
        byCacheKey.put(protection.getCacheKey(), protection);
        byId.put(protection.getId(), protection);

        // get the protection's finder if it was found via that
        if (protection.getProtectionFinder() != null) {
            Block protectedBlock = protection.getBlock();
            for (Block block : protection.getProtectionFinder().getBlocks()) {
                if (block != protectedBlock) {
                    String cacheKey = cacheKey(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
                    byKnownBlock.put(cacheKey, protection);
                }
            }
        }
    }

    /**
     * Remove the protection from the cache
     *
     * @param protection
     */
    public void remove(Protection protection) {
        references.remove(protection);
        byId.remove(protection.getId());

        if (protection.getProtectionFinder() != null) {
            for (Block block : protection.getProtectionFinder().getBlocks()) {
                remove(cacheKey(block.getLocation()));
            }
        }
    }

    /**
     * Remove the given cache key from any caches
     *
     * @param cacheKey
     */
    public void remove(String cacheKey) {
        byCacheKey.remove(cacheKey);
        byKnownBlock.remove(cacheKey);
        byKnownNulls.remove(cacheKey);
    }

    /**
     * Make a cache key known as null in the cache
     *
     * @param cacheKey
     */
    public void addKnownNull(String cacheKey) {
        byKnownNulls.put(cacheKey, FAKE_VALUE);
    }

    /**
     * Check if a cache key is known to not exist in the database
     *
     * @param cacheKey
     * @return
     */
    public boolean isKnownNull(String cacheKey) {
        return byKnownNulls.containsKey(cacheKey);
    }

    /**
     * Get a protection in the cache via its cache key
     *
     * @param cacheKey
     * @return
     */
    public Protection getProtection(String cacheKey) {
        Protection protection;

        // Check the direct cache first
        if ((protection = byCacheKey.get(cacheKey)) != null) {
            return protection;
        }

        // now use the 'others' cache
        return byKnownBlock.get(cacheKey);
    }

    /**
     * Get a protection in the cache located on the given block
     *
     * @param block
     * @return
     */
    public Protection getProtection(Block block) {
        return getProtection(cacheKey(block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
    }

    /**
     * Check if the known block protection cache contains the given key
     *
     * @param block
     * @return
     */
    public boolean isKnownBlock(Block block) {
        return byKnownBlock.containsKey(cacheKey(block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
    }

    /**
     * Get a protection in the cache via its id
     *
     * @param id
     * @return
     */
    public Protection getProtectionById(int id) {
        return byId.get(id);
    }

    /**
     * Gets the cache key for the given location
     *
     * @param location
     * @return
     */
    public String cacheKey(Location location) {
        return cacheKey(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Generate a cache key using the given data
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @return
     */
    private String cacheKey(String world, int x, int y, int z) {
        return world + ":" + x + ":" + y + ":" + z;
    }

    /**
     * Fixes the internal caches and adjusts them to the new cache total capacity
     */
    private void adjustCacheSizes() {
        references.maxCapacity = totalCapacity();
        byCacheKey.maxCapacity = totalCapacity();
        byId.maxCapacity = totalCapacity();
        byKnownBlock.maxCapacity = totalCapacity();
        byKnownNulls.maxCapacity = totalCapacity();
    }

}
