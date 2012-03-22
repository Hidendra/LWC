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

import java.util.logging.Logger;

public class ProtectionCache {

    /**
     * Logging instance
     */
    private Logger logger = Logger.getLogger("Cache");

    /**
     * The LWC instance this set belongs to
     */
    private final LWC lwc;

    /**
     * Hard references to protections still cached 
     */
    private final LRUCache<Protection, Object> references;

    /**
     * Known null cache keys
     */
    private final LRUCache<Integer, Object> nulls;

    /**
     * Weak references to protections and their cache key (protection.getCacheKey())
     */
    private final WeakLRUCache<String, Protection> byCacheKey;

    /**
     * Weak references to protections and their protection id
     */
    private final WeakLRUCache<Integer, Protection> byId;

    /**
     * The capacity of the cache
     */
    private int capacity;

    public ProtectionCache(LWC lwc) {
        this.lwc = lwc;
        this.capacity = lwc.getConfiguration().getInt("core.cacheSize", 10000);

        this.references = new LRUCache<Protection, Object>(capacity);
        this.nulls = new LRUCache<Integer, Object>(capacity * 10);
        this.byCacheKey = new WeakLRUCache<String, Protection>(capacity);
        this.byId = new WeakLRUCache<Integer, Protection>(capacity);
        logger.info("LWC: Protection cache: 0/" + capacity);
    }

    /**
     * Gets the total amount of reads performed on the cache
     *
     * @return
     */
    public long getReads() {
        return byCacheKey.getReads() + byId.getReads();
    }

    /**
     * Gets the total amount of writes performed on the cache
     *
     * @return
     */
    public long getWrites() {
        return byCacheKey.getWrites() + byId.getWrites();
    }

    /**
     * Gets the max capacity of the cache
     *
     * @return
     */
    public int capacity() {
        return capacity;
    }

    /**
     * Clears the entire protection cache
     */
    public void clear() {
        // Remove nulls
        nulls.clear();
        
        // remove hard refs
        references.clear();

        // remove weak refs
        byCacheKey.clear();
        byId.clear();
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
        
        // Remove it from the known nulls if it exists
        nulls.remove(protection.getCacheKey().hashCode());

        // Add the hard reference
        references.put(protection, null);

        // Add weak references which are used to lookup protections
        byCacheKey.put(protection.getCacheKey(), protection);
        byId.put(protection.getId(), protection);
    }

    /**
     * Add a cache key as known to be null
     *
     * @param cacheKey
     */
    public void addNull(String cacheKey) {
        // Is it cached already?
        if (getProtection(cacheKey) != null) {
            return;
        }

        nulls.put(cacheKey.hashCode(), null);
    }

    /**
     * Remove the protection from the cache
     *
     * @param protection
     */
    public void remove(Protection protection) {
        references.remove(protection);
        byCacheKey.remove(protection.getCacheKey());
        byId.remove(protection.getId());
    }

    /**
     * Check if a cache key is known to not exist in the database
     *
     * @param cacheKey
     * @return
     */
    public boolean isKnownToBeNull(String cacheKey) {
        return nulls.containsKey(cacheKey.hashCode());
    }

    /**
     * Get a protection in the cache via its cache key
     *
     * @param cacheKey
     * @return
     */
    public Protection getProtection(String cacheKey) {
        return byCacheKey.get(cacheKey);
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

}
