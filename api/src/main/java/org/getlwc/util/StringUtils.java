/*
 * Copyright (c) 2011-2013 Tyler Blair
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

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    /**
     * Regex for matching quoted string arguments
     */
    private final static Pattern QUOTED_STRING_REGEX = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");


    /**
     * Escape a string. Primarily, replace single quotes with double quotes.
     *
     * @param msg
     * @return
     */
    public static String escapeMessageFormat(String msg) {
        StringBuffer buffer = new StringBuffer();
        char[] chrmsg = msg.toCharArray();

        for (int i = 0; i < chrmsg.length; i++) {
            if (chrmsg[i] == '\'') {
                buffer.append("''");
            } else {
                buffer.append(chrmsg[i]);
            }
        }

        return buffer.toString();
    }

    /**
     * Capitalize the first letter in a word
     *
     * @param str
     * @return
     */
    public static String capitalizeFirstLetter(String str) {
        if (str.length() == 0) {
            return str;
        } else if (str.length() == 1) {
            return str.toUpperCase();
        }

        char[] string = str.toLowerCase().toCharArray();

        // First letter - capatalize it
        string[0] = Character.toUpperCase(string[0]);

        // scan for spaces
        for (int index = 0; index < string.length; index++) {
            if (string[index] == ' ' && (index + 1) != string.length) {
                // convert chars after found spaces to uppercase
                string[index + 1] = Character.toUpperCase(string[index + 1]);
            }
        }

        return new String(string);
    }

    /**
     * Encrypt a string using SHA1
     *
     * @param plaintext
     * @return
     */
    public static String hash(String plaintext) {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA");
            md.update(plaintext.getBytes("UTF-8"));

            final byte[] raw = md.digest();
            return byteArray2Hex(raw);
        } catch (final Exception e) {

        }

        return "";
    }

    /**
     * Splits a string, leaving quoted messages ("one argument") in the same argument.
     * Courtesy http://stackoverflow.com/questions/366202/
     *
     * @param str
     * @return
     */
    public static String[] split(String str) {
        List<String> matchList = new ArrayList<String>();
        Matcher regexMatcher = QUOTED_STRING_REGEX.matcher(str);

        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                // Add double-quoted string without the quotes
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                // Add single-quoted string without the quotes
                matchList.add(regexMatcher.group(2));
            } else {
                // Add unquoted word
                matchList.add(regexMatcher.group());
            }
        }

        return matchList.toArray(new String[matchList.size()]);
    }

    /**
     * Join an array into a String, where the array values are delimited by spaces.
     *
     * @param arr
     * @return
     */
    public static String join(String[] arr) {
        return join(arr, 0);
    }

    /**
     * Join an array into a String, where the array values are delimited by spaces, starting at the given offset.
     *
     * @param arr
     * @param offset
     * @return
     */
    public static String join(String[] arr, int offset) {
        return join(arr, offset, " ");
    }

    /**
     * Join an array into a String, where the array values are delimited by the given string, starting at the given offset.
     *
     * @param arr
     * @param offset
     * @param delim
     * @return
     */
    public static String join(String[] arr, int offset, String delim) {
        String str = "";

        if (arr == null || arr.length == 0) {
            return str;
        }

        for (int i = offset; i < arr.length; i++) {
            str += arr[i];

            if (i + 1 != arr.length) {
                str += delim;
            }
        }

        return str.trim();
    }

    /**
     * Transform a string into one char
     *
     * @param str The string to transform
     * @param chr The char to transform all chars to (ie '*')
     * @return the transformed string
     */
    public static String transform(String str, char chr) {
        char[] charArray = str.toCharArray();

        for (int i = 0; i < charArray.length; i++) {
            charArray[i] = chr;
        }

        return new String(charArray);
    }

    /**
     * Convert a byte array to hex
     *
     * @param hash the hash to convert
     * @return the converted hash
     */
    private static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (final byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

}