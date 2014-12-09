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
/**
 * This copy of Woodstox XML processor is licensed under the
 * Apache (Software) License, version 2.0 ("the License").
 * See the License for details about distribution rights, and the
 * specific rights regarding derivate works.
 *
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing Woodstox, in file "ASL2.0", under the same directory
 * as this file.
 */
package org.getlwc.component;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractObservedSetComponentTest {

    private AbstractObservedSetComponent<String> component;

    @Before
    public void setup() {
        component = new SimpleObservedSetComponent<>();
    }

    @Test
    public void testAdded() {
        component.add("test");

        assertEquals(1, component.getObjectsAdded().size());
        assertTrue(component.getObjectsAdded().contains("test"));

        component.add("test2");
        assertEquals(2, component.getObjectsAdded().size());
        assertTrue(component.getObjectsAdded().contains("test"));
        assertTrue(component.getObjectsAdded().contains("test2"));

        component.resetObservedState();
        assertEquals(0, component.getObjectsAdded().size());
    }

    @Test
    public void testRemoved() {
        component.add("test");
        component.add("test2");
        component.add("test3");
        component.add("test4");

        assertEquals(4, component.getObjectsAdded().size());
        assertEquals(0, component.getObjectsRemoved().size());

        component.remove("test4");
        component.remove("testNotExists");

        assertEquals(4, component.getObjectsAdded().size());
        assertEquals(1, component.getObjectsRemoved().size());

        component.remove("test3");

        assertEquals(4, component.getObjectsAdded().size());
        assertEquals(2, component.getObjectsRemoved().size());

        component.resetObservedState();

        assertEquals(0, component.getObjectsAdded().size());
        assertEquals(0, component.getObjectsRemoved().size());

        component.remove("test");
        component.remove("test2");
        component.remove("testNotExists");

        assertEquals(0, component.getObjectsAdded().size());
        assertEquals(2, component.getObjectsRemoved().size());
    }

    @Test
    public void testCombined() {
        for (int i = 0; i < 1000; i ++) {
            String key = "someLongKeyDefinition" + i;

            component.add(key);
            assertEquals(i + 1, component.getObjectsAdded().size());

            // adding it again should result in no change as String is hashed
            component.add(key);
            assertEquals(i + 1, component.getObjectsAdded().size());
        }

        for (int i = 0; i < 1000; i ++) {
            String key = "someLongKeyDefinition" + i;

            component.remove(key);
            assertEquals(i + 1, component.getObjectsRemoved().size());
            assertEquals(1000, component.getObjectsAdded().size());

            // same as above -- it shouldn't change anything by removing it a second time
            component.remove(key);
            assertEquals(i + 1, component.getObjectsRemoved().size());
        }
    }

}
