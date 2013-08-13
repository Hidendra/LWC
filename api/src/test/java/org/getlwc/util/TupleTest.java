package org.getlwc.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TupleTest {

    private Tuple<Object, Object> tuple;

    private Object first, second;

    @Before
    public void setUp() {
        tuple = new Tuple<Object, Object>(first = new Object(), second = new Object());
    }

    @Test
    public void testGetters() {
        assertEquals(first, tuple.first());
        assertEquals(second, tuple.second());
    }

    @Test
    public void testEquals() {
        Tuple<Object, Object> equiv = new Tuple<Object, Object>(first, second); // should be equivalent to tuple
        assertEquals(tuple, equiv);
    }

    @Test
    public void testHashcode() {
        Tuple<Object, Object> equiv = new Tuple<Object, Object>(first, second); // should be equivalent to tuple
        assertEquals(tuple.hashCode(), equiv.hashCode());
    }

}
