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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Similar to LRUCache but instead uses WeakReferences.
 * The key must be a hard ref, while the value will be a weak reference
 */
public class WeakLRUCache<K, V> implements Map<K, V> {

    /**
     * The backing linked hashmap for the cache
     */
    private final LinkedHashMap<K, WeakValue<V, K>> weakCache;

    /**
     * The queue of references to be removed
     */
    private final ReferenceQueue<? super V> queue = new ReferenceQueue();

    /**
     * The cache's max capacity
     */
    protected int maxCapacity;

    /**
     * Amount of reads performed on the cache
     */
    private long reads = 0;

    /**
     * Amount of writes performed on the cache
     */
    private long writes = 0;

    public WeakLRUCache(int capacity) {
        this.maxCapacity = capacity;

        this.weakCache = new LinkedHashMap<K, WeakValue<V, K>>(maxCapacity) {
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<K, WeakValue<V, K>> eldest) {
                return size() > maxCapacity;
            }
        };
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

    /**
     * Processes the reference queue and removes any garbage collected values
     */
    private void processQueue() {
        WeakValue<V, K> weakValue;
        while ((weakValue = (WeakValue<V, K>) queue.poll()) != null) {
            // remove it from the cache
            weakCache.remove(weakValue.key);
        }
    }

    /**
     * Gets the size of the cache currently
     *
     * @return
     */
    public int size() {
        processQueue();
        return weakCache.size();
    }

    public boolean isEmpty() {
        processQueue();
        return weakCache.isEmpty();
    }

    public boolean containsKey(Object key) {
        processQueue();
        return weakCache.containsKey(key);
    }

    public boolean containsValue(Object value) {
        processQueue();
        return weakCache.containsValue(value);
    }

    public void clear() {
        processQueue();
        weakCache.clear();
    }

    public Set<K> keySet() {
        processQueue();
        return weakCache.keySet();
    }

    public V get(Object key) {
        reads++;
        processQueue();

        WeakValue<V, K> weakRef = weakCache.get(key);
        V result = null;

        if (weakRef != null) {
            result = weakRef.get();

            // If the result is still null, we should remove it!
            if (result == null) {
                weakCache.remove(key);
            }
        }

        return result;
    }

    public V put(K key, V value) {
        writes++;
        processQueue();

        WeakValue<V, K> oldRef = weakCache.put(key, new WeakValue<V, K>(value, key, queue));
        return oldRef != null ? oldRef.get() : null;
    }

    public V remove(Object key) {
        WeakValue<V, K> old = weakCache.remove(key);
        return old != null ? old.get() : null;
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("putAll() is not supported by WeakLRUCache");
    }

    public Collection<V> values() {
        throw new UnsupportedOperationException("values() is not supported by WeakLRUCache");
    }

    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException("entrySet() is not supported by WeakLRUCache");
    }

    /**
     * A WeakValue which stores the value of the weak reference and the key in the HashMap
     * so it can be more quickly removed when GCd
     *
     * @param <V>
     * @param <K>
     */
    private final class WeakValue<V, K> extends WeakReference<V> {

        /**
         * The key for the value
         */
        private final K key;

        private WeakValue(V value, K key, ReferenceQueue<? super V> queue) {
            super(value, queue);
            this.key = key;
        }

    }

}
