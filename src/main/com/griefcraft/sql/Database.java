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
import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.util.ConfigValues;
import com.griefcraft.util.Updater;

public abstract class Database {

	public enum Type {
		SQLite("sqlite.jar"),
		MySQL ("mysql.jar"),
		NONE  ("nil");
		
		private String driver;
		
		Type(String driver) {
			this.driver = driver;
		}
		
		public String getDriver() {
			return driver;
		}
		
	}
	
	/**
	 * @return true if connected to sqlite
	 */
	public static boolean isConnected() {
		return connected;
	}
	
	/**
	 * The default database engine being used. This is set via config
	 * 
	 * @default SQLite
	 */
	public static Type DefaultType = Type.NONE;

	/**
	 * Logging object
	 */
	protected Logger logger = Logger.getLogger(getClass().getSimpleName());

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
	 * The database engine being used for this connection
	 */
	public Type currentType;
	
	/**
	 * If we are connected to sqlite
	 */
	private static boolean connected = false;
	
	public Database() {
		currentType = DefaultType;
	}
	
	public Database(Type currentType) {
		this.currentType = currentType;
	}
	
	/**
	 * @return the database engine type
	 */
	public Type getType() {
		return currentType;
	}
	
	/**
	 * @return the connection to the database
	 */
	public Connection getConnection() {
		return connection;
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
	 * Connect to MySQL
	 * 
	 * @return if the connection was succesful
	 */
	public boolean connect() throws Exception {
		if (connection != null) {
			return true;
		}
		
		// load the database jar
		URLClassLoader classLoader = new URLClassLoader(new URL[] {
				new URL("jar:file:" + new File(Updater.DEST_LIBRARY_FOLDER + "lib/" + currentType.getDriver()).getAbsolutePath() + "!/")
		});

		// log(classLoader.getURLs()[0].getPath());
		
		// load and register the driver
		// Driver driver = (Driver) Class.forName("org.sqlite.JDBC", true, classLoader).newInstance();
		String className = "";
		
		if(currentType == Type.MySQL) {
			className = "com.mysql.jdbc.Driver";
		} else {
			className = "org.sqlite.JDBC";
		}
		
		Driver driver = (Driver) classLoader.loadClass(className).newInstance();
		DriverManager.registerDriver(new DriverStub(driver));
		
		Properties properties = new Properties();
		
		// if we're using mysql, append the database info
		if(currentType == Type.MySQL) {
			properties.put("autoReconnect", "true");
			properties.put("user", ConfigValues.MYSQL_USER.getString());
			properties.put("password", ConfigValues.MYSQL_PASS.getString());
		}

		connection = DriverManager.getConnection("jdbc:" + currentType.toString().toLowerCase() + ":" + getDatabasePath(), properties);
		connected = true;

		return true;
	}

	/**
	 * @return the path where the database file should be saved
	 */
	public String getDatabasePath() {
		if(currentType == Type.MySQL) {
			return "//" + ConfigValues.MYSQL_HOST.getString() + ":" + ConfigValues.MYSQL_PORT.getString() + "/" + ConfigValues.MYSQL_DATABASE.getString();
		}
		
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
		logger.log(str);
	}
	
	public void log(String str, Level level) {
		logger.log(str, level);
	}

}
