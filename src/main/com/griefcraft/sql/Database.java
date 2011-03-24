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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import com.griefcraft.logging.Logger;
import com.griefcraft.util.ConfigValues;
import com.griefcraft.util.Updater;

public abstract class Database {

	public enum Type {
		MySQL("mysql.jar"), //
		SQLite("sqlite.jar"), //
		NONE("nil"); //

		private String driver;

		Type(String driver) {
			this.driver = driver;
		}

		public String getDriver() {
			return driver;
		}

	}

	/**
	 * The database engine being used for this connection
	 */
	public Type currentType;

	/**
	 * Store cached prepared statements.
	 * 
	 * Since SQLite JDBC doesn't cache them.. we do it ourselves :S
	 */
	private Map<String, PreparedStatement> statementCache = new HashMap<String, PreparedStatement>();

	/**
	 * The connection to the database
	 */
	protected Connection connection = null;

	/**
	 * Logging object
	 */
	protected Logger logger = Logger.getLogger(getClass().getSimpleName());

	/**
	 * The default database engine being used. This is set via config
	 * 
	 * @default SQLite
	 */
	public static Type DefaultType = Type.NONE;

	/**
	 * If we are connected to sqlite
	 */
	private static boolean connected = false;

	/**
	 * The Database driver used
	 */
	private static Driver driver;

	/**
	 * The Database driver class
	 */
	private static Class<?> driverClass;

	public Database() {
		currentType = DefaultType;
	}

	public Database(Type currentType) {
		this.currentType = currentType;
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
		
		if(currentType == null || currentType == Type.NONE) {
			log("Invalid database engine");
			return false;
		}

		// load the database jar
		URLClassLoader classLoader = new URLClassLoader(new URL[] { new URL("jar:file:" + new File(Updater.DEST_LIBRARY_FOLDER + "lib/" + currentType.getDriver()).getAbsolutePath() + "!/") });
		// DatabaseClassLoader classLoader = DatabaseClassLoader.getInstance(new URL("jar:file:" + new File(Updater.DEST_LIBRARY_FOLDER + "lib/" + currentType.getDriver()).getAbsolutePath() + "!/"));
		
		String className = "";
		if (currentType == Type.MySQL) {
			className = "com.mysql.jdbc.Driver";
		} else {
			className = "org.sqlite.JDBC";
		}

		Driver driver = (Driver) classLoader.loadClass(className).newInstance();
		DriverManager.registerDriver(new DriverStub(driver));

		Properties properties = new Properties();

		// if we're using mysql, append the database info
		if (currentType == Type.MySQL) {
			properties.put("autoReconnect", "true");
			properties.put("user", ConfigValues.MYSQL_USER.getString());
			properties.put("password", ConfigValues.MYSQL_PASS.getString());
		}

		connection = DriverManager.getConnection("jdbc:" + currentType.toString().toLowerCase() + ":" + getDatabasePath(), properties);
		connected = true;

		return true;
	}

	public void dispose() {
		statementCache.clear();

		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		driverClass = null;
		driver = null;
		connection = null;
		
		System.gc();
	}

	/**
	 * @return the connection to the database
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * @return the path where the database file should be saved
	 */
	public String getDatabasePath() {
		if (currentType == Type.MySQL) {
			return "//" + ConfigValues.MYSQL_HOST.getString() + ":" + ConfigValues.MYSQL_PORT.getString() + "/" + ConfigValues.MYSQL_DATABASE.getString();
		}

		return ConfigValues.DB_PATH.getString();
	}

	/**
	 * @return the database engine type
	 */
	public Type getType() {
		return currentType;
	}

	public abstract void load();

	/**
	 * Log a string to stdout
	 * 
	 * @param str
	 *            The string to log
	 */
	public void log(String str) {
		logger.log(str);
	}

	public void log(String str, Level level) {
		logger.log(str, level);
	}

	/**
	 * Prepare a statement unless it's already cached (and if so, just return it)
	 * 
	 * @param sql
	 * @return
	 */
	public PreparedStatement prepare(String sql) {
		if (connection == null) {
			return null;
		}

		if (statementCache.containsKey(sql)) {
			return statementCache.get(sql);
		}

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			statementCache.put(sql, preparedStatement);

			return preparedStatement;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * @return true if connected to sqlite
	 */
	public static boolean isConnected() {
		return connected;
	}

}
