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
     * Location of the properties file relative to the root Minecraft directory
     */
    public static final String CONF_FILE = "plugins/LWC/lwc.properties";

    /**
     * Full LWC version
     */
    public static String FULL_VERSION = "-1";

    /**
     * LWC's version.
     * 
     * Initialized to bogus value, but it will be set properly once the plugin starts up based
     * on the version listed in plugin.xml.
     */
    public static double VERSION = -1;

    /** Rather than managing the version in multiple spots, I added this method which will be
     * invoked from Plugin startup to set the version, which is pulled from the plugin.xml file.
     * 
     * @param version
     * @author morganm
     */
    public static void setVersion(String version) {
    	try {
    		VERSION = Double.parseDouble(version);
    	}
    	catch(NumberFormatException e) {
    		e.printStackTrace();
    	}
    	
    	FULL_VERSION = version;
    }
}
