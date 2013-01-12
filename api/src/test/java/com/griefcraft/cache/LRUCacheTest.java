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

package com.griefcraft.cache;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LRUCacheTest {

    @Test
    public void testCapacity() {

        // The max capacity for our test
        int capacity = 500;
        int initialObjects = 250;

        // construct a cache
        LRUCache<Integer, Object> cache = new LRUCache<Integer, Object>(capacity);

        // It should be 0 initially
        assertEquals(cache.size(), 0);

        // Fill it with some objects
        for (int i = 0; i < initialObjects; i++) {
            cache.put(i, new Object());
        }

        // Should be initialObjects
        assertEquals(cache.size(), initialObjects);

        // Overfill the array
        for (int i = 0; i < capacity * 2; i++) {
            cache.put(i, new Object());
        }

        // It should only be at max capacity
        assertEquals(cache.size(), capacity);

    }

    @Test
    public void testCounters() {

        // Our numbers to use
        int capacity = 500;
        int writes = 750;
        int reads = 1000;

        LRUCache<Integer, Object> cache = new LRUCache<Integer, Object>(capacity);

        // Counters should be 0 initially
        assertEquals(cache.getReads(), 0);
        assertEquals(cache.getWrites(), 0);

        // Perform some writes
        for (int i = 0; i < writes; i++) {
            cache.put(i, new Object());
        }

        // We should have *writes* writes
        assertEquals(cache.getWrites(), writes);

        // And now some reads
        for (int i = 0; i < reads; i++) {
            cache.get(i);
        }

        // Should be reads reads
        assertEquals(cache.getReads(), reads);

    }

}