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

public enum ConfigValues {

	// ALLOW_FURNACE_PROTECTION("furnace-locks", "true"), //
	ALLOW_BLOCK_DESTRUCTION("allow-block-destruction", "false"), //
	AUTO_REGISTER("auto-register-as", "none"), // public, private
	AUTO_UPDATE("auto-update", "false"), //
	BLACKLISTED_MODES("blacklisted-modes", ""), //
	CACHE_SIZE("cache-size", "10000"), //
	DATABASE("database", "sqlite"), //
	DB_PATH("db-path", "plugins/LWC/lwc.db"), //
	DEFAULT_MENU_STYLE("default-menu-style", "basic"), //
	DENY_REDSTONE("deny-redstone", "false"), // on valid protections (i.e door)
	ENFORCE_WORLDGUARD_REGIONS("enforce-worldguard-regions", "false"), //
	FLUSH_DB_INTERVAL("flush-db-interval", "10"), //
	LOCALE("locale", "eng"), // the locale to use
	MAX_JOBS("max-jobs", "0"), //
	MYSQL_DATABASE("mysql-database", "lwc"), //
	MYSQL_HOST("mysql-host", "localhost"), // default mysql info
	MYSQL_PASS("mysql-pass", ""), //
	MYSQL_PORT("mysql-port", "3306"), //
	MYSQL_USER("mysql-user", "user"), //
	OP_IS_LWCADMIN("op-is-lwcadmin", "true"), //
	PROTECTION_BLACKLIST("protection-blacklist", ""), //
	SHOW_PROTECTION_NOTICES("show-protection-notices", "true"), // show "this is protected" to admins
	VERBOSE("verbose", "false"), //
	WORLDGUARD_ALLOWED_REGIONS("worldguard-allowed-regions", "*"); //

	/**
	 * The default value
	 */
	private String defaultValue;

	/**
	 * The name of the config value used in the conf file
	 */
	private String name;

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

	@Override
	public String toString() {
		return getString();
	}

}
