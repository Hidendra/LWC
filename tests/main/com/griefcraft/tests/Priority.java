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

package com.griefcraft.tests;

public enum Priority {

	/**
	 * Test is not very critical and should not take precedence
	 */
	LOW,
	
	/**
	 * Normal test level, for generally most things
	 */
	NORMAL,
	
	/**
	 * Higher test level, should be performed before everything else
	 */
	HIGH,
	
	/**
	 * Absolutely must be executed first
	 * This may be used to set up initial data structures that will be used throughout many tests
	 */
	CRITICAL,
	
	/**
	 * Will be executed whenever read
	 */
	WILDCARD;
	
}
