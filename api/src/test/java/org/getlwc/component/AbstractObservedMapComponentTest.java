package org.getlwc.component;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractObservedMapComponentTest {

    private AbstractObservedMapComponent<String, String> component;

    @Before
    public void setup() {
        component = new SimpleObservedMapComponent<>();
    }

    @Test
    public void testAdded() {
        component.put("test", "a");

        assertEquals(1, component.getObjectsAdded().size());
        assertTrue(component.getObjectsAdded().contains("a"));

        component.put("test2", "b");
        assertEquals(2, component.getObjectsAdded().size());
        assertTrue(component.getObjectsAdded().contains("a"));
        assertTrue(component.getObjectsAdded().contains("b"));

        component.resetObservedState();
        assertEquals(0, component.getObjectsAdded().size());
    }

    @Test
    public void testRemoved() {
        component.put("test", "a");
        component.put("test2", "b");
        component.put("test3", "d");
        component.put("test4", "c");

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

            component.put(key, Integer.toString(i));
            assertEquals(i + 1, component.getObjectsAdded().size());

            // adding it again should result in no change as String is hashed
            component.put(key, Integer.toString(i));
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
