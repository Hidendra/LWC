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

package com.griefcraft.sql;

import java.sql.Connection;
import java.sql.DriverManager;

import com.griefcraft.logging.Logger;
import com.griefcraft.util.ConfigValues;

public abstract class Database {

	/**
	 * @return true if connected to sqlite
	 */
	public static boolean isConnected() {
		return connected;
	}

	/**
	 * Logging object
	 */
	private Logger logger = Logger.getLogger(getClass().getSimpleName());

	/**
	 * The connection to the database
	 */
	protected Connection connection = null;

	/**
	 * If we are connected to sqlite
	 */
	private static boolean connected = false;
	
	/**
	 * @return the connection to the database
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Connect to MySQL
	 * 
	 * @return if the connection was succesful
	 */
	public boolean connect() throws Exception {
		if (connection != null) {
			return true;
		}

		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath());
		connected = true;

		return true;
	}

	/**
	 * @return the path where the database file should be saved
	 */
	public String getDatabasePath() {
		/*
		 * try { String path = Database.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		 * 
		 * if (path.endsWith(".jar") || path.endsWith("/")) { path = path.substring(0, path.lastIndexOf("/")); } path = path.substring(0, path.lastIndexOf("/"));
		 * 
		 * return path + File.separator + "lwc.db"; } catch (final Exception e) { e.printStackTrace(); }
		 */

		return ConfigValues.DB_PATH.getString();
	}

	public abstract void load();

	/**
	 * Log a string to stdout
	 * 
	 * @param str
	 *            The string to log
	 */
	public void log(String str) {
		logger.info(str);
	}

}
