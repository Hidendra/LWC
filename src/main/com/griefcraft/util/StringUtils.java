/**
 * This file is part of LWC (https://github.com/Hidendra/LWC)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.griefcraft.util;

import java.security.MessageDigest;
import java.util.Formatter;

public class StringUtils {

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

		/*
		 * First cast it all to lower case and get the char array
		 */
		char[] string = str.toLowerCase().toCharArray();

		/*
		 * First letter -- capitalize it
		 */
		string[0] = Character.toUpperCase(string[0]);

		/*
		 * Scan for spaces
		 */
		for (int index = 0; index < string.length; index++) {
			if (string[index] == ' ' && index != string.length) {
				/*
				 * convert chars after spaces to upper case
				 */
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
	public static String encrypt(String plaintext) {
		MessageDigest md = null;

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
	 * Check if the command has the correct flag
	 * 
	 * @param args
	 * @param checkFlag
	 * @return
	 */
	public static boolean hasFlag(String[] args, String checkFlag) {
		String flag = args[0].toLowerCase();
		return flag.equals(checkFlag) || flag.equals("-" + checkFlag);
	}

	/**
	 * Join an array command into a String
	 * 
	 * @param arr
	 * @param offset
	 * @return
	 */
	public static String join(String[] arr, int offset) {
		String str = "";

		if (arr == null || arr.length == 0) {
			return str;
		}

		for (int i = offset; i < arr.length; i++) {
			str += arr[i] + " ";
		}

		return str.trim();
	}

	/**
	 * Transform a string into one char
	 * 
	 * @param str
	 *            The string to transform
	 * @param chr
	 *            The char to transform all chars to (ie '*')
	 * @return the transformed string
	 */
	public static String transform(String str, char chr) {
		final char[] charArray = str.toCharArray();

		for (int i = 0; i < charArray.length; i++) {
			charArray[i] = chr;
		}

		return new String(charArray);
	}

	/**
	 * Convert a byte array to hex
	 * 
	 * @param hash
	 *            the hash to convert
	 * @return the converted hash
	 */
	private static String byteArray2Hex(byte[] hash) {
		final Formatter formatter = new Formatter();
		for (final byte b : hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

}
