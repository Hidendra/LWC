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
import snaq.db.ConnectionPool;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

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
     * The default database engine being used. This is set via config
     *
     * @default SQLite
     */
    public static Type DefaultType = Type.NONE;

    /**
     * The connection pool
     */
    private ConnectionPool pool;

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
     * Safe a connection safely by not leaking connection objects
     */
    protected void safeClose(PreparedStatement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (Exception e) {
                try {
                    statement.close();
                } catch (Exception ex) { }
            }
        }
        // TODO force close in the connection pool if possible
    }

    /**
     * Create a connection in the database pool
     *
     * @return
     */
    public Connection createConnection() {
        try {
            return pool.getConnection();
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Connect to MySQL
     *
     * @return if the connection was succesful
     */
    public boolean connect() throws Exception {
        if (pool != null) {
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
        classLoader.loadClass(className).newInstance();
        try {
            Class.forName(className);
        } catch (Exception e) { }

        // Create the pool
        LWC lwc = LWC.getInstance();
        String connectionString = "jdbc:" + currentType.toString().toLowerCase() + ":" + getDatabasePath();
        if (currentType == Type.MySQL) {
            pool = new ConnectionPool("lwc", 2 /* minPool */, 15 /* maxPool */, 15 /* maxSize */, 180000 /* idleTimeout */,
                    connectionString, lwc.getConfiguration().getString("database.username"), lwc.getConfiguration().getString("database.password"));
        } else { // safe method -- only 1 connection
            pool = new ConnectionPool("lwc", 1 /* minPool */, 1 /* maxPool */, 1 /* maxSize */, 180000 /* idleTimeout */,
                    connectionString, lwc.getConfiguration().getString("database.username"), lwc.getConfiguration().getString("database.password"));
        }
        pool.setCaching(true);
        pool.init();

        // Connect to the database
        try {
            Connection connection = pool.getConnection();
            Statement stmt = connection.createStatement();
            stmt.executeQuery("SELECT 1;");
            stmt.close();
            connection.close();
            return true;
        } catch (SQLException e) {
            log("###########################");
            log("###########################");
            log("### NOTE: LWC has failed to connect to the database (" + currentType + ")");
            log("###       The error received from the server is: \"" + e.getMessage() + "\"");
            log("###");
            log("### RESOLVING: Once you have fixed this error, simply restart the server again");
            log("###########################");
            log("###########################");
            connected = false;
            return false;
        }
    }

    public void dispose() {
        if (pool != null) {
            pool.release();
        }
        pool = null;
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
        try {
            Connection connection = createConnection();
            PreparedStatement preparedStatement;

            if (connection == null) {
                pool.flush();
                connection = createConnection();
                log("Flushed database pool");
            }

            if (returnGeneratedKeys) {
                preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            } else {
                preparedStatement = connection.prepareStatement(sql);
            }

            Statistics.addQuery();

            return new AutoClosingPreparedStatement(connection, preparedStatement);
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
        Connection connection = createConnection();
        Statement statement = null;
        boolean exception = false;

        if (connection == null) {
            return true; // could not create
        }

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
                connection.close();
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

}
