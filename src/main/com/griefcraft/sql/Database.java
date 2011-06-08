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
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import com.griefcraft.logging.Logger;
import com.griefcraft.lwc.LWC;
import com.griefcraft.util.Updater;
import com.griefcraft.util.config.Configuration;
import org.bukkit.Bukkit;

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
	private boolean connected = false;

	public Database() {
		currentType = DefaultType;
	}

	public Database(Type currentType) {
		this.currentType = currentType;
	}
	
	/**
	 * Print an exception to stdout
	 * 
	 * @param exception
	 */
	protected void printException(Exception exception) {
		
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
        ClassLoader classLoader;

        if(currentType == Type.SQLite) {
		    classLoader = new URLClassLoader(new URL[] { new URL("jar:file:" + new File(Updater.DEST_LIBRARY_FOLDER + "lib/" + currentType.getDriver()).getPath() + "!/") });
        } else {
            classLoader = Bukkit.getServer().getClass().getClassLoader();
        }

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
			LWC lwc = LWC.getInstance();
			properties.put("autoReconnect", "true");
			properties.put("user", lwc.getConfiguration().getString("database.username"));
			properties.put("password", lwc.getConfiguration().getString("database.password"));
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
		
		connection = null;
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
		Configuration lwcConfiguration = LWC.getInstance().getConfiguration();
		
		if (currentType == Type.MySQL) {
			return "//" + lwcConfiguration.getString("database.host") + "/" + lwcConfiguration.getString("database.database");
		}

		return lwcConfiguration.getString("database.path");
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
	 * Called after a statement is prepared
	 */
	protected void postPrepare() {
		
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
			postPrepare();
			return statementCache.get(sql);
		}

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			statementCache.put(sql, preparedStatement);
			postPrepare();

			return preparedStatement;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Add a column to a table
	 * 
	 * @param table
	 * @param column
	 */
	public boolean addColumn(String table, String column, String type) {
		return executeUpdateNoException("ALTER TABLE " + table + " ADD " + column + " " + type);
	}
	
	/**
	 * Rename a table
	 * 
	 * @param table
	 * @param newName
	 */
	public boolean renameTable(String table, String newName) {
		return executeUpdateNoException("ALTER TABLE " + table + " RENAME TO " + newName);
	}
	
	/**
	 * Drop a table
	 * 
	 * @param table
	 */
	public boolean dropTable(String table) {
		return executeUpdateNoException("DROP TABLE " + table);
	}

	/**
	 * Execute an update, ignoring any exceptions
	 * 
	 * @param query
	 * @return true if an exception was thrown
	 */
	public boolean executeUpdateNoException(String query) {
		Statement statement = null;
		boolean exception = false;

		try {
			statement = connection.createStatement();
			statement.executeUpdate(query);
		} catch (SQLException e) {
            exception = true;
		} finally {
			try {
				if(statement != null) {
					statement.close();
				}
			} catch(SQLException e) { }
		}
		
		return exception;
	}
	
	/**
	 * @return true if connected to the database
	 */
	public boolean isConnected() {
		return connected;
	}

}
