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

package com.griefcraft.lwc;

/**
 * Temporary, just need to get version info, etc into a packaged class
 */
public class LWCInfo {

	/**
	 * Enable/disable dev mode
	 */
	public static final boolean DEVELOPMENT = false;

	/**
	 * LWC's version
	 */
	public static final double VERSION = 1.49;
	
	/**
	 * Full LWC version
	 */
	public static final String FULL_VERSION = String.format("%.2f", VERSION);

	/**
	 * Location of the properties file relative to the root Minecraft directory
	 */
	public static final String CONF_FILE = "plugins/LWC/lwc.properties";

}
