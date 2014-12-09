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
package org.getlwc.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class StringUtilsTest {

    @Test
    public void testEscapeMessageFormat() {
        assertEquals("Test''ing", StringUtils.escapeMessageFormat("Test'ing"));
        assertEquals("Testing", StringUtils.escapeMessageFormat("Testing"));
        assertEquals("''", StringUtils.escapeMessageFormat("'"));
    }

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
