/**
 * Copyright (c) 2011-2014 Tyler Blair
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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.getlwc.Engine;
import org.getlwc.Location;
import org.getlwc.SaveQueue;
import org.getlwc.World;
import org.getlwc.component.LocationSetComponent;
import org.getlwc.db.Database;
import org.getlwc.db.DatabaseException;
import org.getlwc.meta.Meta;
import org.getlwc.meta.MetaKey;
import org.getlwc.model.Protection;
import org.getlwc.model.Savable;
import org.getlwc.model.State;
import org.getlwc.role.Role;
import org.getlwc.role.RoleCreationException;
import org.getlwc.util.Tuple;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
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
    private HikariDataSource pool = null;

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
    }

    @Override
    public boolean connect() throws DatabaseException {
        Driver driver = details.getDriver();

        // Load any resources required for the driver
        engine.getResourceDownloader().ensureResourceInstalled("hikaricp");
        engine.getResourceDownloader().ensureResourceInstalled("flywaydb");
        engine.getResourceDownloader().ensureResourceInstalled("databases." + driver.toString().toLowerCase());

        // setup the database pool
        pool = new HikariDataSource(createHikariConfig(details));

        try {
            migrate();
            lookup.populate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            pool = null;
            return false;
        }
    }

    /**
     * Creates a HikariCP config object for the given connection details.
     *
     * @param details
     * @return
     */
    private HikariConfig createHikariConfig(JDBCConnectionDetails details) {
        HikariConfig config = new HikariConfig();

        // Get the path to the database
        String databasePath;

        // For SQLite & H2 the path is the path to the db file
        // for MySQL and others it's the hostname + the database name
        if (details.getDriver() == Driver.SQLITE || details.getDriver() == Driver.H2) {
            databasePath = details.getDatabasePath().replaceAll("%home%", engine.getServerLayer().getDataFolder().getPath().replaceAll("\\\\", "/"));
        } else {
            databasePath = "//" + details.getHostname() + "/" + details.getDatabase();
        }

        config.setJdbcUrl(String.format("jdbc:%s:%s", details.getDriver().toString().toLowerCase(), databasePath));
        config.setDriverClassName(details.getDriver().getClassName());
        config.setUsername(details.getUsername());
        config.setPassword(details.getPassword());

        // h2: enforce the user/pass
        if (details.getDriver() == Driver.H2) {
            config.setUsername("sa");
            config.setPassword("");
        }

        return config;
    }

    /**
     * Migrate the database up to the latest version.
     */
    private void migrate() {
        Flyway flyway = new Flyway();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("prefix", details.getPrefix());

        flyway.setLocations("/db/common", String.format("/db/%s", details.getDriver().toString().toLowerCase()));
        flyway.setTable(details.getPrefix() + "schema_version");
        flyway.setPlaceholders(placeholders);
        flyway.setSqlMigrationPrefix("v");
        flyway.setDataSource(new SingleConnectionDataSource(pool));

        // fixes issue where flyway could not load its metadata sql file
        flyway.setClassLoader(Flyway.class.getClassLoader());

        try {
            flyway.migrate();
        } catch (Exception e) { // FlywayException will crash LWC as Flyway is not loaded until init
            engine.getConsoleSender().sendMessage("Database migrations failed. Repairing & trying again.");

            flyway.repair();
            flyway.migrate();
        }
    }

    @Override
    public void disconnect() {
        pool.close();
        pool = null;
    }

    @Override
    public void saveLater(Savable savable) {
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
        List<Tuple<String, Integer>> result = new ArrayList<>();

        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, name FROM " + details.getPrefix() + "lookup_" + type.getSuffix());
             ResultSet set = statement.executeQuery()) {

            while (set.next()) {
                result.add(new Tuple<>(set.getString("name"), set.getInt("id")));
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

    @Override
    public Protection createProtection() {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + details.getPrefix() + "protections (updated, created, accessed) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            int epoch = (int) (System.currentTimeMillis() / 1000L);
            statement.setInt(1, epoch);
            statement.setInt(2, epoch);
            statement.setInt(3, epoch);

            int affected = statement.executeUpdate();

            if (affected == 0) {
                return null;
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int protectionId = generatedKeys.getInt(1);

                    return loadProtection(protectionId);
                }
            }
        } catch (SQLException e) {
            handleException(e);
        }

        return null;
    }

    @Override
    public Protection loadProtection(Location location) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT protection_id FROM " + details.getPrefix() + "protection_blocks WHERE x = ? AND y = ? AND z = ? AND world = ?")) {
            statement.setInt(1, location.getBlockX());
            statement.setInt(2, location.getBlockY());
            statement.setInt(3, location.getBlockZ());
            statement.setInt(4, lookup.get(JDBCLookupService.LookupType.WORLD_NAME, location.getWorld().getName()));

            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    return loadProtection(set.getInt("protection_id"));
                }
            }
        } catch (SQLException e) {
            handleException(e);
        }

        return null;
    }

    @Override
    public Protection loadProtection(int id) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, updated, created, accessed FROM " + details.getPrefix() + "protections WHERE id = ?")) {
            statement.setInt(1, id);

            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    Protection protection = resolveProtection(set);

                    Set<Location> locations = loadProtectionLocations(protection);

                    if (locations.size() > 0) {
                        LocationSetComponent locationSet = new LocationSetComponent();

                        for (Location location : locations) {
                            locationSet.add(location);
                        }

                        locationSet.resetObservedState();
                        protection.addComponent(locationSet);
                    }

                    return protection;
                }
            }
        } catch (SQLException e) {
            handleException(e);
        }

        return null;
    }

    @Override
    public void saveProtection(Protection protection) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE " + details.getPrefix() + "protections SET created = ?, updated = ?, accessed = ? WHERE id = ?")) {
            // TODO create / update / delete protection_blocks / protection_entities
            statement.setInt(1, protection.getCreated());
            statement.setInt(2, protection.getUpdated());
            statement.setInt(3, protection.getAccessed());
            statement.setInt(4, protection.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void removeProtection(Protection protection) {
        removeAllProtectionRoles(protection);
        removeAllProtectionMetadata(protection);

        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protections WHERE id = ?")) {
            statement.setInt(1, protection.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public Set<Location> loadProtectionLocations(Protection protection) {
        Set<Location> result = new HashSet<>();

        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT world, x, y, z FROM " + details.getPrefix() + "protection_blocks WHERE protection_id = ?")) {
            statement.setInt(1, protection.getId());

            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    World world = engine.getServerLayer().getWorld(lookup.get(JDBCLookupService.LookupType.WORLD_NAME, set.getInt("world")));
                    int x = set.getInt("x");
                    int y = set.getInt("y");
                    int z = set.getInt("z");

                    result.add(new Location(world, x, y, z));
                }
            }
        } catch (SQLException e) {
            handleException(e);
            return null;
        }

        return result;
    }

    @Override
    public void addProtectionLocation(Protection protection, Location location) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + details.getPrefix() + "protection_blocks (protection_id, world, x, y, z) VALUES (?, ?, ?, ?, ?)")) {
            statement.setInt(1, protection.getId());
            statement.setInt(2, lookup.get(JDBCLookupService.LookupType.WORLD_NAME, location.getWorld().getName()));
            statement.setInt(3, location.getBlockX());
            statement.setInt(4, location.getBlockY());
            statement.setInt(5, location.getBlockZ());
            statement.execute();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void removeProtectionLocation(Protection protection, Location location) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protection_blocks WHERE protection_id = ? AND world = ? AND x = ? AND y = ? AND z = ?")) {
            statement.setInt(1, protection.getId());
            statement.setInt(2, lookup.get(JDBCLookupService.LookupType.WORLD_NAME, location.getWorld().getName()));
            statement.setInt(3, location.getBlockX());
            statement.setInt(4, location.getBlockY());
            statement.setInt(5, location.getBlockZ());
            statement.execute();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void removeAllProtectionLocations(Protection protection) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protection_blocks WHERE protection_id = ?")) {
            statement.setInt(1, protection.getId());
            statement.execute();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void saveOrCreateProtectionRole(Protection protection, Role role) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + details.getPrefix() + "protection_roles (protection_id, type, name, role) VALUES (?, ?, ?, ?)")) {
            statement.setInt(1, protection.getId());
            statement.setInt(2, lookup.get(JDBCLookupService.LookupType.ROLE_TYPE, role.getType()));
            statement.setInt(3, lookup.get(JDBCLookupService.LookupType.ROLE_NAME, role.serialize()));
            statement.setInt(4, role.getAccess().ordinal());
            statement.executeUpdate();
        } catch (SQLException e) {
            try (Connection connection = pool.getConnection();
                 PreparedStatement statement = connection.prepareStatement("UPDATE " + details.getPrefix() + "protection_roles SET name = ?, role = ? WHERE protection_id = ? AND type = ? AND name = ?")) {
                statement.setInt(1, lookup.get(JDBCLookupService.LookupType.ROLE_NAME, role.serialize()));
                statement.setInt(2, role.getAccess().ordinal());
                statement.setInt(3, protection.getId());
                statement.setInt(4, lookup.get(JDBCLookupService.LookupType.ROLE_TYPE, role.getType()));
                statement.setInt(5, lookup.get(JDBCLookupService.LookupType.ROLE_NAME, role.serialize()));
                statement.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void removeProtectionRole(Protection protection, Role role) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protection_roles WHERE protection_id = ? AND type = ? AND name = ?")) {
            statement.setInt(1, protection.getId());
            statement.setInt(2, lookup.get(JDBCLookupService.LookupType.ROLE_TYPE, role.getType()));
            statement.setInt(3, lookup.get(JDBCLookupService.LookupType.ROLE_NAME, role.serialize()));
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void removeAllProtectionRoles(Protection protection) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protection_roles WHERE protection_id = ?")) {
            statement.setInt(1, protection.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void saveOrCreateProtectionMetadata(Protection protection, Meta meta) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + details.getPrefix() + "protection_meta (protection_id, meta_name, meta_value) VALUES (?, ?, ?)")) {
            statement.setInt(1, protection.getId());
            statement.setInt(2, lookup.get(JDBCLookupService.LookupType.META_NAME, meta.getKey().getKey()));
            statement.setString(3, meta.asString());
            statement.executeUpdate();
        } catch (SQLException e) {
            try (Connection connection = pool.getConnection();
                 PreparedStatement statement = connection.prepareStatement("UPDATE " + details.getPrefix() + "protection_meta SET meta_value = ? WHERE protection_id = ? AND meta_name = ?")) {
                statement.setString(1, meta.getValue());
                statement.setInt(2, protection.getId());
                statement.setInt(3, lookup.get(JDBCLookupService.LookupType.META_NAME, meta.getKey().getKey()));
                statement.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void removeProtectionMetadata(Protection protection, Meta meta) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protection_meta WHERE protection_id = ? AND meta_name = ?")) {
            statement.setInt(1, protection.getId());
            statement.setInt(2, lookup.get(JDBCLookupService.LookupType.META_NAME, meta.getKey().getKey()));
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void removeAllProtectionMetadata(Protection protection) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM " + details.getPrefix() + "protection_meta WHERE protection_id = ?")) {
            statement.setInt(1, protection.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public Set<Meta> loadProtectionMetadata(Protection protection) {
        Set<Meta> meta = new HashSet<>();

        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT meta_name, meta_value FROM " + details.getPrefix() + "protection_meta WHERE protection_id = ?")) {
            statement.setInt(1, protection.getId());

            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    String key = lookup.get(JDBCLookupService.LookupType.META_NAME, set.getInt("meta_name"));
                    String value = set.getString("meta_value");

                    meta.add(new Meta(MetaKey.valueOf(key), value));
                }
            }
        } catch (SQLException e) {
            handleException(e);
        }

        return meta;
    }

    @Override
    public Set<Role> loadProtectionRoles(Protection protection) {
        Set<Role> roles = new HashSet<>();

        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT type, name, role FROM " + details.getPrefix() + "protection_roles WHERE protection_id = ?")) {
            statement.setInt(1, protection.getId());

            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    String type = lookup.get(JDBCLookupService.LookupType.ROLE_TYPE, set.getInt("type"));
                    String name = lookup.get(JDBCLookupService.LookupType.ROLE_NAME, set.getInt("name"));

                    Role role = null;

                    try {
                        role = engine.getProtectionManager().getRoleRegistry().loadRole(type, name);
                    } catch (RoleCreationException e) {
                        e.printStackTrace();
                    }

                    if (role != null) {
                        role.setAccess(Protection.Access.values()[set.getInt("role")]);
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
        int protectionId = set.getInt("id");

        Protection protection = new Protection(engine, protectionId);

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
