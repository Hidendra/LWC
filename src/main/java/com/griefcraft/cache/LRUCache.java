/**
 * This file is part of LWC (https://github.com/Hidendra/LWC)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.griefcraft.cache;

import java.util.LinkedHashMap;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 1L;

    /**
     * The max number of entries allowed
     */
    private int maxCapacity;

    /**
     * Amount of reads performed on the cache
     */
    private int reads = 0;

    /**
     * Amount of writes performed on the cache
     */
    private int writes = 0;

    public LRUCache(int maxCapacity) {
        super(maxCapacity, 0.75f, true);
        this.maxCapacity = maxCapacity;
    }

    @Override
    public V get(Object key) {
        reads ++;
        return super.get(key);
    }

    @Override
    public V put(K key, V value) {
        writes ++;
        return super.put(key, value);
    }

    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        return size() > maxCapacity;
    }

    /**
     * @return amount of reads on the cache
     */
    public int getReads() {
        return reads;
    }

    /**
     * @return amount of writes on the cache
     */
    public int getWrites() {
        return writes;
    }

}
