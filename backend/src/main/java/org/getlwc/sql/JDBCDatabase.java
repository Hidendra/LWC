/*
 * Copyright (c) 2011-2013 Tyler Blair
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

package org.getlwc.sql;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.getlwc.Engine;
import org.getlwc.Location;
import org.getlwc.ProtectionAccess;
import org.getlwc.Role;
import org.getlwc.RoleDefinition;
import org.getlwc.model.AbstractAttribute;
import org.getlwc.model.Protection;
import org.getlwc.model.State;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

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
        SQLITE("org.sqlite.JDBC"),

        /**
         * H2. http://www.h2database.com
         */
        H2("org.h2.Driver");

        /**
         * The driver class
         */
        private String clazz;

        Driver(String clazz) {
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
    private ComboPooledDataSource pool = null;

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

    /**
     * {@inheritDoc}
     */
    public boolean connect() throws DatabaseException {
        Driver driver = details.getDriver();

        // Get the path to the database
        String databasePath;

        // For SQLite the 'database' value will be the path to the sqlite database
        // for MySQL and other RDBMS this will be the name of the database in the server
        // and also the hostname
        if (driver == Driver.SQLITE || driver == Driver.H2) {
            databasePath = details.getDatabasePath().replaceAll("%home%", engine.getServerLayer().getEngineHomeFolder().getPath().replaceAll("\\\\", "/"));
        } else {
            databasePath = "//" + details.getHostname() + "/" + details.getDatabase();
        }

        // Create the connection string
        String connectionString = "jdbc:" + driver.toString().toLowerCase() + ":" + databasePath;

        // Disable c3p0 logging
        Properties prop = new Properties(System.getProperties());
        prop.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
        prop.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF"); // or any other
        System.setProperties(prop);

        // setup the database pool
        pool = new ComboPooledDataSource();

        Connection connection = null;
        Statement stmt = null;
        try {
            pool.setDriverClass(driver.getClassName());
            pool.setJdbcUrl(connectionString);
            pool.setUser(details.getUsername());
            pool.setPassword(details.getPassword());
            pool.setPreferredTestQuery("SELECT 1;");

            if (driver == Driver.H2) {
                pool.setUser("sa");
                pool.setPassword("");
            }

            verifyBase();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            safeClose(stmt);
            safeClose(connection);
        }
    }

    /**
     * Verify the base SQL. If the base tables were not created, create them
     */
    private void verifyBase() {
        boolean baseRequired;

        Connection connection = null;
        Statement stmt = null;
        try {
            connection = pool.getConnection();
            stmt = connection.createStatement();
            stmt.executeQuery("SELECT 1 FROM " + details.getPrefix() + "protections");
            baseRequired = false;
        } catch (SQLException e) {
            baseRequired = true;
        } finally {
            safeClose(stmt);
            safeClose(connection);
        }

        if (baseRequired) {
            engine.getConsoleSender().sendTranslatedMessage("Creating base database via base.sql");

            executeInternalSQLFile("/sql/base/base.sql");
            executeInternalSQLFile("/sql/base/base." + details.getDriver().toString().toLowerCase() + ".sql");
        }
    }

    /**
     * Execute an internal SQL file
     *
     * @param path
     */
    private void executeInternalSQLFile(String path) {
        String file = "";

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)));
            String line;
            while ((line = reader.readLine()) != null) {
                file += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // fix the prefix
        file = file.replaceAll("__PREFIX__", details.getPrefix());

        Connection connection = null;
        try {
            // convert the SQL to a stream so we can use a (mostly) unmodified ScriptRunner class
            InputStream stream = new ByteArrayInputStream(file.getBytes("UTF-8"));

            // create a connection for our attempt
            connection = pool.getConnection();

            ScriptRunner runner = new ScriptRunner(connection, false, false);
            runner.setLogWriter(null);
            runner.runScript(new InputStreamReader(stream));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            safeClose(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void disconnect() {
        pool.close();
        pool = null;
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
            } catch (SQLException ex) {
            }
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
            } catch (SQLException ex) {
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Protection createProtection(Location location) {
        try {
            Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + details.getPrefix() + "protections (world, x, y, z, updated, created, accessed) VALUES (?, ?, ?, ?, ?, ?, ?)");

            try {
                statement.setString(1, location.getWorld().getName());
                statement.setInt(2, location.getBlockX());
                statement.setInt(3, location.getBlockY());
                statement.setInt(4, location.getBlockZ());
                int epoch = (int) (System.currentTimeMillis() / 1000L);
                statement.setInt(5, epoch);
                statement.setInt(6, epoch);
                statement.setInt(7, epoch);
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public void saveProtection(Protection protection) {
        try {
            Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE " + details.getPrefix() + "protections SET x = ?, y = ?, z = ?, world = ?, created = ?, updated = ?, accessed = ? WHERE id = ?");

            try {
                statement.setInt(1, protection.getX());
                statement.setInt(2, protection.getY());
                statement.setInt(3, protection.getZ());
                statement.setString(4, protection.getWorld().getName());
                statement.setInt(5, protection.getCreated());
                statement.setInt(6, protection.getUpdated());
                statement.setInt(7, protection.getAccessed());
                statement.setInt(8, protection.getId());
                statement.executeUpdate();
            } finally {
                safeClose(statement);
                safeClose(connection);
            }
        } catch (SQLException e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeProtection(Protection protection) {
        removeRoles(protection);
        removeProtectionAttributes(protection);

        try {
            Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protections WHERE id = ?");

            try {
                statement.setInt(1, protection.getId());
                statement.executeUpdate();
            } finally {
                safeClose(statement);
                safeClose(connection);
            }
        } catch (SQLException e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void saveOrCreateRole(Role role) {
        try {
            Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + details.getPrefix() + "protection_roles (protection_id, type, name, role) VALUES (?, ?, ?, ?)");

            try {
                statement.setInt(1, role.getProtection().getId());
                statement.setInt(2, role.getType());
                statement.setString(3, role.getRoleName());
                statement.setInt(4, role.getRoleAccess().ordinal());
                statement.executeUpdate();
            } finally {
                safeClose(statement);
                safeClose(connection);
            }
        } catch (SQLException e) {
            try {
                Connection connection = pool.getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE " + details.getPrefix() + "protection_roles SET name = ?, role = ? WHERE protection_id = ? AND type = ? AND name = ?");

                try {
                    statement.setString(1, role.getRoleName());
                    statement.setInt(2, role.getRoleAccess().ordinal());
                    statement.setInt(3, role.getProtection().getId());
                    statement.setInt(4, role.getType());
                    statement.setString(5, role.getRoleName());
                    statement.executeUpdate();
                } finally {
                    safeClose(statement);
                    safeClose(connection);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeRole(Role role) {
        try {
            Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protection_roles WHERE protection_id = ? AND type = ? AND name = ?");

            try {
                statement.setInt(1, role.getProtection().getId());
                statement.setInt(2, role.getType());
                statement.setString(3, role.getRoleName());
                statement.executeUpdate();
            } finally {
                safeClose(statement);
                safeClose(connection);
            }
        } catch (SQLException e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeRoles(Protection protection) {
        try {
            Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protection_roles WHERE protection_id = ?");

            try {
                statement.setInt(1, protection.getId());
                statement.executeUpdate();
            } finally {
                safeClose(statement);
                safeClose(connection);
            }
        } catch (SQLException e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void saveOrCreateProtectionAttribute(Protection protection, AbstractAttribute attribute) {
        try {
            Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + details.getPrefix() + "protection_attributes (protection_id, attribute_name, attribute_value) VALUES (?, ?, ?)");

            try {
                statement.setInt(1, protection.getId());
                statement.setString(2, attribute.getName());
                statement.setString(3, attribute.getStorableValue());
                statement.executeUpdate();
            } finally {
                safeClose(statement);
                safeClose(connection);
            }
        } catch (SQLException e) {
            try {
                Connection connection = pool.getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE " + details.getPrefix() + "protection_attributes SET attribute_value = ? WHERE protection_id = ? AND attribute_name = ?");

                try {
                    statement.setString(1, attribute.getStorableValue());
                    statement.setInt(2, protection.getId());
                    statement.setString(3, attribute.getName());
                    statement.executeUpdate();
                } finally {
                    safeClose(statement);
                    safeClose(connection);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeProtectionAttribute(Protection protection, AbstractAttribute attribute) {
        try {
            Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protection_attributes WHERE protection_id = ? AND attribute_name = ?");

            try {
                statement.setInt(1, protection.getId());
                statement.setString(2, attribute.getName());
                statement.executeUpdate();
            } finally {
                safeClose(statement);
                safeClose(connection);
            }
        } catch (SQLException e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeProtectionAttributes(Protection protection) {
        try {
            Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protection_attributes WHERE protection_id = ?");

            try {
                statement.setInt(1, protection.getId());
                statement.executeUpdate();
            } finally {
                safeClose(statement);
                safeClose(connection);
            }
        } catch (SQLException e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<AbstractAttribute> loadProtectionAttributes(Protection protection) {
        Set<AbstractAttribute> attributes = new HashSet<AbstractAttribute>();

        try {
            Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT attribute_name, attribute_value FROM " + details.getPrefix() + "protection_attributes WHERE protection_id = ?");

            try {
                statement.setInt(1, protection.getId());

                ResultSet set = statement.executeQuery();

                while (set.next()) {
                    AbstractAttribute attribute = engine.getProtectionManager().createProtectionAttribute(set.getString("attribute_name"));

                    if (attribute == null) {
                        // the attribute is no longer registered
                        // perhaps it was a plugin's attribute
                        // but it was removed ?
                        continue;
                    }

                    attribute.loadValue(set.getString("attribute_value"));
                    attributes.add(attribute);
                }

                set.close();
            } finally {
                safeClose(statement);
                safeClose(connection);
            }
        } catch (SQLException e) {
            handleException(e);
        }

        return attributes;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Role> loadProtectionRoles(Protection protection) {
        Set<Role> roles = new HashSet<Role>();

        try {
            Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT type, name, role FROM " + details.getPrefix() + "protection_roles WHERE protection_id = ?");

            try {
                statement.setInt(1, protection.getId());

                ResultSet set = statement.executeQuery();

                while (set.next()) {
                    RoleDefinition definition = engine.getRoleManager().getDefinition(set.getInt("type"));
                    Role role = definition.createRole(protection, set.getString("name"), ProtectionAccess.values()[set.getInt("role")]);

                    if (role != null) {
                        roles.add(role);
                    }
                }

                set.close();
            } finally {
                safeClose(statement);
                safeClose(connection);
            }
        } catch (SQLException e) {
            handleException(e);
        }

        return roles;
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
        protection.setState(State.UNMODIFIED);
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
        private final String hostname, database, databasePath, prefix, username, password;

        public JDBCConnectionDetails(Driver driver, String hostname, String database, String databasePath, String prefix, String username, String password) {
            if (driver == null) {
                throw new IllegalArgumentException("JDBC Driver cannot be null");
            }
            if (hostname == null) {
                throw new IllegalArgumentException("Hostname cannot be null");
            }
            if (database == null) {
                throw new IllegalArgumentException("Database cannot be null");
            }
            if (databasePath == null) {
                throw new IllegalArgumentException("Database path cannot be null");
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
            this.databasePath = databasePath;
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
         * Get the path to the database if needed (e.g for sqlite)
         *
         * @return
         */
        public String getDatabasePath() {
            return databasePath;
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
