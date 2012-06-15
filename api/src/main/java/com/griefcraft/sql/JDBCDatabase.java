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

import com.griefcraft.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.world.Location;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCDatabase implements Database {

    public enum Driver {

        /**
         * MySQL, a proven RDBMS!
         */
        MYSQL("com.mysql.jdbc.Driver"),

        /**
         * SQLite, a stateless, serverless database format ideal for servers that cannot run a separate process for
         * e.g MySQL.
         */
        SQLITE("org.sqlite.JDBC");

        /**
         * The driver class
         */
        private String clazz;
        
        Driver (String clazz) {
            this.clazz = clazz;
        }

        /**
         * Resolve a driver name to its enum equivalent. This method is case insensitive.
         *
         * @param name
         * @return
         */
        public static Driver resolveDriver(String name) {
            for (Driver driver : values()) {
                if (driver.toString().equalsIgnoreCase(name)) {
                    return driver;
                }
            }

            return null;
        }

    }

    /**
     * LWC instance
     */
    private LWC lwc;

    /**
     * The connection to the database
     */
    private Connection connection;

    /**
     * The connection details to the server
     */
    private final JDBCConnectionDetails details;

    public JDBCDatabase(LWC lwc, JDBCConnectionDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("Connection details cannot be null");
        }

        this.details = details;
    }

    public boolean isConnected() {
        try {
            return connection == null || connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean connect() throws DatabaseException {
        Driver driver = details.getDriver();

        // Get the path to the database
        String databasePath;

        // For SQLite the 'database' value will be the path to the sqlite database
        // for MySQL and other RDBMS this will be the name of the database in the server
        // and also the hostname
        if (driver == Driver.SQLITE) {
            databasePath = details.getDatabase();
        } else {
            databasePath = "//" + details.getHostname() + "/" + details.getDatabase();
        }

        // Create the connection string
        String connectionString = "jdbc:" + driver.toString().toLowerCase() + ":" + databasePath;

        // Create the properties object
        // This will generally include connection details e.g username and password and also settings
        Properties properties = new Properties();

        // For MySQL, append our connection specific details
        if (driver == Driver.MYSQL) {
            properties.put("autoReconnect", "true"); // Auto reconnect in the face of connection loss
            properties.put("user", details.getUsername());
            properties.put("password", details.getPassword());
        }

        // Now we can finally [try to] connect to the database
        try {
            connection = DriverManager.getConnection(connectionString, properties);
            return true;
        } catch (SQLException e) {
            // Rethrow the exception as our own
            throw new DatabaseException("Exception occurred while connecting to the database!", e);
        }
    }

    /**
     * Handle an exception thrown by this class
     *
     * @param e
     */
    private void handleException(SQLException e) {
        // TOOD
        e.printStackTrace();
    }

    /**
     * Prepare an insert query
     *
     * @param table
     * @param query
     * @return
     * @throws SQLException
     */
    private PreparedStatement prepareInsertQuery(String table, String query) throws SQLException {
        return connection.prepareStatement("INSERT INTO " + details.getPrefix() + table + " " + query);
    }

    /**
     * Prepare a select query
     *
     * @param table
     * @param columns
     * @param query
     * @return
     * @throws SQLException
     */
    private PreparedStatement prepareSelectQuery(String table, String columns, String query) throws SQLException {
        return connection.prepareStatement("SELECT " + columns + " FROM " + details.getPrefix() + table + " " + query);
    }

    public Protection createProtection(Protection.Type type, String owner, Location location) {
        try {
            PreparedStatement statement = prepareInsertQuery("protections", "(type, owner, world, x, y, z, updated, created) VALUES (?, ?, ?, ?, ?, ?, UNIX_TIMESTAMP(), UNIX_TIMESTAMP())");

            try {
                statement.setInt(1, type.ordinal());
                statement.setString(2, owner);
                statement.setString(3, location.getWorld().getName());
                statement.setInt(4, location.getBlockX());
                statement.setInt(5, location.getBlockY());
                statement.setInt(6, location.getBlockZ());
                statement.executeUpdate();
            } finally {
                statement.close();
            }
        } catch (SQLException e) {
            handleException(e);
        }

        return loadProtection(location);
    }

    public Protection loadProtection(Location location) {
        try {
            PreparedStatement statement = prepareSelectQuery("protections", "id, type, owner, world, x, y, z, updated, created", "WHERE x = ? AND y = ? AND z = ? AND world = ?");

            try {
                statement.setInt(1, location.getBlockX());
                statement.setInt(2, location.getBlockY());
                statement.setInt(3, location.getBlockZ());
                statement.setString(4, location.getWorld().getName());

                ResultSet set = statement.executeQuery();

                try {
                    if (set.next()) {
                        return resolveProtection(set);
                    }
                } finally {
                    set.close();
                }
            } finally {
                statement.close();
            }
        } catch (SQLException e) {
            handleException(e);
        }

        return null;
    }

    public Protection loadProtection(int id) {
        try {
            PreparedStatement statement = prepareSelectQuery("protections", "id, type, owner, world, x, y, z, updated, created", "WHERE id = ?");

            try {
                statement.setInt(1, id);

                ResultSet set = statement.executeQuery();

                try {
                    return resolveProtection(set);
                } finally {
                    set.close();
                }
            } finally {
                statement.close();
            }
        } catch (SQLException e) {
            handleException(e);
        }

        return null;
    }

    public void saveProtection(Protection protection) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void removeProtection(Protection protection) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Resolve a protection from a result set
     *
     * @param set
     * @return
     */
    private Protection resolveProtection(ResultSet set) throws SQLException {
        Protection protection = new Protection(set.getInt("id"));
        protection.setType(Protection.Type.values()[set.getInt("type")]);
        protection.setOwner(set.getString("owner"));
        protection.setWorld(lwc.getServerLayer().getWorld(set.getString("world")));
        protection.setX(set.getInt("x"));
        protection.setY(set.getInt("y"));
        protection.setZ(set.getInt("z"));
        protection.setUpdated(set.getInt("updated"));
        protection.setCreated(set.getInt("created"));
        return protection;
    }

    public final static class JDBCConnectionDetails {

        /**
         * The jdbc driver to connect with
         */
        private final Driver driver;

        /**
         * The connection details
         */
        private final String hostname, database, prefix, username, password;
        
        public JDBCConnectionDetails(Driver driver, String hostname, String database, String prefix, String username, String password) {
            if (driver == null) {
                throw new IllegalArgumentException("JDBC Driver cannot be null");
            }
            if (hostname == null) {
                throw new IllegalArgumentException("Hostname cannot be null");
            }
            if (database == null) {
                throw new IllegalArgumentException("Database cannot be null");
            }
            if (username == null) {
                throw new IllegalArgumentException("Username cannot be null");
            }
            if (password == null) {
                throw new IllegalArgumentException("Password cannot be null");
            }

            this.driver = driver;
            this.hostname = hostname;
            this.database = database;
            this.prefix = prefix;
            this.username = username;
            this.password = password;
        }

        /**
         * Get the jdbc driver to connect with
         *
         * @return
         */
        public Driver getDriver() {
            return driver;
        }

        /**
         * Get the hostname to connect to
         * 
         * @return
         */
        public String getHostname() {
            return hostname;
        }

        /**
         * Get the database to open
         * 
         * @return
         */
        public String getDatabase() {
            return database;
        }

        /**
         * Get the table prefix
         *
         * @return
         */
        public String getPrefix() {
            return prefix;
        }

        /**
         * Get hte username to connect as
         * 
         * @return
         */
        public String getUsername() {
            return username;
        }

        /**
         * Get the password to connect with
         *
         * @return
         */
        public String getPassword() {
            return password;
        }
        
    }

}
