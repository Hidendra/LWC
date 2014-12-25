package org.getlwc.configuration;

import org.junit.Test;

import java.util.Arrays;
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
        assertFalse(emptyConfiguration.contains("root"));
        assertFalse(emptyConfiguration.contains("root.node"));

        assertTrue(preloadedConfiguration.contains("map"));
        assertTrue(preloadedConfiguration.contains("map.key"));
        assertTrue(preloadedConfiguration.contains("map.map"));
        assertTrue(preloadedConfiguration.contains("map.map.list"));
        assertTrue(preloadedConfiguration.contains("primitives"));
        assertTrue(preloadedConfiguration.contains("primitives.int"));
        assertTrue(preloadedConfiguration.contains("primitives.bool"));
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
        assertFalse(emptyConfiguration.contains("some.sub.key"));
        emptyConfiguration.set("some.sub.key", 42);
        assertTrue(emptyConfiguration.contains("some.sub.key"));
        assertFalse(emptyConfiguration.contains("some.sub.other"));
        assertFalse(emptyConfiguration.contains("some.sub2.key"));
        assertEquals(42, emptyConfiguration.getInt("some.sub.key"));

        emptyConfiguration.set("some", "testString");
        assertTrue(emptyConfiguration.contains("some"));
        assertFalse(emptyConfiguration.contains("some.sub.key"));
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
        assertTrue(preloadedConfiguration.contains("map.key"));
    }

}
