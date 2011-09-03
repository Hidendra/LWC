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
     * Full LWC version with git commit & CI build (unless manually built.)
     */
    public static String FULL_VERSION;

    /**
     * LWC's version.
     * <p/>
     * Initialized to bogus value, but it will be set properly once the plugin starts up based
     * on the version listed in plugin.xml.
     */
    public static double VERSION;

    /**
     * Rather than managing the version in multiple spots, I added this method which will be
     * invoked from Plugin startup to set the version, which is pulled from the plugin.xml file.
     *
     * @param version
     * @author morganm
     */
    public static void setVersion(String version) {
        try {
            // account for dev bilds
            if (version.endsWith("-dev")) {
                version = version.substring(0, version.length() - "-dev".length());
            }

            VERSION = Double.parseDouble(version);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        String versionString = LWCPlugin.class.getPackage().getImplementationVersion();

        // if it's not a manual build, prepend a b
        if(!versionString.equals("MANUAL")) {
            versionString = "b" + versionString;
        }

        FULL_VERSION = String.format("%.2f (%s)", VERSION, versionString);
    }
}
