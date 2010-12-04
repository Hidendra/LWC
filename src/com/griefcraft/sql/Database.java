package com.griefcraft.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public abstract class Database {

	/**
	 * The connection to the database
	 */
	public Connection connection = null;

	/**
	 * Connect to MySQL
	 * 
	 * @return if the connection was succesful
	 */
	public boolean connect() {
		if(connection != null) {
			return true;
		}
		
		try {
			Class.forName("org.sqlite.JDBC");

			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ getDatabasePath());
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * @return the path where the database file should be saved
	 */
	public String getDatabasePath() {
		try {
			String path = Database.class.getProtectionDomain().getCodeSource()
					.getLocation().toURI().getPath();

			if (path.endsWith(".jar") || path.endsWith("/")) {
				path = path.substring(0, path.lastIndexOf("/"));
			}
			path = path.substring(0, path.lastIndexOf("/"));

			return path + File.separator + "lwc.db";
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public abstract void load();

	/**
	 * Log a string to stdout
	 * 
	 * @param str
	 *            The string to log
	 */
	public void log(String str) {
		System.out.println("[LWC->sqlite] " + str);
	}

}
