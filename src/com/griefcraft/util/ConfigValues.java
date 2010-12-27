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

/**
 * This is the class you would edit to add new config values to lwc.properties - NO others !!
 * 
 * Config.class DEPENDS on the listed enums in this class. If you want to get the current value for BLACKLISTED_MODES, do
 * <p>
 * ConfigValues.BLACKLISTED_MODES.getValue();
 * </p>
 * 
 * If you use a value a lot, you can import it statically in your class like so:
 * <p>
 * import static com.griefcraft.util.ConfigValues.BLACKLISTED_MODES; // or *, but not recommended
 * 
 * [...] BLACKLISTED_MODES.getValue();
 * </p>
 * 
 * 
 * Create new config values by creating a new enum:
 * <p>
 * DECLARATION("simple-name", "default-value")
 * </p>
 * 
 * Keep declarations obvious and in caps. Simple names shouldn't be too descriptive but specific enough
 */
public enum ConfigValues {

	// TODO: furnace protection

	BLACKLISTED_MODES("blacklisted-modes", ""), ALLOW_FURNACE_PROTECTION("furnace-locks", "true");

	/**
	 * The name of the config value used in the conf file
	 */
	private String name;

	/**
	 * The default value
	 */
	private String defaultValue;

	private ConfigValues(String name, String defaultValue) {
		this.name = name;
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the current value in boolean form
	 */
	public boolean getBool() {
		return getString().equalsIgnoreCase("true");
	}

	/**
	 * @return defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return the current value in integer form
	 */
	public int getInt() {
		return Integer.parseInt(getString());
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the current value (NOT the default !!)
	 */
	public String getString() {
		return Config.getInstance().getProperty(name, defaultValue);
	}

}
