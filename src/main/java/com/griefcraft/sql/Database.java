/*
 * Copyright 2011 Tyler Blair. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package com.griefcraft.sql;


import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.ModuleException;
import com.griefcraft.util.Statistics;
import com.griefcraft.util.Updater;
import com.griefcraft.util.config.Configuration;
import org.bukkit.Bukkit;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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

        /**
         * Match the given string to a database type
         *
         * @param str
         * @return
         */
        public static Type matchType(String str) {
            for (Type type : values()) {
                if (type.toString().equalsIgnoreCase(str)) {
                    return type;
                }
            }

            return null;
        }

    }

    /**
     * The database engine being used for this connection
     */
    public Type currentType;

    /**
     * Store cached prepared statements.
     * <p/>
     * Since SQLite JDBC doesn't cache them.. we do it ourselves :S
     */
    private Map<String, PreparedStatement> statementCache = new HashMap<String, PreparedStatement>();

    /**
     * The connection to the database
     */
    protected Connection connection = null;

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

    /**
     * If the database has been loaded
     */
    protected boolean loaded = false;

    /**
     * The database prefix (only if we're using MySQL.)
     */
    protected String prefix = "";

    /**
     * If the high level statement cache should be used. If this is false, already cached statements are ignored
     */
    private boolean useStatementCache = true;

    public Database() {
        currentType = DefaultType;

        prefix = LWC.getInstance().getConfiguration().getString("database.prefix", "");
        if (prefix == null) {
            prefix = "";
        }
    }

    public Database(Type currentType) {
        this();
        this.currentType = currentType;
    }

    /**
     * Ping the database to keep the connection alive
     */
    public void pingDatabase() {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.executeQuery("SELECT 1;");
            stmt.close();
        } catch (SQLException e) {
            log("Keepalive packet (ping) failed!");
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) { }
        }
    }

    /**
     * Set the value of auto commit
     *
     * @param autoCommit
     * @return TRUE if successful, FALSE if exception was thrown
     */
    public boolean setAutoCommit(boolean autoCommit) {
        try {
            // Commit the database if we are setting auto commit back to true
            if (autoCommit) {
                connection.commit();
            }

            connection.setAutoCommit(autoCommit);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @return the table prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Print an exception to stdout
     *
     * @param exception
     */
    protected void printException(Exception exception) {
        throw new ModuleException(exception);
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

        if (currentType == null || currentType == Type.NONE) {
            log("Invalid database engine");
            return false;
        }

        // load the database jar
        ClassLoader classLoader;

        if (currentType == Type.SQLite) {
            classLoader = new URLClassLoader(new URL[]{new URL("jar:file:" + new File(Updater.DEST_LIBRARY_FOLDER + currentType.getDriver()).getPath() + "!/")});
        } else {
            classLoader = Bukkit.getServer().getClass().getClassLoader();
        }

        // What class should we try to load?
        String className = "";
        if (currentType == Type.MySQL) {
            className = "com.mysql.jdbc.Driver";
        } else {
            className = "org.sqlite.JDBC";
        }

        // Load the driver class
        Driver driver = (Driver) classLoader.loadClass(className).newInstance();

        // Create the properties to pass to the driver
        Properties properties = new Properties();

        // if we're using mysql, append the database info
        if (currentType == Type.MySQL) {
            LWC lwc = LWC.getInstance();
            properties.put("autoReconnect", "true");
            properties.put("user", lwc.getConfiguration().getString("database.username"));
            properties.put("password", lwc.getConfiguration().getString("database.password"));
        }

        // Connect to the database
        try {
            connection = driver.connect("jdbc:" + currentType.toString().toLowerCase() + ":" + getDatabasePath(), properties);
            connected = true;
            return true;
        } catch (SQLException e) {
            log("Failed to connect to " + currentType + ": " + e.getErrorCode() + " - " + e.getMessage());
            return false;
        }
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

    /**
     * Load the database
     */
    public abstract void load();

    /**
     * Log a string to stdout
     *
     * @param str The string to log
     */
    public void log(String str) {
        LWC.getInstance().log(str);
    }

    /**
     * Prepare a statement unless it's already cached (and if so, just return it)
     *
     * @param sql
     * @return
     */
    public PreparedStatement prepare(String sql) {
        return prepare(sql, false);
    }

    /**
     * Prepare a statement unless it's already cached (and if so, just return it)
     *
     * @param sql
     * @param returnGeneratedKeys
     * @return
     */
    public PreparedStatement prepare(String sql, boolean returnGeneratedKeys) {
        if (connection == null) {
            return null;
        }

        if (useStatementCache && statementCache.containsKey(sql)) {
            Statistics.addQuery();
            return statementCache.get(sql);
        }

        try {
            PreparedStatement preparedStatement;

            if (returnGeneratedKeys) {
                preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            } else {
                preparedStatement = connection.prepareStatement(sql);
            }

            statementCache.put(sql, preparedStatement);
            Statistics.addQuery();

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
     * Add a column to a table
     *
     * @param table
     * @param column
     */
    public boolean dropColumn(String table, String column) {
        return executeUpdateNoException("ALTER TABLE " + table + " DROP COLUMN " + column);
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
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
            }
        }

        return exception;
    }

    /**
     * @return true if connected to the database
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Returns true if the high level statement cache should be used. If this is false, already cached statements are ignored
     *
     * @return
     */
    public boolean useStatementCache() {
        return useStatementCache;
    }

    /**
     * Set if the high level statement cache should be used.
     *
     * @param useStatementCache
     * @return
     */
    public void setUseStatementCache(boolean useStatementCache) {
        this.useStatementCache = useStatementCache;
    }

}
