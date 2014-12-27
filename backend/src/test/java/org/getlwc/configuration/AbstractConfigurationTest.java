/**
 * Copyright (c) 2011-2014 Tyler Blair
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
package org.getlwc.configuration;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractConfigurationTest {

    /**
     * Empty config object (no pre-loaded data)
     */
    protected Configuration emptyConfiguration;

    /**
     * Config object pre-loaded with the following (JSON-equivalent):
     * { "map": { "key": "value", "map": { "list": [ 1, 2, 3 ] } }, "primitives": { "int": 1, "bool": true } }
     */
    protected Configuration preloadedConfiguration;

    @Test
    public void testHasProperty() {
        assertFalse(emptyConfiguration.containsPath("root"));
        assertFalse(emptyConfiguration.containsPath("root.node"));

        assertTrue(preloadedConfiguration.containsPath("map"));
        assertTrue(preloadedConfiguration.containsPath("map.key"));
        assertTrue(preloadedConfiguration.containsPath("map.map"));
        assertTrue(preloadedConfiguration.containsPath("map.map.list"));
        assertTrue(preloadedConfiguration.containsPath("primitives"));
        assertTrue(preloadedConfiguration.containsPath("primitives.int"));
        assertTrue(preloadedConfiguration.containsPath("primitives.bool"));
    }

    @Test
    public void testGet() {
        assertTrue(preloadedConfiguration.getBoolean("primitives.bool"));
        assertEquals(1, preloadedConfiguration.getInt("primitives.int"));
        assertEquals("value", preloadedConfiguration.getString("map.key"));

        // TODO getter for lists?
        List list = (List) preloadedConfiguration.get("map.map.list");
        assertEquals(3, list.size());
        assertEquals(1, (long) list.get(0));
        assertEquals(2, (long) list.get(1));
        assertEquals(3, (long) list.get(2));
    }

    @Test
    public void testSetOnNonexistentKeys() {
        assertFalse(emptyConfiguration.containsPath("some.sub.key"));
        emptyConfiguration.set("some.sub.key", 42);
        assertTrue(emptyConfiguration.containsPath("some.sub.key"));
        assertFalse(emptyConfiguration.containsPath("some.sub.other"));
        assertFalse(emptyConfiguration.containsPath("some.sub2.key"));
        assertEquals(42, emptyConfiguration.getInt("some.sub.key"));

        emptyConfiguration.set("some", "testString");
        assertTrue(emptyConfiguration.containsPath("some"));
        assertFalse(emptyConfiguration.containsPath("some.sub.key"));
        assertEquals("testString", emptyConfiguration.get("some"));
    }

    @Test
    public void testSetOnExistentKeys() {
        preloadedConfiguration.set("primitives.int", 42);
        preloadedConfiguration.set("primitives.bool", false);
        assertEquals(42, preloadedConfiguration.getInt("primitives.int"));
        assertEquals(false, preloadedConfiguration.getBoolean("primitives.bool"));

        preloadedConfiguration.set("map.key", null);
        assertEquals(null, preloadedConfiguration.get("map.key"));
        assertTrue(preloadedConfiguration.containsPath("map.key"));
    }

}
