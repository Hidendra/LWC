package org.getlwc;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocationTest {

    private Location loc;

    @Before
    public void setUp() {
        loc = new Location(mock(World.class), 1.25, 2.50, 3.75);
        when(loc.getWorld().getName()).thenReturn("world");
    }

    @Test
    public void testDoubleGetters() {
        assertEquals(1.25, loc.getX(), 0);
        assertEquals(2.50, loc.getY(), 0);
        assertEquals(3.75, loc.getZ(), 0);
    }

    @Test
    public void testIntGetters() {
        assertEquals(1, loc.getBlockX());
        assertEquals(2, loc.getBlockY());
        assertEquals(3, loc.getBlockZ());
    }

    @Test
    public void testEquals() {
        Location equiv = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
        assertEquals(equiv, loc);
    }

    @Test
    public void testHashCode() {
        Location equiv = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
        assertEquals(equiv.hashCode(), loc.hashCode());
    }

}
