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

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;

/**
 * Similar to LRUCache but instead uses WeakReferences.
 * The key must be a hard ref, while the value will be a weak reference
 */
public class WeakLRUCache<K, V> {

    /**
     * The backing linked hashmap for the cache
     */
    private final LinkedHashMap<K, WeakReference<V>> weakCache;

    /**
     * Amount of reads performed on the cache
     */
    private long reads = 0;

    /**
     * Amount of writes performed on the cache
     */
    private long writes = 0;

    public WeakLRUCache(final int maxCapacity) {
        this.weakCache = new LinkedHashMap<K, WeakReference<V>>(maxCapacity) {
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<K, WeakReference<V>> eldest) {
                return size() > maxCapacity;
            }
        };
    }

    /**
     * Gets the size of the cache currently
     * 
     * @return
     */
    public int size() {
        return weakCache.size();
    }

    /**
     * Clear the cache
     */
    public void clear() {
        weakCache.clear();
    }

    /**
     * Get a value from the cache
     *
     * @param key
     * @return
     */
    public V get(Object key) {
        reads++;

        WeakReference<V> weakRef = weakCache.get(key);
        return weakRef != null ? weakRef.get() : null;
    }

    /**
     * Put a value into the cache
     *
     * @param key
     * @param value
     * @return
     */
    public V put(K key, V value) {
        writes++;

        WeakReference<V> oldRef = weakCache.put(key, new WeakReference<V>(value));
        return oldRef != null ? oldRef.get() : null;
    }

    /**
     * @return amount of reads on the cache
     */
    public long getReads() {
        return reads;
    }

    /**
     * @return amount of writes on the cache
     */
    public long getWrites() {
        return writes;
    }

}
