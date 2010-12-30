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

		String firstLetter = str.substring(0, 1);
		String endLetters = str.substring(1);

		return firstLetter.toUpperCase() + endLetters.toLowerCase();
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

}
