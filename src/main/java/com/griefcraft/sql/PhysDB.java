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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.griefcraft.cache.LRUCache;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.AccessRight;
import com.griefcraft.model.Protection;
import com.griefcraft.modules.limits.LimitsModule;
import com.griefcraft.scripting.Module;
import com.griefcraft.util.Performance;

public class PhysDB extends Database {

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
     * Count the rights
     *
     * @param protectionId
     * @return
     */
    public int countRightsForProtection(int protectionId) {
        return Integer.decode(fetch("SELECT COUNT(*) AS count FROM " + prefix + "rights WHERE chest=?", "count", protectionId) + "");
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
     * Get the access level of a player to a chest -1 = no access 0 = normal access 1 = chest admin
     *
     * @param type
     * @param protectionId
     * @return the player's access level
     */
    @Deprecated
    public int getPrivateAccess(int type, int protectionId, String... entities) {
        int access = -1;

        try {
            PreparedStatement statement = prepare("SELECT entity, rights FROM " + prefix + "rights WHERE type = ? AND chest = ?");
            statement.setInt(1, type);
            statement.setInt(2, protectionId);

            ResultSet set = statement.executeQuery();

            _main:
            while (set.next()) {
                String entity = set.getString("entity");

                for (String str : entities) {
                    if (str.equalsIgnoreCase(entity)) {
                        access = set.getInt("rights");
                        break _main;
                    }
                }
            }

            set.close();

        } catch (SQLException e) {
            printException(e);
        }

        return access;
    }

    /**
     * @return the number of protected chests
     */
    public int getProtectionCount() {
        return Integer.decode(fetch("SELECT COUNT(*) AS count FROM " + prefix + "protections", "count") + "");
    }

    /**
     * Get the amount of protections a player has
     *
     * @param player
     * @return the amount of protections they have
     */
    public int getProtectionCount(String player) {
        int amount = 0;

        try {
            PreparedStatement statement = prepare("SELECT id FROM " + prefix + "protections WHERE owner = ?");
            statement.setString(1, player);

            ResultSet set = statement.executeQuery();

            while (set.next()) {
                amount++;
            }

            set.close();
        } catch (SQLException e) {
            printException(e);
        }

        return amount;
    }

    /**
     * Get the amount of chests a player has of a specific block id
     *
     * @param player
     * @return the amount of protections they have of blockId
     */
    public int getProtectionCount(String player, int blockId) {
        int amount = 0;

        try {
            PreparedStatement statement = prepare("SELECT id FROM " + prefix + "protections WHERE owner = ? AND blockId = ?");
            statement.setString(1, player);
            statement.setInt(2, blockId);

            ResultSet set = statement.executeQuery();

            while (set.next()) {
                amount++;
            }

            set.close();
        } catch (SQLException e) {
            printException(e);
        }

        return amount;
    }

    /**
     * @return the number of limits
     */
    public int getRightsCount() {
        return Integer.decode(fetch("SELECT COUNT(*) AS count FROM " + prefix + "rights", "count") + "");
    }

    /**
     * Create the table needed if it does not already exist
     */
    @Override
    public void load() {
        if (loaded) {
            return;
        }

        /**
         * Updates that alter or rename a table go here
         */
        doUpdate140();
        doUpdate150();
        doUpdate170();
        doUpdate220();
        doUpdate301();
        doUpdate302();
        doUpdate330();

        try {
            connection.setAutoCommit(false);

            Column column;

            Table protections = new Table(this, "protections");

            {
                column = new Column("id");
                column.setType("INTEGER");
                column.setPrimary(true);
                protections.addColumn(column);

                column = new Column("type");
                column.setType("INTEGER");
                protections.addColumn(column);

                column = new Column("flags");
                column.setType("INTEGER");
                protections.addColumn(column);

                column = new Column("blockId");
                column.setType("INTEGER");
                protections.addColumn(column);

                column = new Column("world");
                column.setType("VARCHAR(255)");
                protections.addColumn(column);

                column = new Column("owner");
                column.setType("VARCHAR(255)");
                protections.addColumn(column);

                column = new Column("password");
                column.setType("VARCHAR(255)");
                protections.addColumn(column);

                column = new Column("x");
                column.setType("INTEGER");
                protections.addColumn(column);

                column = new Column("y");
                column.setType("INTEGER");
                protections.addColumn(column);

                column = new Column("z");
                column.setType("INTEGER");
                protections.addColumn(column);

                column = new Column("date");
                column.setType("VARCHAR(255)");
                protections.addColumn(column);
                
                column = new Column("last_accessed");
                column.setType("INTEGER");
                protections.addColumn(column);
            }

            Table rights = new Table(this, "rights");

            {
                column = new Column("id");
                column.setType("INTEGER");
                column.setPrimary(true);
                rights.addColumn(column);

                column = new Column("chest");
                column.setType("INTEGER");
                rights.addColumn(column);

                column = new Column("entity");
                column.setType("TEXT");
                rights.addColumn(column);

                column = new Column("rights");
                column.setType("INTEGER");
                rights.addColumn(column);

                column = new Column("type");
                column.setType("INTEGER");
                rights.addColumn(column);
            }

            Table menuStyles = new Table(this, "menu_styles");

            {
                column = new Column("player");
                column.setType("VARCHAR(255)");
                column.setPrimary(true);
                column.setAutoIncrement(false);
                menuStyles.addColumn(column);

                column = new Column("menu");
                column.setType("VARCHAR(255)");
                menuStyles.addColumn(column);
            }

            protections.execute();
            rights.execute();
            menuStyles.execute();

            connection.commit();

            doIndexes();
        } catch (SQLException e) {
            printException(e);
        }

        try {
            connection.setAutoCommit(true);
        } catch (Exception e) {
        	printException(e);
        }

        doUpdate100();

        loaded = true;
    }

    /**
     * Get the rights for a protection id ranging from start-max
     *
     * @param protectionId
     * @param start
     * @param max
     * @return
     */
    public List<AccessRight> loadAccessRights(int protectionId, int start, int max) {
        List<AccessRight> accessRights = new ArrayList<AccessRight>();

        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "rights WHERE chest = ? ORDER BY rights DESC LIMIT ?,?");
            statement.setInt(1, protectionId);
            statement.setInt(2, start);
            statement.setInt(3, max);

            ResultSet set = statement.executeQuery();

            while (set.next()) {
                AccessRight accessRight = new AccessRight();

                accessRight.setId(set.getInt("id"));
                accessRight.setProtectionId(protectionId);
                accessRight.setName(set.getString("entity"));
                accessRight.setRights(set.getInt("rights"));
                accessRight.setType(set.getInt("type"));

                accessRights.add(accessRight);
            }

            set.close();
        } catch (Exception e) {
            printException(e);
        }

        return accessRights;
    }

    /**
     * Load a protection with the given id
     *
     * @param protectionId
     * @return the Chest object
     */
    public Protection loadProtection(int protectionId) {
        try {
            PreparedStatement statement = prepare("SELECT " + prefix + "protections.id AS protectionId, " + prefix + "rights.id AS rightsId, " + prefix + "protections.type AS protectionType, " + prefix + "rights.type AS rightsType, x, y, z, flags, blockId, world, owner, password, date, entity, rights, last_accessed FROM " + prefix + "protections LEFT OUTER JOIN " + prefix + "rights ON " + prefix + "protections.id = " + prefix + "rights.chest WHERE " + prefix + "protections.id = ?");
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
    public List<Protection> loadProtectionsUsingType(int type) {
        try {
            PreparedStatement statement = prepare("SELECT " + prefix + "protections.id AS protectionId, " + prefix + "rights.id AS rightsId, " + prefix + "protections.type AS protectionType, " + prefix + "rights.type AS rightsType, x, y, z, flags, blockId, world, owner, password, date, entity, rights, last_accessed FROM " + prefix + "protections LEFT OUTER JOIN " + prefix + "rights ON " + prefix + "protections.id = " + prefix + "rights.chest WHERE " + prefix + "protections.type = ?");
            statement.setInt(1, type);

            return resolveProtections(statement);
        } catch (SQLException e) {
            printException(e);
        }

        return new ArrayList<Protection>();
    }

    /**
     * Resolve a list of n protections from a statement
     *
     * @param statement
     * @return
     */
    private List<Protection> resolveProtections(PreparedStatement statement) {
        List<Protection> protections = new ArrayList<Protection>();

        int lastId = -1;
        ResultSet set = null;
        Protection protection = null;
        boolean init = true;

        try {
            set = statement.executeQuery();

            while (set.next()) {
                int protectionId = set.getInt("protectionId");

                if (lastId != protectionId) {
                    // add the last found protection
                    if (protection != null) {
                        protections.add(protection);
                    }

                    lastId = protectionId;
                    init = true;
                    protection = new Protection();
                }

                // we only want to set the initial data first
                if (init) {
                    int x = set.getInt("x");
                    int y = set.getInt("y");
                    int z = set.getInt("z");
                    int flags = set.getInt("flags");
                    int blockId = set.getInt("blockId");
                    int type = set.getInt("protectionType");
                    String world = set.getString("world");
                    String owner = set.getString("owner");
                    String password = set.getString("password");
                    String date = set.getString("date");
                    long lastAccessed = set.getLong("last_accessed");

                    protection.setId(protectionId);
                    protection.setX(x);
                    protection.setY(y);
                    protection.setZ(z);
                    protection.setFlags(flags);
                    protection.setBlockId(blockId);
                    protection.setType(type);
                    protection.setWorld(world);
                    protection.setOwner(owner);
                    protection.setData(password);
                    protection.setDate(date);
                    protection.setLastAccessed(lastAccessed);
                    init = false;
                }

                // check for oh so beautiful rights!
                String entity = set.getString("entity");

                // we have joined in some rights! Rev up those accessors!
                if (entity != null) {
                    int rightsId = set.getInt("rightsId");
                    int rights = set.getInt("rights");
                    int type = set.getInt("rightsType");

                    AccessRight right = new AccessRight();
                    right.setId(rightsId);
                    right.setProtectionId(protectionId);
                    right.setName(entity);
                    right.setRights(rights);
                    right.setType(type);

                    protection.addAccessRight(right);
                }
            }

            if (protection != null && !protections.contains(protection)) {
                protections.add(protection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
    private Protection resolveProtection(PreparedStatement statement) {
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

        int precacheSize = lwc.getConfiguration().getInt("core.precache", -1);
        
        if(precacheSize == -1) {
        	precacheSize = lwc.getConfiguration().getInt("core.cacheSize", 10000);
        }
        
        try {
            PreparedStatement statement = prepare("SELECT " + prefix + "protections.id AS protectionId, " + prefix + "rights.id AS rightsId, " + prefix + "protections.type AS protectionType, " + prefix + "rights.type AS rightsType, x, y, z, flags, blockId, world, owner, password, date, entity, rights, last_accessed FROM " + prefix + "protections LEFT OUTER JOIN " + prefix + "rights ON " + prefix + "protections.id = " + prefix + "rights.chest ORDER BY " + prefix + "protections.id DESC LIMIT ?");
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

    /** Used for the Bukkit #656 workaround to add a "cached" protection node when we find a
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
    
    /** Return the cached Protection for a given block (if any). 
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
            PreparedStatement statement = prepare("SELECT " + prefix + "protections.id AS protectionId, " + prefix + "rights.id AS rightsId, " + prefix + "protections.type AS protectionType, " + prefix + "rights.type AS rightsType, x, y, z, flags, blockId, world, owner, password, date, entity, rights, last_accessed FROM " + prefix + "protections LEFT OUTER JOIN " + prefix + "rights ON " + prefix + "protections.id = " + prefix + "rights.chest WHERE " + prefix + "protections.x = ? AND " + prefix + "protections.y = ? AND " + prefix + "protections.z = ?");
            statement.setInt(1, x);
            statement.setInt(2, y);
            statement.setInt(3, z);

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
     * Load every protection, use sparingly!
     *
     * @return
     */
    public List<Protection> loadProtections() {
        try {
            PreparedStatement statement = prepare("SELECT " + prefix + "protections.id AS protectionId, " + prefix + "rights.id AS rightsId, " + prefix + "protections.type AS protectionType, " + prefix + "rights.type AS rightsType, x, y, z, flags, blockId, world, owner, password, date, entity, rights, last_accessed FROM " + prefix + "protections LEFT OUTER JOIN " + prefix + "rights ON " + prefix + "protections.id = " + prefix + "rights.chest");

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
            PreparedStatement statement = prepare("SELECT " + prefix + "protections.id AS protectionId, " + prefix + "rights.id AS rightsId, " + prefix + "protections.type AS protectionType, " + prefix + "rights.type AS rightsType, x, y, z, flags, blockId, world, owner, password, date, entity, rights, last_accessed FROM " + prefix + "protections LEFT OUTER JOIN " + prefix + "rights ON " + prefix + "protections.id = " + prefix + "rights.chest WHERE " + prefix + "protections.world = ? AND " + prefix + "protections.x >= ? AND " + prefix + "protections.x <= ? AND " + prefix + "protections.y >= ? AND " + prefix + "protections.y <= ? AND " + prefix + "protections.z >= ? AND " + prefix + "protections.z <= ?");

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
            PreparedStatement statement = prepare("SELECT " + prefix + "protections.id AS protectionId, " + prefix + "rights.id AS rightsId, " + prefix + "protections.type AS protectionType, " + prefix + "rights.type AS rightsType, x, y, z, flags, blockId, world, owner, password, date, entity, rights, last_accessed FROM " + prefix + "protections LEFT OUTER JOIN " + prefix + "rights ON " + prefix + "protections.id = " + prefix + "rights.chest WHERE " + prefix + "protections.owner = ? ORDER BY " + prefix + "protections.id DESC limit ?,?");
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
     * Get all access rights for a protection
     *
     * @return
     */
    public List<AccessRight> loadRights(int protectionId) {
        List<AccessRight> accessRights = new ArrayList<AccessRight>();

        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "rights WHERE chest = ?");
            statement.setInt(1, protectionId);

            ResultSet set = statement.executeQuery();

            while (set.next()) {
                AccessRight accessRight = new AccessRight();

                accessRight.setId(set.getInt("id"));
                accessRight.setProtectionId(set.getInt("chest"));
                accessRight.setName(set.getString("entity"));
                accessRight.setRights(set.getInt("rights"));
                accessRight.setType(set.getInt("type"));

                accessRights.add(accessRight);
            }

            set.close();
        } catch (Exception e) {
            printException(e);
        }

        return accessRights;
    }

    /**
     * Add a chest to the protected chests
     *
     * @param player   the player that owns the chest
     * @param password the password of the chest
     * @param x        the x coordinate of the chest
     * @param y        the y coordinate of the chest
     * @param z        the z coordinate of the chest
     */
    public Protection registerProtection(int blockId, int type, String world, String player, String password, int x, int y, int z) {
        try {
            PreparedStatement statement = prepare("INSERT INTO " + prefix + "protections (blockId, type, world, owner, password, x, y, z, date, last_accessed) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            statement.setInt(1, blockId);
            statement.setInt(2, type);
            statement.setString(3, world);
            statement.setString(4, player);
            statement.setString(5, password);
            statement.setInt(6, x);
            statement.setInt(7, y);
            statement.setInt(8, z);
            statement.setString(9, new Timestamp(new Date().getTime()).toString());
            statement.setLong(10, System.currentTimeMillis() / 1000L);

            statement.executeUpdate();

            // remove the null protection from cache if it's in there
            LWC.getInstance().getCaches().getProtections().remove(world + ":" + x + ":" + y + ":" + z);
            
            // return the newly created protection
            return loadProtection(world, x, y, z);
        } catch (SQLException e) {
            printException(e);
        }
        
        return null;
    }

    /**
     * Register rights to a chest
     *
     * @param protectionId the protectionId to add to
     * @param entity       the entity to register
     * @param rights       the rights to register
     * @param type         the type to register
     */
    public void registerProtectionRights(int protectionId, String entity, int rights, int type) {
        try {
            PreparedStatement statement = prepare("INSERT INTO " + prefix + "rights (chest, entity, rights, type) VALUES (?, ?, ?, ?)");

            statement.setInt(1, protectionId);
            statement.setString(2, entity.toLowerCase());
            statement.setInt(3, rights);
            statement.setInt(4, type);

            statement.executeUpdate();

        } catch (SQLException e) {
            printException(e);
        }
    }

    /**
     * Save a protection to the database
     *
     * @param protection
     */
    public void saveProtection(Protection protection) {
        try {
            PreparedStatement statement = prepare("REPLACE INTO " + prefix + "protections (id, type, blockId, world, flags, owner, password, x, y, z, date, last_accessed) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            statement.setInt(1, protection.getId());
            statement.setInt(2, protection.getType());
            statement.setInt(3, protection.getBlockId());
            statement.setString(4, protection.getWorld());
            statement.setInt(5, protection.getFlags());
            statement.setString(6, protection.getOwner());
            statement.setString(7, protection.getData());
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

        unregisterProtectionRights(protectionId);
    }

    /**
     * Remove all protection rights
     */
    public void unregisterProtectionRights() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM " + prefix + "rights");
            statement.close();
        } catch (Exception e) {
            printException(e);
        }
    }

    /**
     * Remove all of the rights from a chest
     *
     * @param chestID the chest ID
     */
    public void unregisterProtectionRights(int chestID) {
        try {
            PreparedStatement statement = prepare("DELETE FROM " + prefix + "rights WHERE chest = ?");

            statement.setInt(1, chestID);
            statement.executeUpdate();

        } catch (SQLException e) {
            printException(e);
        }
    }

    /**
     * Remove all of the rights from a chest
     *
     * @param chestID the chest ID
     */
    public void unregisterProtectionRights(int chestID, String entity) {
        try {
            PreparedStatement statement = prepare("DELETE FROM " + prefix + "rights WHERE chest = ? AND entity = ?");

            statement.setInt(1, chestID);
            statement.setString(2, entity.toLowerCase());

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
     * Instead of "updating indexes", let's just use IF NOT EXISTS each time
     */
    private void doIndexes() {
        try {
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();

            if (currentType == Type.SQLite) {
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS in1 ON " + prefix + "protections (owner, x, y, z)");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS in3 ON " + prefix + "rights (chest, entity)");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS in6 ON " + prefix + "protections (id)");
            } else {
                statement.executeUpdate("CREATE INDEX in1 ON " + prefix + "protections (x, y, z)");
                statement.executeUpdate("CREATE INDEX in3 ON " + prefix + "rights (chest)");
                statement.executeUpdate("CREATE INDEX in6 ON " + prefix + "protections (id)");
            }

            connection.commit();

            statement.close();
        } catch (Exception e) {
            // printException(e);
        }

        try {
            connection.setAutoCommit(true);
        } catch (Exception e) {
        }
    }

    /**
     * Update process from 1.00 -> 1.10
     */
    private void doUpdate100() {
        try {
            Statement statement = connection.createStatement();
            statement.executeQuery("SELECT type FROM protections");
            statement.close();

        } catch (SQLException e) {
            addColumn("protections", "type", "INTEGER");
            executeUpdateNoException("UPDATE protections SET type='1'");
        }
    }

    /**
     * Upgrade process for 1.40, rename table protections to protections
     */
    private void doUpdate140() {
        try {
            Statement statement = connection.createStatement();
            statement.executeQuery("SELECT id FROM protections");
            statement.close();

        } catch (Exception e) {
            renameTable("chests", "protections");
        }
    }

    /**
     * Update to 150, altered a table
     */
    private void doUpdate150() {
        try {
            Statement statement = connection.createStatement();
            statement.executeQuery("SELECT blockId FROM protections");
            statement.close();

        } catch (Exception e) {
            addColumn("protections", "blockId", "INTEGER");
        }
    }

    /**
     * Update to 1.70, altered a table
     */
    private void doUpdate170() {
        try {
            Statement statement = connection.createStatement();
            statement.executeQuery("SELECT world FROM protections");
            statement.close();

        } catch (Exception e) {
            addColumn("protections", "world", "TEXT");
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
            statement.execute("SELECT * FROM " + prefix + "protections");
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
            statement.execute("SELECT last_accessed FROM " + prefix + "protections");
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
     * Update to 2.20, altered a table
     */
    private void doUpdate220() {
        try {
            Statement statement = connection.createStatement();
            statement.executeQuery("SELECT flags FROM protections");
            statement.close();

        } catch (Exception e) {
            addColumn("protections", "flags", "INTEGER");
        }
    }
}
