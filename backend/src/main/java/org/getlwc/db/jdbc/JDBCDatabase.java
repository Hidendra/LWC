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

package org.getlwc.db.jdbc;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.getlwc.Engine;
import org.getlwc.Location;
import org.getlwc.SaveQueue;
import org.getlwc.World;
import org.getlwc.db.Database;
import org.getlwc.db.DatabaseException;
import org.getlwc.model.AbstractAttribute;
import org.getlwc.model.AbstractSavable;
import org.getlwc.model.BlockProtection;
import org.getlwc.model.Protection;
import org.getlwc.model.State;
import org.getlwc.provider.BasicProvider;
import org.getlwc.provider.ProtectionProvider;
import org.getlwc.role.ProtectionRole;
import org.getlwc.util.Tuple;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        H2("org.h2.Driver"),

        /**
         * PostgreSQL
         */
        POSTGRESQL("org.postgresql.Driver");

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

    /**
     * The service used to translate names / ids between each other. This just saves space in the database
     * to save the space that a string would have taken
     */
    private JDBCLookupService lookup = new JDBCLookupService(this);

    /**
     * The queue used to save savables later
     */
    private SaveQueue saveQueue = new SaveQueue();

    public JDBCDatabase(Engine engine, JDBCConnectionDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("Connection details cannot be null");
        }

        this.engine = engine;
        this.details = details;
        engine.getLibraryDownloader().ensureResourceInstalled("c3p0");
    }

    /**
     * {@inheritDoc}
     */
    public boolean connect() throws DatabaseException {
        Driver driver = details.getDriver();

        // Load any resources required for the driver
        engine.getLibraryDownloader().ensureResourceInstalled("databases." + driver.toString().toLowerCase());

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
            lookup.populate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            pool = null;
            return false;
        }
    }

    /**
     * Verify the base SQL. If the base tables were not created, create them
     */
    private void verifyBase() {
        boolean baseRequired;

        try (Connection connection = pool.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.executeQuery("SELECT 1 FROM " + details.getPrefix() + "protections").close();
            baseRequired = false;
        } catch (SQLException e) {
            baseRequired = true;
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

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                file += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // fix the prefix
        file = file.replaceAll("__PREFIX__", details.getPrefix());

        try (Connection connection = pool.getConnection()) {
            // convert the SQL to a stream so we can use a (mostly) unmodified ScriptRunner class
            InputStream stream = new ByteArrayInputStream(file.getBytes("UTF-8"));

            JDBCScriptRunner runner = new JDBCScriptRunner(connection, false, false);
            runner.setLogWriter(null);
            runner.runScript(new InputStreamReader(stream));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void disconnect() {
        pool.close();
        pool = null;
    }

    public void saveLater(AbstractSavable savable) {
        saveQueue.add(savable);
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
     * Get all of the lookup associations for the given type in the database
     *
     * @param type
     * @return
     */
    public List<Tuple<String, Integer>> getLookupAssociations(JDBCLookupService.LookupType type) {
        List<Tuple<String, Integer>> result = new ArrayList<Tuple<String, Integer>>();

        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, name FROM " + details.getPrefix() + "lookup_" + type.getSuffix());
             ResultSet set = statement.executeQuery()) {

            while (set.next()) {
                result.add(new Tuple<String, Integer>(set.getString("name"), set.getInt("id")));
            }
        } catch (SQLException e) {
            handleException(e);
        }

        return result;
    }

    /**
     * Create a lookup in the database
     *
     * @param name
     * @return
     */
    public int createLookup(JDBCLookupService.LookupType type, String name) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + details.getPrefix() + "lookup_" + type.getSuffix() + " (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.executeUpdate();

            try (ResultSet set = statement.getGeneratedKeys()) {
                if (set.next()) {
                    return set.getInt(1);
                }
            }
        } catch (SQLException e) {
            handleException(e);
        }

        return -1;
    }

    /**
     * {@inheritDoc}
     */
    public Protection createProtection(Location location) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + details.getPrefix() + "protections (type, updated, created, accessed) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, Protection.Type.BLOCK.ordinal());
            int epoch = (int) (System.currentTimeMillis() / 1000L);
            statement.setInt(2, epoch);
            statement.setInt(3, epoch);
            statement.setInt(4, epoch);

            int affected = statement.executeUpdate();

            if (affected == 0) {
                return null;
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int protectionId = generatedKeys.getInt(1);
                    addProtectionBlock(protectionId, location);
                }
            }
        } catch (SQLException e) {
            handleException(e);
        }

        return loadProtection(location);
    }

    /**
     * Add a protection's block to the database.
     *
     * @param protectionId
     * @param location
     * @return true if the block was added; false otherwise
     */
    private boolean addProtectionBlock(int protectionId, Location location) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + details.getPrefix() + "protection_blocks (protection_id, world, x, y, z) VALUES (?, ?, ?, ?, ?)")) {
            statement.setInt(1, protectionId);
            statement.setInt(2, lookup.get(JDBCLookupService.LookupType.WORLD_NAME, location.getWorld().getName()));
            statement.setInt(3, location.getBlockX());
            statement.setInt(4, location.getBlockY());
            statement.setInt(5, location.getBlockZ());

            int affected = statement.executeUpdate();

            if (affected == 0) {
                return true;
            }
        } catch (SQLException e) {
            handleException(e);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Protection loadProtection(Location location) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT protection_id FROM " + details.getPrefix() + "protection_blocks WHERE x = ? AND y = ? AND z = ? AND world = ?")) {
            statement.setInt(1, location.getBlockX());
            statement.setInt(2, location.getBlockY());
            statement.setInt(3, location.getBlockZ());
            statement.setInt(4, lookup.get(JDBCLookupService.LookupType.WORLD_NAME, location.getWorld().getName()));

            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    // TODO compress into a join? At the time this is the very simplest solution
                    return loadProtection(set.getInt("protection_id"));
                }
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
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, type, updated, created, accessed FROM " + details.getPrefix() + "protections WHERE id = ?")) {
            statement.setInt(1, id);

            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    return resolveProtection(set);
                }
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
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE " + details.getPrefix() + "protections SET type = ?, created = ?, updated = ?, accessed = ? WHERE id = ?")) {
            // TODO create / update / delete protection_blocks / protection_entities
            statement.setInt(1, protection.getType().ordinal());
            statement.setInt(2, protection.getCreated());
            statement.setInt(3, protection.getUpdated());
            statement.setInt(4, protection.getAccessed());
            statement.setInt(5, protection.getId());
            statement.executeUpdate();
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

        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protections WHERE id = ?")) {
            statement.setInt(1, protection.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void saveOrCreateRole(ProtectionRole role) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + details.getPrefix() + "protection_roles (protection_id, type, name, role) VALUES (?, ?, ?, ?)")) {
            statement.setInt(1, role.getProtection().getId());
            statement.setInt(2, lookup.get(JDBCLookupService.LookupType.ROLE_TYPE, role.getType()));
            statement.setInt(3, lookup.get(JDBCLookupService.LookupType.ROLE_NAME, role.getName()));
            statement.setInt(4, role.getAccess().ordinal());
            statement.executeUpdate();
        } catch (SQLException e) {
            try (Connection connection = pool.getConnection();
                 PreparedStatement statement = connection.prepareStatement("UPDATE " + details.getPrefix() + "protection_roles SET name = ?, role = ? WHERE protection_id = ? AND type = ? AND name = ?")) {
                statement.setString(1, role.getName());
                statement.setInt(2, role.getAccess().ordinal());
                statement.setInt(3, role.getProtection().getId());
                statement.setString(4, role.getType());
                statement.setString(5, role.getName());
                statement.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeRole(ProtectionRole role) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protection_roles WHERE protection_id = ? AND type = ? AND name = ?")) {
            statement.setInt(1, role.getProtection().getId());
            statement.setInt(2, lookup.get(JDBCLookupService.LookupType.ROLE_TYPE, role.getType()));
            statement.setInt(3, lookup.get(JDBCLookupService.LookupType.ROLE_NAME, role.getName()));
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeRoles(Protection protection) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protection_roles WHERE protection_id = ?")) {
            statement.setInt(1, protection.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void saveOrCreateProtectionAttribute(Protection protection, AbstractAttribute attribute) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + details.getPrefix() + "protection_attributes (protection_id, attribute_name, attribute_value) VALUES (?, ?, ?)")) {
            statement.setInt(1, protection.getId());
            statement.setInt(2, lookup.get(JDBCLookupService.LookupType.ATTRIBUTE_NAME, attribute.getName()));
            statement.setString(3, attribute.getStorableValue());
            statement.executeUpdate();
        } catch (SQLException e) {
            try (Connection connection = pool.getConnection();
                 PreparedStatement statement = connection.prepareStatement("UPDATE " + details.getPrefix() + "protection_attributes SET attribute_value = ? WHERE protection_id = ? AND attribute_name = ?")) {
                statement.setString(1, attribute.getStorableValue());
                statement.setInt(2, protection.getId());
                statement.setInt(3, lookup.get(JDBCLookupService.LookupType.ATTRIBUTE_NAME, attribute.getName()));
                statement.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeProtectionAttribute(Protection protection, AbstractAttribute attribute) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protection_attributes WHERE protection_id = ? AND attribute_name = ?")) {
            statement.setInt(1, protection.getId());
            statement.setInt(2, lookup.get(JDBCLookupService.LookupType.ATTRIBUTE_NAME, attribute.getName()));
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeProtectionAttributes(Protection protection) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protection_attributes WHERE protection_id = ?")) {
            statement.setInt(1, protection.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<AbstractAttribute> loadProtectionAttributes(Protection protection) {
        Set<AbstractAttribute> attributes = new HashSet<AbstractAttribute>();

        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT attribute_name, attribute_value FROM " + details.getPrefix() + "protection_attributes WHERE protection_id = ?")) {
            statement.setInt(1, protection.getId());

            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    String attributeName = lookup.get(JDBCLookupService.LookupType.ATTRIBUTE_NAME, set.getInt("attribute_name"));
                    BasicProvider<AbstractAttribute> provider = engine.getProtectionManager().getAttributeManager().get(attributeName);

                    if (provider == null) {
                        // possibly from another plugin but no longer on the server
                        // just gracefully ignore it
                        continue;
                    }

                    String attributeValue = set.getString("attribute_value");
                    AbstractAttribute loaded = provider.create();

                    if (loaded != null) {
                        loaded.loadData(attributeValue);
                        attributes.add(loaded);
                    }
                }
            }
        } catch (SQLException e) {
            handleException(e);
        }

        return attributes;
    }

    /**
     * {@inheritDoc}
     */
    public Set<ProtectionRole> loadProtectionRoles(Protection protection) {
        Set<ProtectionRole> roles = new HashSet<ProtectionRole>();

        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT type, name, role FROM " + details.getPrefix() + "protection_roles WHERE protection_id = ?")) {
            statement.setInt(1, protection.getId());

            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    String type = lookup.get(JDBCLookupService.LookupType.ROLE_TYPE, set.getInt("type"));
                    String name = lookup.get(JDBCLookupService.LookupType.ROLE_NAME, set.getInt("name"));

                    ProtectionProvider<ProtectionRole> provider = engine.getProtectionManager().getRoleManager().get(type);

                    if (provider == null) {
                        // possibly from another plugin but no longer on the server
                        // just gracefully ignore it
                        continue;
                    }

                    ProtectionRole role = provider.create(protection);

                    if (role != null) {
                        role.setName(name);
                        role.setProtectionAccess(ProtectionRole.Access.values()[set.getInt("role")]);
                        roles.add(role);
                    }
                }
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
        Protection protection = null;
        int protectionId = set.getInt("id");
        int typeId = set.getInt("type");

        if (typeId < 0 || typeId >= Protection.Type.values().length) {
            return null;
        }

        Protection.Type type = Protection.Type.values()[typeId];

        switch (type) {
            case BLOCK:
                try (Connection connection = pool.getConnection();
                     PreparedStatement statement = connection.prepareStatement("SELECT world, x, y, z FROM " + details.getPrefix() + "protection_blocks WHERE protection_id = ?")) {
                    statement.setInt(1, protectionId);

                    try (ResultSet blockSet = statement.executeQuery()) {
                        if (blockSet.next()) {
                            // TODO allow multiple blocks per protection ?
                            World world = engine.getServerLayer().getWorld(lookup.get(JDBCLookupService.LookupType.WORLD_NAME, blockSet.getInt("world")));
                            int x = blockSet.getInt("x");
                            int y = blockSet.getInt("y");
                            int z = blockSet.getInt("z");

                            protection = new BlockProtection(engine, protectionId, new Location(world, x, y, z));
                        }
                    }
                } catch (SQLException e) {
                    handleException(e);
                    return null;
                }

                break;

            default:
                throw new UnsupportedOperationException("Unsupported protection type: " + type);
        }

        if (protection == null) {
            return null;
        }

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
