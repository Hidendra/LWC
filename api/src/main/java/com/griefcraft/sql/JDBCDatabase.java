/*
 * Copyright (c) 2011, 2012, Tyler Blair
 * All rights reserved.
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

import com.griefcraft.Engine;
import com.griefcraft.model.Protection;
import com.griefcraft.model.Role;
import com.griefcraft.world.Location;
import snaq.db.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
         * Get the driver's class name
         *
         * @return
         */
        public String getClassName() {
            return clazz;
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
     * LWC engine instance
     */
    private Engine engine;

    /**
     * The connection pool
     */
    private ConnectionPool pool = null;

    /**
     * The connection details to the server
     */
    private final JDBCConnectionDetails details;

    public JDBCDatabase(Engine engine, JDBCConnectionDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("Connection details cannot be null");
        }

        this.engine = engine;
        this.details = details;
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

        // setup the database pool
        pool = new ConnectionPool("lwc", 2, 15, 15, 180000, connectionString, details.getUsername(), details.getPassword());
        pool.setCaching(true);
        pool.init();

        Connection connection = null;
        Statement stmt = null;
        try {
            connection = pool.getConnection();
            stmt = connection.createStatement();
            stmt.executeQuery("SELECT 1;");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            safeClose(stmt);
            safeClose(connection);
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
     * Safely close a {@link Connection} object
     *
     * @param conn
     */
    private void safeClose(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            try {
                conn.close();
            } catch (SQLException ex) { }
        }
    }

    /**
     * Safely close a {@link Statement} object
     *
     * @param stmt
     */
    private void safeClose(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            try {
                stmt.close();
            } catch (SQLException ex) { }
        }
    }

    public Protection createProtection(String owner, Location location) {
        try {
            Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + details.getPrefix() + "protections (world, x, y, z, updated, created, accessed) VALUES (?, ?, ?, ?, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), UNIX_TIMESTAMP())");

            try {
                statement.setString(1, location.getWorld().getName());
                statement.setInt(2, location.getBlockX());
                statement.setInt(3, location.getBlockY());
                statement.setInt(4, location.getBlockZ());
                statement.executeUpdate();
            } finally {
                safeClose(statement);
                safeClose(connection);
            }
        } catch (SQLException e) {
            handleException(e);
        }

        return loadProtection(location);
    }

    public Protection loadProtection(Location location) {
        try {
            Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT id, world, x, y, z, updated, created, accessed FROM " + details.getPrefix() + "protections WHERE x = ? AND y = ? AND z = ? AND world = ?");

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
                safeClose(statement);
                safeClose(connection);
            }
        } catch (SQLException e) {
            handleException(e);
        }

        return null;
    }

    public Protection loadProtection(int id) {
        try {
            Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT id, world, x, y, z, updated, created, accessed FROM " + details.getPrefix() + "protections WHERE id = ?");

            try {
                statement.setInt(1, id);

                ResultSet set = statement.executeQuery();

                try {
                    return resolveProtection(set);
                } finally {
                    set.close();
                }
            } finally {
                safeClose(statement);
                safeClose(connection);
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

    public void saveRole(Role role) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void removeRole(Role role) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Resolve a protection from a result set
     *
     * @param set
     * @return
     */
    private Protection resolveProtection(ResultSet set) throws SQLException {
        Protection protection = new Protection(engine, set.getInt("id"));
        protection.setWorld(engine.getServerLayer().getWorld(set.getString("world")));
        protection.setX(set.getInt("x"));
        protection.setY(set.getInt("y"));
        protection.setZ(set.getInt("z"));
        protection.setUpdated(set.getInt("updated"));
        protection.setCreated(set.getInt("created"));
        protection.setAccessed(set.getInt("accessed"));
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
