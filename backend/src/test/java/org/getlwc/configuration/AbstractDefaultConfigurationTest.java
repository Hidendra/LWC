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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractDefaultConfigurationTest extends AbstractConfigurationTest {

    @Test
    public void testDefaultEmptyConfigContains() {
        assertFalse(emptyConfiguration.contains("some.path"));

        emptyConfiguration.setDefault("some.path", 42);

        assertTrue(emptyConfiguration.contains("some.path"));
        assertEquals(42, emptyConfiguration.getInt("some.path"));

        retestConfigTests();
    }

    @Test
    public void testDefaultPreLoadedConfigContains() {
        preloadedConfiguration.setDefault("map", 42);
        preloadedConfiguration.setDefault("map.key2", 42);
        preloadedConfiguration.setDefault("map.map.list2", 42);

        assertTrue(preloadedConfiguration.contains("map.key"));
        assertTrue(preloadedConfiguration.contains("map.key2"));
        assertTrue(preloadedConfiguration.contains("map.map.list"));
        assertTrue(preloadedConfiguration.contains("map.map.list2"));

        assertEquals(42, preloadedConfiguration.getInt("map.key2"));
        assertEquals(42, preloadedConfiguration.getInt("map.map.list2"));

        retestConfigTests();
    }

    @Test
    public void testPreLoadedExistingGet() {
        preloadedConfiguration.setDefault("map.key", "defaultValue");
        assertEquals("value", preloadedConfiguration.getString("map.key"));

        retestConfigTests();
    }

    /**
     * Runs all abstract config tests again. This is typically to ensure that
     * everything still works as they should even with defaults set.
     */
    private void retestConfigTests() {
        super.testGet();
        super.testHasProperty();
        super.testSetOnExistentKeys();
        super.testSetOnNonexistentKeys();
    }

}
