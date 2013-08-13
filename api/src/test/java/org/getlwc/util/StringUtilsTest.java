package org.getlwc.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class StringUtilsTest {

    @Test
    public void testCapitalizeFirstLetter() {
        assertEquals("The Best Test In The World", StringUtils.capitalizeFirstLetter("the best teSt in the world"));
    }

    @Test
    public void testSplit() {
        String[] split = StringUtils.split("This is a \"parsed string\" with 'quotation marks'");

        assertEquals(6, split.length);
        assertArrayEquals(new String[]{"This", "is", "a", "parsed string", "with", "quotation marks"}, split);
    }

    @Test
    public void testSplitInnerQuotes() {
        String[] split = StringUtils.split("string has \"quotes with 'inner quotes'\"");

        assertArrayEquals(new String[]{"string", "has", "quotes with 'inner quotes'"}, split);
    }

    @Test
    public void testSplitInnerQuotes2() {
        String[] split = StringUtils.split("string has 'quotes with \"inner quotes\"'");

        assertArrayEquals(new String[]{"string", "has", "quotes with \"inner quotes\""}, split);
    }

    @Test
    public void testJoin() {
        String[] arr = new String[]{"test", "string", "is", "awesome"};
        assertEquals(StringUtils.join(arr, 0, ","), "test,string,is,awesome");
        assertEquals(StringUtils.join(arr, 1, ","), "string,is,awesome");
        assertEquals(StringUtils.join(arr, 2, ","), "is,awesome");
        assertEquals(StringUtils.join(arr, 3, ","), "awesome");
        assertEquals(StringUtils.join(arr, 4, ","), "");
        assertEquals(StringUtils.join(arr, 5000, ","), "");
    }

    @Test
    public void testTransform() {
        assertEquals(StringUtils.transform("secret", '*'), "******");
        assertEquals(StringUtils.transform("super_secret", 'X'), "XXXXXXXXXXXX");
    }

}
