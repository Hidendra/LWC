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

import com.griefcraft.cache.LRUCache;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.AccessRight;
import com.griefcraft.model.Flag;
import com.griefcraft.model.History;
import com.griefcraft.model.Job;
import com.griefcraft.model.Protection;
import com.griefcraft.modules.limits.LimitsModule;
import com.griefcraft.scripting.Module;
import com.griefcraft.util.Performance;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PhysDB extends Database {

    /**
     * The JSON Parser object
     */
    private final JSONParser jsonParser = new JSONParser();

    /**
     * The database version
     */
    private int databaseVersion = 0;

    public PhysDB() {
        super();
    }

    public PhysDB(Type currentType) {
        super(currentType);
    }

    @Override
    protected void postPrepare() {
        Performance.addPhysDBQuery();
    }

    /**
     * Fetch an object from the sql database
     *
     * @param sql
     * @param column
     * @return
     */
    public Object fetch(String sql, String column, Object... toBind) {
        try {
            int index = 1;
            PreparedStatement statement = prepare(sql);

            for (Object bind : toBind) {
                statement.setObject(index, bind);
                index++;
            }

            ResultSet set = statement.executeQuery();

            if (set.next()) {
                Object object = set.getObject(column);
                set.close();
                return object;
            }

            set.close();
        } catch (Exception e) {
            printException(e);
        }

        return null;
    }

    /**
     * Get the menu style for a player
     *
     * @param player
     * @return
     */
    public String getMenuStyle(String player) {
        try {
            PreparedStatement statement = prepare("SELECT menu FROM " + prefix + "menu_styles WHERE player = ?");
            statement.setString(1, player);

            ResultSet set = statement.executeQuery();

            if (set.next()) {
                String style = set.getString("menu");
                set.close();
                return style;
            }

            set.close();
        } catch (Exception e) {
            printException(e);
        }

        return LWC.getInstance().getConfiguration().getString("core.defaultMenuStyle");
    }

    /**
     * @return the number of protected chests
     */
    public int getProtectionCount() {
        return Integer.decode(fetch("SELECT COUNT(*) AS count FROM " + prefix + "protections", "count").toString());
    }

    /**
     * @return the number of history items stored
     */
    public int getHistoryCount() {
        return Integer.decode(fetch("SELECT COUNT(*) AS count FROM " + prefix + "history", "count").toString());
    }

    /**
     * Get the amount of protections a player has
     *
     * @param player
     * @return the amount of protections they have
     */
    public int getProtectionCount(String player) {
        int count = 0;

        try {
            PreparedStatement statement = prepare("SELECT COUNT(*) as count FROM " + prefix + "protections WHERE owner = ?");
            statement.setString(1, player);

            ResultSet set = statement.executeQuery();

            if (set.next()) {
                count = set.getInt("count");
            }

            set.close();
        } catch (SQLException e) {
            printException(e);
        }

        return count;
    }

    /**
     * Get the amount of protections a player has
     *
     * @param player
     * @return the amount of protections they have
     */
    public int getHistoryCount(String player) {
        int count = 0;

        try {
            PreparedStatement statement = prepare("SELECT COUNT(*) AS count FROM " + prefix + "history WHERE LOWER(player) = LOWER(?)");
            statement.setString(1, player);

            ResultSet set = statement.executeQuery();

            if (set.next()) {
                count = set.getInt("count");
            }

            set.close();
        } catch (SQLException e) {
            printException(e);
        }

        return count;
    }

    /**
     * Get the amount of chests a player has of a specific block id
     *
     * @param player
     * @return the amount of protections they have of blockId
     */
    public int getProtectionCount(String player, int blockId) {
        int count = 0;

        try {
            PreparedStatement statement = prepare("SELECT COUNT(*) AS count FROM " + prefix + "protections WHERE owner = ? AND blockId = ?");
            statement.setString(1, player);
            statement.setInt(2, blockId);

            ResultSet set = statement.executeQuery();

            if (set.next()) {
                count = set.getInt("count");
            }

            set.close();
        } catch (SQLException e) {
            printException(e);
        }

        return count;
    }

    /**
     * Load the database and do any updating required or create the tables
     */
    @Override
    public void load() {
        if (loaded) {
            return;
        }

        /**
         * Updates that alter or rename a table go here
         */
        doUpdate301();
        doUpdate302();
        doUpdate330();
        doUpdate400_1();
        doUpdate400_4();
        doUpdate400_2();
        doUpdate400_3();
        doUpdate400_4();
        doUpdate400_5();
        doUpdate400_6();

        Column column;

        Table protections = new Table(this, "protections");
        {
            column = new Column("id");
            column.setType("INTEGER");
            column.setPrimary(true);
            protections.add(column);

            column = new Column("owner");
            column.setType("VARCHAR(255)");
            protections.add(column);

            column = new Column("type");
            column.setType("INTEGER");
            protections.add(column);

            column = new Column("x");
            column.setType("INTEGER");
            protections.add(column);

            column = new Column("y");
            column.setType("INTEGER");
            protections.add(column);

            column = new Column("z");
            column.setType("INTEGER");
            protections.add(column);

            column = new Column("flags");
            column.setType("INTEGER");
            protections.add(column);

            column = new Column("data");
            column.setType("TEXT");
            protections.add(column);

            column = new Column("blockId");
            column.setType("INTEGER");
            protections.add(column);

            column = new Column("world");
            column.setType("VARCHAR(255)");
            protections.add(column);

            column = new Column("password");
            column.setType("VARCHAR(255)");
            protections.add(column);

            column = new Column("date");
            column.setType("VARCHAR(255)");
            protections.add(column);

            column = new Column("last_accessed");
            column.setType("INTEGER");
            protections.add(column);
        }

        Table menuStyles = new Table(this, "menu_styles");
        {
            column = new Column("player");
            column.setType("VARCHAR(255)");
            column.setPrimary(true);
            column.setAutoIncrement(false);
            menuStyles.add(column);

            column = new Column("menu");
            column.setType("VARCHAR(255)");
            menuStyles.add(column);
        }

        Table history = new Table(this, "history");
        {
            column = new Column("id");
            column.setType("INTEGER");
            column.setPrimary(true);
            history.add(column);

            column = new Column("protectionId");
            column.setType("INTEGER");
            history.add(column);

            column = new Column("player");
            column.setType("VARCHAR(255)");
            history.add(column);

            column = new Column("x");
            column.setType("INTEGER");
            history.add(column);

            column = new Column("y");
            column.setType("INTEGER");
            history.add(column);

            column = new Column("z");
            column.setType("INTEGER");
            history.add(column);

            column = new Column("type");
            column.setType("INTEGER");
            history.add(column);

            column = new Column("status");
            column.setType("INTEGER");
            history.add(column);

            column = new Column("metadata");
            column.setType("VARCHAR(255)");
            history.add(column);

            column = new Column("timestamp");
            column.setType("long");
            history.add(column);
        }

        Table jobs = new Table(this, "jobs");
        {
            column = new Column("id");
            column.setType("INTEGER");
            column.setPrimary(true);
            jobs.add(column);

            column = new Column("name");
            column.setType("VARCHAR(64)");
            jobs.add(column);

            column = new Column("type");
            column.setType("INTEGER");
            jobs.add(column);

            column = new Column("data");
            column.setType("TEXT");
            jobs.add(column);

            column = new Column("nextRun");
            column.setType("INTEGER");
            jobs.add(column);
        }

        Table internal = new Table(this, "internal");
        {
            column = new Column("name");
            column.setType("VARCHAR(40)");
            column.setPrimary(true);
            column.setAutoIncrement(false);
            internal.add(column);

            column = new Column("value");
            column.setType("VARCHAR(40)");
            internal.add(column);
        }

        protections.execute();
        menuStyles.execute();
        history.execute();
        jobs.execute();
        internal.execute();

        // Load the database version
        loadDatabaseVersion();

        // perform database upgrades
        performDatabaseUpdates();

        loaded = true;
    }

    /**
     * Perform any database updates
     */
    public void performDatabaseUpdates() {

        // Indexes
        if (databaseVersion == 0) {
            // Drop old, old indexes
            log("Dropping old indexes (One time, may take a while!)");
            dropIndex("protections", "in1");
            dropIndex("protections", "in6");
            dropIndex("protections", "in7");
            dropIndex("history", "in8");
            dropIndex("history", "in9");
            dropIndex("protections", "in10");
            dropIndex("history", "in12");
            dropIndex("history", "in13");
            dropIndex("history", "in14");

            // Create our updated (good) indexes
            log("Creating new indexes (One time, may take a while!)");
            doIndex("protections", "protections_main", "x, y, z, world");
            doIndex("protections", "protections_utility", "owner");
            doIndex("history", "history_main", "protectionId");
            doIndex("history", "history_utility", "player");
            doIndex("history", "history_utility2", "x, y, z");

            // increment the database version
            incrementDatabaseVersion();
        }

        if (databaseVersion == 1) {
            log("Creating index on internal");
            doIndex("internal", "internal_main", "name");
            incrementDatabaseVersion();
        }

    }

    /**
     * Increment the database version
     */
    public void incrementDatabaseVersion() {
        setDatabaseVersion(++databaseVersion);
    }

    /**
     * Set the database version and sync it to the database
     * 
     * @param databaseVersion
     */
    public void setDatabaseVersion(int databaseVersion) {
        // set it locally
        this.databaseVersion = databaseVersion;

        // ship it to the database
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE " + prefix + "internal SET value = ? WHERE name = ?");
            statement.setInt(1, databaseVersion);
            statement.setString(2, "version");

            // ok
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) { }
    }

    /**
     * Load the database internal version
     *
     * @return
     */
    public int loadDatabaseVersion() {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT value FROM " + prefix + "internal WHERE name = ?");
            statement.setString(1, "version");

            // Execute it
            ResultSet set = statement.executeQuery();

            // load the version
            if (set.next()) {
                databaseVersion = Integer.parseInt(set.getString("value"));
            } else {
                throw new IllegalStateException("Internal is empty");
            }

            // close everything
            set.close();
            statement.close();
        } catch (Exception e) {
            // Doesn't exist, create it
            try {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO " + prefix + "internal (name, value) VALUES(?, ?)");
                statement.setString(1, "version");
                statement.setInt(2, databaseVersion);

                // ok
                statement.executeUpdate();
                statement.close();
            } catch (SQLException ex) { }
        }

        return databaseVersion;
    }

    /**
     * Load a protection with the given id
     *
     * @param protectionId
     * @return the Chest object
     */
    public Protection loadProtection(int protectionId) {
        try {
            PreparedStatement statement = prepare("SELECT id, owner, type, x, y, z, data, blockId, world, password, date, last_accessed FROM " + prefix + "protections WHERE id = ?");
            statement.setInt(1, protectionId);

            return resolveProtection(statement);
        } catch (SQLException e) {
            printException(e);
        }

        return null;
    }

    /**
     * Load protections using a specific type
     *
     * @param type
     * @return the Protection object
     */
    public List<Protection> loadProtectionsUsingType(Protection.Type type) {
        try {
            PreparedStatement statement = prepare("SELECT id, owner, type, x, y, z, data, blockId, world, password, date, last_accessed FROM " + prefix + "protections WHERE type = ?");
            statement.setInt(1, type.ordinal());

            return resolveProtections(statement);
        } catch (SQLException e) {
            printException(e);
        }

        return new ArrayList<Protection>();
    }

    /**
     * Resolve one protection from a ResultSet. The ResultSet is not closed.
     *
     * @param set
     * @return
     */
    public Protection resolveProtection(ResultSet set) {
        try {
            Protection protection = new Protection();

            int protectionId = set.getInt("id");
            int x = set.getInt("x");
            int y = set.getInt("y");
            int z = set.getInt("z");
            int blockId = set.getInt("blockId");
            int type = set.getInt("type");
            String world = set.getString("world");
            String owner = set.getString("owner");
            String password = set.getString("password");
            String date = set.getString("date");
            long lastAccessed = set.getLong("last_accessed");

            protection.setId(protectionId);
            protection.setX(x);
            protection.setY(y);
            protection.setZ(z);
            protection.setBlockId(blockId);
            protection.setType(Protection.Type.values()[type]);
            protection.setWorld(world);
            protection.setOwner(owner);
            protection.setPassword(password);
            protection.setDate(date);
            protection.setLastAccessed(lastAccessed);

            // check for oh so beautiful data!
            String data = set.getString("data");

            if (data == null || data.trim().isEmpty()) {
                return protection;
            }

            // rev up them JSON parsers!
            Object object = null;

            try {
                object = jsonParser.parse(data);
            } catch (Exception e) {
                return protection;
            } catch (Error e) {
                return protection;
            }

            if (!(object instanceof JSONObject)) {
                return protection;
            }

            // obtain the root
            JSONObject root = (JSONObject) object;
            protection.getData().putAll(root);

            // Attempt to parse rights
            Object rights = root.get("rights");

            if (rights != null && (rights instanceof JSONArray)) {
                JSONArray array = (JSONArray) rights;

                for (Object node : array) {
                    // we only want to use the maps
                    if (!(node instanceof JSONObject)) {
                        continue;
                    }

                    JSONObject map = (JSONObject) node;

                    // decode the map
                    AccessRight right = AccessRight.decodeJSON(map);

                    // bingo!
                    if (right != null) {
                        protection.addAccessRight(right);
                    }
                }
            }

            // Attempt to parse flags
            Object flags = root.get("flags");
            if (flags != null && (rights instanceof JSONArray)) {
                JSONArray array = (JSONArray) flags;

                for (Object node : array) {
                    if (!(node instanceof JSONObject)) {
                        continue;
                    }

                    JSONObject map = (JSONObject) node;

                    Flag flag = Flag.decodeJSON(map);

                    if (flag != null) {
                        protection.addFlag(flag);
                    }
                }
            }

            return protection;
        } catch (SQLException e) {
            printException(e);
            return null;
        }
    }

    /**
     * Resolve every protection from a result set
     *
     * @param set
     * @return
     */
    private List<Protection> resolveProtections(ResultSet set) {
        List<Protection> protections = new ArrayList<Protection>();

        try {
            while (set.next()) {
                Protection protection = resolveProtection(set);

                if (protection != null) {
                    protections.add(protection);
                }
            }
        } catch (SQLException e) {
            printException(e);
        }

        return protections;
    }

    /**
     * Resolve a list of protections from a statement
     *
     * @param statement
     * @return
     */
    private List<Protection> resolveProtections(PreparedStatement statement) {
        List<Protection> protections = new ArrayList<Protection>();
        ResultSet set = null;

        try {
            set = statement.executeQuery();
            protections = resolveProtections(set);
        } catch (SQLException e) {
            printException(e);
        } finally {
            if (set != null) {
                try {
                    set.close();
                } catch (SQLException e) {
                }
            }
        }

        return protections;
    }

    /**
     * Resolve the first protection from a statement
     *
     * @param statement
     * @return
     */
    public Protection resolveProtection(PreparedStatement statement) {
        List<Protection> protections = resolveProtections(statement);

        if (protections.size() == 0) {
            return null;
        }

        return protections.get(0);
    }

    /**
     * Fill the protection cache as much as possible with protections
     * Caches the most recent protections
     */
    public void precache() {
        LWC lwc = LWC.getInstance();
        LRUCache<String, Protection> cache = lwc.getCaches().getProtections();

        // clear the cache incase we're working on a dirty cache
        cache.clear();

        int precacheSize = lwc.getConfiguration().getInt("core.precache", -1);

        if (precacheSize == -1) {
            precacheSize = lwc.getConfiguration().getInt("core.cacheSize", 10000);
        }

        try {
            PreparedStatement statement = prepare("SELECT id, owner, type, x, y, z, data, blockId, world, password, date, last_accessed FROM " + prefix + "protections ORDER BY id DESC LIMIT ?");
            statement.setInt(1, precacheSize);
            statement.setFetchSize(10);

            // scrape the protections from the result set now
            List<Protection> protections = resolveProtections(statement);

            // throw all of the protections in
            for (Protection protection : protections) {
                String cacheKey = protection.getCacheKey();
                cache.put(cacheKey, protection);
            }

            log("Precached " + protections.size() + " protections.");
        } catch (SQLException e) {
            printException(e);
        }

        // Cache them all
    }

    /**
     * Used for the Bukkit #656 workaround to add a "cached" protection node when we find a
     * 2-block chest. A protection normally only applies to one block, so this method will be
     * called for the 2nd half of the chest to apply the same protection to the second block
     * when it is first noticed.  In this way, even if Bukkit goes bonkers (as in bug #656),
     * and starts returning bogus Blocks, we have already cached the double chest when
     * we first noticed it and the Protection will still apply.
     *
     * @param worldName
     * @param x
     * @param y
     * @param z
     */
    public void addCachedProtection(String worldName, int x, int y, int z, Protection p) {
        String cacheKey = worldName + ":" + x + ":" + y + ":" + z;
        LRUCache<String, Protection> cache = LWC.getInstance().getCaches().getProtections();

        cache.put(cacheKey, p);
    }

    /**
     * Return the cached Protection for a given block (if any).
     *
     * @param worldName
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Protection getCachedProtection(String worldName, int x, int y, int z) {
        String cacheKey = worldName + ":" + x + ":" + y + ":" + z;
        LRUCache<String, Protection> cache = LWC.getInstance().getCaches().getProtections();

        return cache.get(cacheKey);
    }

    /**
     * Load a chest at a given tile
     *
     * @param x
     * @param y
     * @param z
     * @return the Protection object
     */
    public Protection loadProtection(String worldName, int x, int y, int z) {
        // the unique key to use in the cache
        String cacheKey = worldName + ":" + x + ":" + y + ":" + z;

        // the protection cache
        LRUCache<String, Protection> cache = LWC.getInstance().getCaches().getProtections();

        // check if the protection is already cached
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        try {
            PreparedStatement statement = prepare("SELECT id, owner, type, x, y, z, data, blockId, world, password, date, last_accessed FROM " + prefix + "protections WHERE x = ? AND y = ? AND z = ? AND world = ?");
            statement.setInt(1, x);
            statement.setInt(2, y);
            statement.setInt(3, z);
            statement.setString(4, worldName);

            Protection protection = resolveProtection(statement);

            // cache the protection
            cache.put(cacheKey, protection);

            return protection;
        } catch (SQLException e) {
            printException(e);
        }

        return null;
    }

    /**
     * Load all protections (use sparingly !!)
     *
     * @return
     */
    public List<Protection> loadProtections() {
        try {
            PreparedStatement statement = prepare("SELECT id, owner, type, x, y, z, data, blockId, world, password, date, last_accessed FROM " + prefix + "protections");

            return resolveProtections(statement);
        } catch (Exception e) {
            printException(e);
        }

        return new ArrayList<Protection>();
    }

    /**
     * Load the first protection within a block's radius
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @param radius
     * @return list of Protection objects found
     */
    public List<Protection> loadProtections(String world, int x, int y, int z, int radius) {
        try {
            PreparedStatement statement = prepare("SELECT id, owner, type, x, y, z, data, blockId, world, password, date, last_accessed FROM " + prefix + "protections WHERE world = ? AND x >= ? AND x <= ? AND y >= ? AND y <= ? AND z >= ? AND z <= ?");

            statement.setString(1, world);
            statement.setInt(2, x - radius);
            statement.setInt(3, x + radius);
            statement.setInt(4, y - radius);
            statement.setInt(5, y + radius);
            statement.setInt(6, z - radius);
            statement.setInt(7, z + radius);

            return resolveProtections(statement);
        } catch (Exception e) {
            printException(e);
        }

        return new ArrayList<Protection>();
    }

    /**
     * Load all protections in the coordinate ranges
     *
     * @param world
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @param z1
     * @param z2
     * @return list of Protection objects found
     */
    public List<Protection> loadProtections(String world, int x1, int x2, int y1, int y2, int z1, int z2) {
        try {
            PreparedStatement statement = prepare("SELECT id, owner, type, x, y, z, data, blockId, world, password, date, last_accessed FROM " + prefix + "protections WHERE world = ? AND x >= ? AND x <= ? AND y >= ? AND y <= ? AND z >= ? AND z <= ?");

            statement.setString(1, world);
            statement.setInt(2, x1);
            statement.setInt(3, x2);
            statement.setInt(4, y1);
            statement.setInt(5, y2);
            statement.setInt(6, z1);
            statement.setInt(7, z2);

            return resolveProtections(statement);
        } catch (Exception e) {
            printException(e);
        }

        return new ArrayList<Protection>();
    }

    /**
     * Load protections by a player
     *
     * @param player
     * @return
     */
    public List<Protection> loadProtectionsByPlayer(String player) {
        List<Protection> protections = new ArrayList<Protection>();

        try {
            PreparedStatement statement = prepare("SELECT id, owner, type, x, y, z, data, blockId, world, password, date, last_accessed FROM " + prefix + "protections WHERE owner = ?");
            statement.setString(1, player);

            return resolveProtections(statement);
        } catch (Exception e) {
            printException(e);
        }

        return protections;
    }

    /**
     * Load protections by a player
     *
     * @param player
     * @param start
     * @param count
     * @return
     */
    public List<Protection> loadProtectionsByPlayer(String player, int start, int count) {
        List<Protection> protections = new ArrayList<Protection>();

        try {
            PreparedStatement statement = prepare("SELECT id, owner, type, x, y, z, data, blockId, world, password, date, last_accessed FROM " + prefix + "protections WHERE owner = ? ORDER BY id DESC limit ?,?");
            statement.setString(1, player);
            statement.setInt(2, start);
            statement.setInt(3, count);

            return resolveProtections(statement);
        } catch (Exception e) {
            printException(e);
        }

        return protections;
    }

    /**
     * Register a protection
     *
     * @param blockId
     * @param type
     * @param world
     * @param player
     * @param data
     * @param x
     * @param y
     * @param z
     */
    public Protection registerProtection(int blockId, Protection.Type type, String world, String player, String data, int x, int y, int z) {
        try {
            PreparedStatement statement = prepare("INSERT INTO " + prefix + "protections (blockId, type, world, owner, password, x, y, z, date, last_accessed) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            statement.setInt(1, blockId);
            statement.setInt(2, type.ordinal());
            statement.setString(3, world);
            statement.setString(4, player);
            statement.setString(5, data);
            statement.setInt(6, x);
            statement.setInt(7, y);
            statement.setInt(8, z);
            statement.setString(9, new Timestamp(new Date().getTime()).toString());
            statement.setLong(10, System.currentTimeMillis() / 1000L);

            statement.executeUpdate();

            // remove the null protection from cache if it's in there
            LWC.getInstance().getCaches().getProtections().remove(world + ":" + x + ":" + y + ":" + z);

            // We need to create the initial transaction for this protection
            // this transaction is viewable and modifiable during POST_REGISTRATION
            Protection protection = loadProtection(world, x, y, z);

            // if history logging is enabled, create it
            if (LWC.getInstance().isHistoryEnabled() && protection != null) {
                History transaction = protection.createHistoryObject();

                transaction.setPlayer(player);
                transaction.setType(History.Type.TRANSACTION);
                transaction.setStatus(History.Status.ACTIVE);

                // store the player that created the protection
                transaction.addMetaData("creator=" + player);

                // now sync the history object to the database
                transaction.saveNow();
            }

            // return the newly created protection
            return protection;
        } catch (SQLException e) {
            printException(e);
        }

        return null;
    }

    /**
     * Sync a History object to the database or save a newly created one
     *
     * @param history
     */
    public void saveHistory(History history) {
        try {
            PreparedStatement statement;

            if (history.doesExist()) {
                statement = prepare("UPDATE " + prefix + "history SET protectionId = ?, player = ?, x = ?, y = ?, z = ?, type = ?, status = ?, metadata = ?, timestamp = ? WHERE id = ?");
            } else {
                statement = prepare("INSERT INTO " + prefix + "history (protectionId, player, x, y, z, type, status, metadata, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", true);
                history.setTimestamp(System.currentTimeMillis() / 1000L);
            }

            statement.setInt(1, history.getProtectionId());
            statement.setString(2, history.getPlayer());
            statement.setInt(3, history.getX());
            statement.setInt(4, history.getY());
            statement.setInt(5, history.getZ());
            statement.setInt(6, history.getType().ordinal());
            statement.setInt(7, history.getStatus().ordinal());
            statement.setString(8, history.getSafeMetaData());
            statement.setLong(9, history.getTimestamp());

            if (history.doesExist()) {
                statement.setInt(10, history.getId());
            }

            int affectedRows = statement.executeUpdate();

            // set the history id if inserting
            if (!history.doesExist()) {
                if (affectedRows > 0) {
                    ResultSet generatedKeys = statement.getGeneratedKeys();

                    // get the key inserted
                    if (generatedKeys.next()) {
                        history.setId(generatedKeys.getInt(1));
                    }

                    generatedKeys.close();
                }
            }
        } catch (SQLException e) {
            printException(e);
        }
    }

    /**
     * Resolve 1 history object from the result set but do not close it
     *
     * @return
     */
    public History resolveHistory(History history, ResultSet set) throws SQLException {
        if (history == null) {
            return null;
        }

        int historyId = set.getInt("id");
        int protectionId = set.getInt("protectionId");
        int x = set.getInt("x");
        int y = set.getInt("y");
        int z = set.getInt("z");
        String player = set.getString("player");
        int type_ord = set.getInt("type");
        int status_ord = set.getInt("status");
        String[] metadata = set.getString("metadata").split(",");
        long timestamp = set.getLong("timestamp");

        History.Type type = History.Type.values()[type_ord];
        History.Status status = History.Status.values()[status_ord];

        history.setId(historyId);
        history.setProtectionId(protectionId);
        history.setType(type);
        history.setPlayer(player);
        history.setX(x);
        history.setY(y);
        history.setZ(z);
        history.setStatus(status);
        history.setMetaData(metadata);
        history.setTimestamp(timestamp);

        return history;
    }

    /**
     * Load all of the History objects for a given protection
     *
     * @param protection
     * @return
     */
    public List<History> loadHistory(Protection protection) {
        List<History> temp = new ArrayList<History>();

        if (!LWC.getInstance().isHistoryEnabled()) {
            return temp;
        }

        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "history WHERE protectionId = ? ORDER BY id DESC");
            statement.setInt(1, protection.getId());

            ResultSet set = statement.executeQuery();

            while (set.next()) {
                History history = resolveHistory(protection.createHistoryObject(), set);

                if (history != null) {
                    // seems ok
                    temp.add(history);
                }
            }

        } catch (SQLException e) {
            printException(e);
        }

        return temp;
    }

    /**
     * Load all protection history that the given player created
     *
     * @param player
     * @return
     */
    public List<History> loadHistory(Player player) {
        return loadHistory(player.getName());
    }

    /**
     * Load all protection history that the given player created
     *
     * @param player
     * @return
     */
    public List<History> loadHistory(String player) {
        List<History> temp = new ArrayList<History>();

        if (!LWC.getInstance().isHistoryEnabled()) {
            return temp;
        }

        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "history WHERE LOWER(player) = LOWER(?) ORDER BY id DESC");
            statement.setString(1, player);

            ResultSet set = statement.executeQuery();

            while (set.next()) {
                History history = resolveHistory(new History(), set);

                if (history != null) {
                    // seems ok
                    temp.add(history);
                }
            }

        } catch (SQLException e) {
            printException(e);
        }

        return temp;
    }

    /**
     * Load all protection history that has the given history id
     *
     * @param historyId
     * @return
     */
    public History loadHistory(int historyId) {
        if (!LWC.getInstance().isHistoryEnabled()) {
            return null;
        }

        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "history WHERE id = ?");
            statement.setInt(1, historyId);

            ResultSet set = statement.executeQuery();

            if (set.next()) {
                History history = resolveHistory(new History(), set);

                set.close();
                return history;
            }

            set.close();
        } catch (SQLException e) {
            printException(e);
        }

        return null;
    }

    /**
     * Load all protection history that the given player created for a given page, getting count history items.
     *
     * @param player
     * @param start
     * @param count
     * @return
     */
    public List<History> loadHistory(Player player, int start, int count) {
        return loadHistory(player.getName(), start, count);
    }

    /**
     * Load all protection history that the given player created for a given page, getting count history items.
     *
     * @param player
     * @param start
     * @param count
     * @return
     */
    public List<History> loadHistory(String player, int start, int count) {
        List<History> temp = new ArrayList<History>();

        if (!LWC.getInstance().isHistoryEnabled()) {
            return temp;
        }

        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "history WHERE LOWER(player) = LOWER(?) ORDER BY id DESC LIMIT ?,?");
            statement.setString(1, player);
            statement.setInt(2, start);
            statement.setInt(3, count);

            ResultSet set = statement.executeQuery();

            while (set.next()) {
                History history = resolveHistory(new History(), set);

                if (history != null) {
                    // seems ok
                    temp.add(history);
                }
            }

            set.close();
        } catch (SQLException e) {
            printException(e);
        }

        return temp;
    }

    /**
     * Load all protection history
     *
     * @return
     */
    public List<History> loadHistory() {
        List<History> temp = new ArrayList<History>();

        if (!LWC.getInstance().isHistoryEnabled()) {
            return temp;
        }

        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "history ORDER BY id DESC");
            ResultSet set = statement.executeQuery();

            while (set.next()) {
                History history = resolveHistory(new History(), set);

                if (history != null) {
                    // seems ok
                    temp.add(history);
                }
            }

            set.close();
        } catch (SQLException e) {
            printException(e);
        }

        return temp;
    }

    /**
     * Load all of the history at the given location
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public List<History> loadHistory(int x, int y, int z) {
        List<History> temp = new ArrayList<History>();

        if (!LWC.getInstance().isHistoryEnabled()) {
            return temp;
        }

        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "history WHERE x = ? AND y = ? AND z = ?");
            statement.setInt(1, x);
            statement.setInt(2, y);
            statement.setInt(3, z);

            ResultSet set = statement.executeQuery();

            while (set.next()) {
                History history = resolveHistory(new History(), set);

                if (history != null) {
                    // seems ok
                    temp.add(history);
                }
            }

            set.close();
        } catch (SQLException e) {
            printException(e);
        }

        return temp;
    }

    /**
     * Load all protection history
     *
     * @return
     */
    public List<History> loadHistory(int start, int count) {
        List<History> temp = new ArrayList<History>();

        if (!LWC.getInstance().isHistoryEnabled()) {
            return temp;
        }

        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "history ORDER BY id DESC LIMIT ?,?");
            statement.setInt(1, start);
            statement.setInt(2, count);

            ResultSet set = statement.executeQuery();

            while (set.next()) {
                History history = resolveHistory(new History(), set);

                if (history != null) {
                    // seems ok
                    temp.add(history);
                }
            }

            set.close();
        } catch (SQLException e) {
            printException(e);
        }

        return temp;
    }

    /**
     * Save a job to the database
     *
     * @param job
     */
    public void saveJob(Job job) {
        try {
            PreparedStatement statement = null;
            int index = 1;

            if (job.doesExist()) {
                statement = prepare("REPLACE INTO " + prefix + "jobs (id, name, type, data, nextRun) VALUES (?, ?, ?, ?, ?)");
                statement.setInt(index++, job.getId());
            } else {
                statement = prepare("INSERT INTO " + prefix + "jobs (name, type, data, nextRun) VALUES (?, ?, ?, ?)");
            }

            statement.setString(index++, job.getName());
            statement.setInt(index++, job.getType());
            statement.setString(index++, job.getData().toJSONString());
            statement.setLong(index++, job.getNextRun());
            statement.executeUpdate();

            // check if it was inserted correctly
            if (!job.doesExist()) {
                ResultSet keys = statement.getGeneratedKeys();

                if (keys != null && keys.next()) {
                    job.setId(keys.getInt(1));
                    keys.close();
                }
            }
        } catch (SQLException e) {
            printException(e);
        }
    }

    public void removeJob(Job job) {
        try {
            PreparedStatement statement = prepare("DELETE FROM " + prefix + "jobs WHERE id = ?");

            statement.setInt(1, job.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            printException(e);
        }
    }

    /**
     * Load all of the jobs in the database
     *
     * @return a List of the jobs in the database
     */
    public List<Job> loadJobs() {
        List<Job> jobs = new ArrayList<Job>();

        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "jobs");
            ResultSet set = statement.executeQuery();

            while (set.next()) {
                Job job = new Job();

                int id = set.getInt("id");
                String name = set.getString("name");
                int type = set.getInt("type");
                String data = set.getString("data");
                long nextRun = set.getLong("nextRun");

                job.setId(id);
                job.setName(name);
                job.setType(type);
                job.setData(Job.decodeJSON(data));
                job.setNextRun(nextRun);

                jobs.add(job);
            }

            set.close();
        } catch (SQLException e) {
            printException(e);
        }

        return jobs;
    }

    /**
     * Save a protection to the database
     *
     * @param protection
     */
    public void saveProtection(Protection protection) {
        try {
            PreparedStatement statement = prepare("REPLACE INTO " + prefix + "protections (id, type, blockId, world, data, owner, password, x, y, z, date, last_accessed) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            statement.setInt(1, protection.getId());
            statement.setInt(2, protection.getType().ordinal());
            statement.setInt(3, protection.getBlockId());
            statement.setString(4, protection.getWorld());
            statement.setString(5, protection.getData().toJSONString());
            statement.setString(6, protection.getOwner());
            statement.setString(7, protection.getPassword());
            statement.setInt(8, protection.getX());
            statement.setInt(9, protection.getY());
            statement.setInt(10, protection.getZ());
            statement.setString(11, protection.getDate());
            statement.setLong(12, protection.getLastAccessed());

            statement.executeUpdate();
        } catch (SQLException e) {
            printException(e);
        }
    }

    /**
     * Set the menu style for a place
     *
     * @param player
     * @param menu
     */
    public void setMenuStyle(String player, String menu) {
        try {
            PreparedStatement statement = prepare("REPLACE INTO " + prefix + "menu_styles (player, menu) VALUES (?,?)");
            statement.setString(1, player);
            statement.setString(2, menu);

            statement.executeUpdate();
        } catch (Exception e) {
            printException(e);
        }
    }

    /**
     * Free a chest from protection
     *
     * @param protectionId the protection Id
     */
    public void unregisterProtection(int protectionId) {
        try {
            PreparedStatement statement = prepare("DELETE FROM " + prefix + "protections WHERE id = ?");
            statement.setInt(1, protectionId);

            statement.executeUpdate();
        } catch (SQLException e) {
            printException(e);
        }

        // unregisterProtectionHistory(protectionId);
    }

    public void unregisterProtectionHistory(int protectionId) {
        try {
            PreparedStatement statement = prepare("DELETE FROM " + prefix + "history WHERE protectionId = ?");
            statement.setInt(1, protectionId);

            statement.executeUpdate();
        } catch (SQLException e) {
            printException(e);
        }
    }

    public void unregisterHistory(int historyId) {
        try {
            PreparedStatement statement = prepare("DELETE FROM " + prefix + "history WHERE id = ?");
            statement.setInt(1, historyId);

            statement.executeUpdate();
        } catch (SQLException e) {
            printException(e);
        }
    }

    /**
     * Remove all of the registered chests
     */
    public void unregisterProtections() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM " + prefix + "protections");
            statement.close();

        } catch (SQLException e) {
            printException(e);
        }
    }

    /**
     * Attempt to create an index on the table
     *
     * @param table
     * @param indexName
     * @param columns
     */
    private void doIndex(String table, String indexName, String columns) {
        Statement statement = null;

        try {
            statement = connection.createStatement();
            statement.executeUpdate("CREATE INDEX" + (currentType == Type.SQLite ? " IF NOT EXISTS" : "") + " " + indexName + " ON " + prefix + table + " (" + columns + ")");
        } catch (Exception e) {
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * Attempt to create an index on the table
     *
     * @param indexName
     */
    private void dropIndex(String table, String indexName) {
        Statement statement = null;

        try {
            statement = connection.createStatement();

            if (currentType == Type.SQLite) {
                statement.executeUpdate("DROP INDEX IF EXISTS " + indexName);
            } else {
                statement.executeUpdate("DROP INDEX " + indexName + " ON " + prefix + table);
            }
        } catch (Exception e) {
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 3.01
     */
    private void doUpdate301() {
        // check limits table
        try {
            Statement statement = connection.createStatement();
            statement.executeQuery("SELECT * FROM limits LIMIT 1");
            statement.close();
        } catch (Exception e) {
            return;
        }

        // Convert limits
        LWC lwc = LWC.getInstance();
        Module rawModule = lwc.getModuleLoader().getModule(LimitsModule.class);

        if (rawModule == null) {
            log("Failed to load the Limits module. Something is wrong!");
            return;
        }

        LimitsModule limits = (LimitsModule) rawModule;

        // start going through the database
        PreparedStatement statement = prepare("SELECT * FROM limits");
        try {
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                int type = result.getInt("type");
                int amount = result.getInt("amount");
                String entity = result.getString("entity");

                switch (type) {
                    // Global
                    case 2:
                        limits.set("master.type", "default");
                        limits.set("master.limit", amount);
                        break;

                    // Group
                    case 0:
                        limits.set("groups." + entity + ".type", "default");
                        limits.set("groups." + entity + ".limit", amount);
                        break;

                    // Player
                    case 1:
                        limits.set("players." + entity + ".type", "default");
                        limits.set("players." + entity + ".limit", amount);
                        break;
                }
            }
        } catch (SQLException e) {
            printException(e);
            return;
        }

        limits.save();
        dropTable("limits");
    }

    /**
     * 3.02
     */
    private void doUpdate302() {
        if (prefix == null || prefix.length() == 0) {
            return;
        }

        // check for the table
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute("SELECT id FROM " + prefix + "protections limit 1");
        } catch (SQLException e) {
            // The table does not exist, let's go ahead and rename all of the tables
            renameTable("protections", prefix + "protections");
            renameTable("rights", prefix + "rights");
            renameTable("menu_styles", prefix + "menu_styles");
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 3.30
     */
    private void doUpdate330() {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute("SELECT last_accessed FROM " + prefix + "protections LIMIT 1");
        } catch (SQLException e) {
            addColumn(prefix + "protections", "last_accessed", "INTEGER");
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 4.0.0, update 1
     */
    private void doUpdate400_1() {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute("SELECT rights FROM " + prefix + "protections LIMIT 1");
        } catch (SQLException e) {
            addColumn(prefix + "protections", "rights", "TEXT");
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 4.0.0, update 2
     */
    private void doUpdate400_2() {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute("SELECT id FROM " + prefix + "rights LIMIT 1");

            // it exists ..!
            Statement stmt = connection.createStatement();
            ResultSet set = stmt.executeQuery("SELECT * FROM " + prefix + "rights");

            // keep a mini-cache of protections, max size of 100k should be OK!
            LRUCache<Integer, Protection> cache = new LRUCache<Integer, Protection>(1000 * 100);

            while (set.next()) {
                // load the data we will be using
                int protectionId = set.getInt("chest");
                String entity = set.getString("entity");
                int rights = set.getInt("rights");
                int type = set.getInt("type");

                // begin loading the protection
                Protection protection = null;

                // check cache
                if (cache.containsKey(protectionId)) {
                    protection = cache.get(protectionId);
                } else {
                    // else, load it...
                    protection = loadProtection(protectionId);
                    cache.put(protectionId, protection);
                }

                if (protection == null) {
                    continue;
                }

                // create the access right
                AccessRight right = new AccessRight();
                right.setProtectionId(protectionId);
                right.setType(type);
                right.setRights(rights);
                right.setName(entity);

                // add it to the protection and queue it for saving!
                protection.addAccessRight(right);
                protection.save();
            }

            // drop the rights table
            dropTable(prefix + "rights");

            // Good!
            set.close();
            stmt.close();
        } catch (SQLException e) {
            // no need to convert!
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 4.0.0, update 3
     */
    private void doUpdate400_3() {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute("SELECT name FROM " + prefix + "jobs LIMIT 1");
        } catch (SQLException e) {
            addColumn(prefix + "jobs", "name", "VARCHAR(64)");
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 4.0.0, update 4
     */
    private void doUpdate400_4() {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute("SELECT data FROM " + prefix + "protections LIMIT 1");
        } catch (SQLException e) {
            dropColumn(prefix + "protections", "rights");
            addColumn(prefix + "protections", "data", "TEXT");
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 4.0.0, update 5
     */
    private void doUpdate400_5() {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeQuery("SELECT flags FROM " + prefix + "protections LIMIT 1");

            // The flags column is still there ..!
            // instead of looping through every protection, let's do this a better way
            PreparedStatement pStatement = prepare("SELECT * FROM " + prefix + "protections WHERE flags = 8"); // exempt

            for (Protection protection : resolveProtections(pStatement)) {
                Flag flag = new Flag(Flag.Type.EXEMPTION);
                protection.addFlag(flag);
                protection.save();
            }

            pStatement = prepare("SELECT * FROM " + prefix + "protections WHERE flags = 3"); // redstone

            for (Protection protection : resolveProtections(pStatement)) {
                Flag flag = new Flag(Flag.Type.MAGNET);
                protection.addFlag(flag);
                protection.save();
            }

            pStatement = prepare("SELECT * FROM " + prefix + "protections WHERE flags = 2"); // redstone

            for (Protection protection : resolveProtections(pStatement)) {
                Flag flag = new Flag(Flag.Type.REDSTONE);
                protection.addFlag(flag);
                protection.save();
            }

            dropColumn(prefix + "protections", "flags");
        } catch (SQLException e) {

        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 4.0.0, update 6 (alpha7)
     */
    private void doUpdate400_6() {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeQuery("SELECT x FROM " + prefix + "history LIMIT 1");
        } catch (SQLException e) {
            //  add x, y, z
            addColumn(prefix + "history", "x", "INTEGER");
            addColumn(prefix + "history", "y", "INTEGER");
            addColumn(prefix + "history", "z", "INTEGER");
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
    }

}
