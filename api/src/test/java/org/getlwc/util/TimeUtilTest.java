package org.getlwc.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeUtilTest {

    @Test
    public void testParseTime() {
        assertEquals(60 * 60 * 24, TimeUtil.parseTime("1 day"));
        assertEquals(60 * 60 * 24 * 14, TimeUtil.parseTime("2 weeks"));
        assertEquals(60 * 60 * 24 * 14, TimeUtil.parseTime("   2 weeks   "));
    }

    @Test
    public void testTimeToString() {
        assertEquals("1 day", TimeUtil.timeToString(60 * 60 * 24));
        assertEquals("1 day 2 seconds", TimeUtil.timeToString(60 * 60 * 24 + 2));
        assertEquals("12 days 1 minute 5 seconds", TimeUtil.timeToString(60 * 60 * 24 * 12 + 65));
    }

}
