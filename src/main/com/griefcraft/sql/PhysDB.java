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
import com.griefcraft.model.Limit;
import com.griefcraft.model.Protection;
import com.griefcraft.util.Performance;

public class PhysDB extends Database {

	/**
	 * If the database was already loaded
	 */
	private boolean loaded = false;

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
		return Integer.decode(fetch("SELECT COUNT(*) AS count FROM rights WHERE chest=?", "count", protectionId) + "");
	}

	/**
	 * Insert or update an inventory in the database
	 * 
	 * @param protectionId
	 * @param slots
	 * @param stacks
	 * @param items
	 * @param durability
	 * @param last_transaction
	 * @param last_update
	 */
	public void createInventory(int protectionId, int slots, String stacks, String items, String durability, String last_transaction, String last_update) {
		try {
			PreparedStatement statement = prepare("REPLACE INTO inventory (protectionId, slots, stacks, items, durability, last_transaction, last_update) VALUES (?, ?, ?, ?, ?, ?, ?)");
			statement.setInt(1, protectionId);
			statement.setInt(2, slots);
			statement.setString(3, stacks);
			statement.setString(4, items);
			statement.setString(5, durability);
			statement.setString(6, last_transaction);
			statement.setString(7, last_update);

			statement.executeUpdate();
		} catch (Exception e) {
			printException(e);
		}
	}

	/**
	 * Schedule a Job
	 * 
	 * @param type
	 * @param owner
	 * @param payload
	 */
	public void createJob(int type, String owner, String payload) {
		try {
			PreparedStatement statement = prepare("INSERT INTO jobs (type, owner, payload, timestamp) VALUES(?, ?, ?, ?)");

			statement.setInt(1, type);
			statement.setString(2, owner);
			statement.setString(3, payload);
			statement.setLong(4, System.currentTimeMillis() / 1000L);

			statement.executeUpdate();

		} catch (Exception e) {
			printException(e);
		}
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
			PreparedStatement statement = prepare("SELECT menu FROM menu_styles WHERE player = ?");
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
	 * @param player
	 * @param chestID
	 * @return the player's access level
	 * @see Protection.getAccess(int, string)
	 */
	@Deprecated
	public int getPrivateAccess(int type, int chestID, String... entities) {
		int access = -1;

		try {
			PreparedStatement statement = prepare("SELECT entity, rights FROM rights WHERE type = ? AND chest = ?");
			statement.setInt(1, type);
			statement.setInt(2, chestID);

			ResultSet set = statement.executeQuery();

			_main: while (set.next()) {
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
		return Integer.decode(fetch("SELECT COUNT(*) AS count FROM protections", "count") + "");
	}

	/**
	 * Get the amount of chests a player has
	 * 
	 * @param user
	 *            the player to check
	 * @return the amount of chests they have locked
	 */
	public int getProtectionCount(String user) {
		int amount = 0;

		try {
			PreparedStatement statement = prepare("SELECT id FROM protections WHERE owner = ?");
			statement.setString(1, user);

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
		return Integer.decode(fetch("SELECT COUNT(*) AS count FROM rights", "count") + "");
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
			PreparedStatement statement = prepare("SELECT * FROM rights WHERE chest = ? ORDER BY rights DESC LIMIT ?,?");
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
	 * Get all limits
	 * 
	 * @return
	 */
	public List<Limit> loadLimits() {
		List<Limit> limits = new ArrayList<Limit>();

		try {
			PreparedStatement statement = prepare("SELECT * FROM limits");

			ResultSet set = statement.executeQuery();

			while (set.next()) {
				Limit limit = new Limit();

				limit.setId(set.getInt("id"));
				limit.setType(set.getInt("type"));
				limit.setAmount(set.getInt("amount"));
				limit.setEntity(set.getString("entity"));

				limits.add(limit);
			}

			set.close();
		} catch (Exception e) {
			printException(e);
		}

		return limits;
	}

	/**
	 * Load a chest at a given tile
	 * 
	 * @param protectionId
	 *            the chest's ID
	 * @return the Chest object
	 */
	public Protection loadProtection(int protectionId) {
		try {
			PreparedStatement statement = prepare("SELECT protections.id AS protectionId, rights.id AS rightsId, protections.type AS protectionType, rights.type AS rightsType, x, y, z, flags, blockId, world, owner, password, date, entity, rights FROM protections LEFT OUTER JOIN rights ON protections.id = rights.chest WHERE protections.id = ?");
			statement.setInt(1, protectionId);

			return resolveProtection(statement);
		} catch (SQLException e) {
			printException(e);
		}

		return null;
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

				if(lastId != protectionId) {
					// add the last found protection
					if(protection != null) {
						protections.add(protection);
					}

					lastId = protectionId;
					init = true;
					protection = new Protection();
				}

				// we only want to set the initial data first
				if(init) {
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
					init = false;
				}

				// check for oh so beautiful rights!
				String entity = set.getString("entity");

				// we have joined in some rights! Rev up those accessors!
				if(entity != null) {
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

			if(protection != null && !protections.contains(protection)) {
				protections.add(protection);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			if(set != null) {
				try {
					set.close();
				} catch(SQLException e) { }
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

		if(protections.size() == 0) {
			return null;
		}

		return protections.get(0);
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
		if(cache.containsKey(cacheKey)) {
			return cache.get(cacheKey);
		}

		try {
			PreparedStatement statement = prepare("SELECT protections.id AS protectionId, rights.id AS rightsId, protections.type AS protectionType, rights.type AS rightsType, x, y, z, flags, blockId, world, owner, password, date, entity, rights FROM protections LEFT OUTER JOIN rights ON protections.id = rights.chest WHERE protections.x = ? AND protections.y = ? AND protections.z = ?");
			statement.setInt(1, x);
			statement.setInt(2, y);
			statement.setInt(3, z);

			Protection protection = resolveProtection(statement);

			// cache the protection
			if(protection != null) {
				cache.put(cacheKey, protection);
			}

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
		List<Protection> protections = new ArrayList<Protection>();

		try {
			PreparedStatement statement = prepare("SELECT protections.id AS protectionId, rights.id AS rightsId, protections.type AS protectionType, rights.type AS rightsType, x, y, z, flags, blockId, world, owner, password, date, entity, rights FROM protections LEFT OUTER JOIN rights ON protections.id = rights.chest");

			return resolveProtections(statement);
		} catch (Exception e) {
			printException(e);
		}

		return protections;
	}

	/**
	 * Load the first chest within a block's radius
	 * 
	 * @param x
	 *            the block's x coordinate
	 * @param y
	 *            the block's y coordinate
	 * @param z
	 *            the block's z coordinate
	 * @param radius
	 *            the radius to search
	 * @return the Chest found , null otherwise
	 */
	public List<Protection> loadProtections(String world, int _x, int _y, int _z, int radius) {
		List<Protection> chests = new ArrayList<Protection>();

		try {
			PreparedStatement statement = prepare("SELECT protections.id AS protectionId, rights.id AS rightsId, protections.type AS protectionType, rights.type AS rightsType, x, y, z, flags, blockId, world, owner, password, date, entity, rights FROM protections LEFT OUTER JOIN rights ON protections.id = rights.chest WHERE protections.world = ? AND protections.x >= ? AND protections.x <= ? AND protections.y >= ? AND protections.y <= ? AND protections.z >= ? AND protections.z <= ?");

			statement.setString(1, world);
			statement.setInt(2, _x - radius);
			statement.setInt(3, _x + radius);
			statement.setInt(4, _y - radius);
			statement.setInt(5, _y + radius);
			statement.setInt(6, _z - radius);
			statement.setInt(7, _z + radius);

			return resolveProtections(statement);
		} catch (Exception e) {
			printException(e);
		}

		return chests;
	}

	/**
	 * Load protections by a player, utilizing limits
	 * 
	 * @param player
	 * @param start
	 * @param max
	 * @return
	 */
	public List<Protection> loadProtectionsByPlayer(String player, int start, int count) {
		List<Protection> protections = new ArrayList<Protection>();

		try {
			PreparedStatement statement = prepare("SELECT protections.id AS protectionId, rights.id AS rightsId, protections.type AS protectionType, rights.type AS rightsType, x, y, z, flags, blockId, world, owner, password, date, entity, rights FROM protections LEFT OUTER JOIN rights ON protections.id = rights.chest WHERE protections.owner = ? ORDER BY protections.id DESC limit ?,?");
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
	 * Get all access rights
	 * 
	 * @return
	 */
	public List<AccessRight> loadRights() {
		List<AccessRight> accessRights = new ArrayList<AccessRight>();

		try {
			PreparedStatement statement = prepare("SELECT * FROM rights");

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
	 * Get all access rights for a protection
	 * 
	 * @return
	 */
	public List<AccessRight> loadRights(int protectionId) {
		List<AccessRight> accessRights = new ArrayList<AccessRight>();

		try {
			PreparedStatement statement = prepare("SELECT * FROM rights WHERE chest = ?");
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
	 * @param player
	 *            the player that owns the chest
	 * @param password
	 *            the password of the chest
	 * @param x
	 *            the x coordinate of the chest
	 * @param y
	 *            the y coordinate of the chest
	 * @param z
	 *            the z coordinate of the chest
	 */
	public void registerProtection(int blockId, int type, String world, String player, String password, int x, int y, int z) {
		try {
			PreparedStatement statement = prepare("INSERT INTO protections (blockId, type, world, owner, password, x, y, z, date) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");

			statement.setInt(1, blockId);
			statement.setInt(2, type);
			statement.setString(3, world);
			statement.setString(4, player);
			statement.setString(5, password);
			statement.setInt(6, x);
			statement.setInt(7, y);
			statement.setInt(8, z);
			statement.setString(9, new Timestamp(new Date().getTime()).toString());

			statement.executeUpdate();

		} catch (SQLException e) {
			printException(e);
		}
	}

	/**
	 * Register a limit
	 * 
	 * @param type
	 *            the type to register
	 * @param data
	 *            the user/group to register
	 */
	public void registerProtectionLimit(int type, int amount, String entity) {
		try {
			unregisterProtectionLimit(type, entity.toLowerCase());

			PreparedStatement statement = prepare("INSERT INTO limits (type, amount, entity) VALUES(?, ?, ?)");

			statement.setInt(1, type);
			statement.setInt(2, amount);
			statement.setString(3, entity.toLowerCase());

			statement.executeUpdate();

		} catch (SQLException e) {
			printException(e);
		}
	}

	/**
	 * Register rights to a chest
	 * 
	 * @param protectionId
	 *            the protectionId to add to
	 * @param entity
	 *            the entity to register
	 * @param rights
	 *            the rights to register
	 * @param type
	 *            the type to register
	 */
	public void registerProtectionRights(int protectionId, String entity, int rights, int type) {
		try {
			PreparedStatement statement = prepare("INSERT INTO rights (chest, entity, rights, type) VALUES (?, ?, ?, ?)");

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
			PreparedStatement statement = prepare("REPLACE INTO protections (id, type, blockId, world, flags, owner, password, x, y, z, date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

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

			statement.executeUpdate();

		} catch(SQLException e) {
			printException(e);
		}
	}

	/**
	 * Remove a scheduled job
	 * 
	 * @param id
	 */
	public void removeJob(int id) {
		try {
			PreparedStatement statement = prepare("DELETE FROM jobs WHERE id = ?");

			statement.setInt(1, id);

			statement.executeUpdate();

		} catch (Exception e) {
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
			PreparedStatement statement = prepare("REPLACE INTO menu_styles (player, menu) VALUES (?,?)");
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
	 * @param protectionId
	 *            the protection Id
	 */
	public void unregisterProtection(int protectionId) {
		try {
			PreparedStatement statement = prepare("DELETE FROM protections WHERE id = ?");

			statement.setInt(1, protectionId);

			statement.executeUpdate();

		} catch (SQLException e) {
			printException(e);
		}

		unregisterProtectionRights(protectionId);
	}

	/**
	 * Remove all protections made by a player
	 * 
	 * @param player
	 */
	public void unregisterProtectionByPlayer(String player) {
		try {
			PreparedStatement statement = prepare("DELETE FROM protections WHERE owner = ?");

			statement.setString(1, player);

			statement.executeUpdate();

		} catch (Exception e) {
			printException(e);
		}

		unregisterProtectionRightsByPlayer(player);
	}

	/**
	 * Unregister a limit
	 * 
	 * @param type
	 *            the type to unregister
	 * @param entity
	 *            the user/group to unregister
	 */
	public void unregisterProtectionLimit(int type, String entity) {
		try {
			PreparedStatement statement = prepare("DELETE FROM limits WHERE type = ? AND entity = ?");

			statement.setInt(1, type);
			statement.setString(2, entity.toLowerCase());

			statement.executeUpdate();

		} catch (SQLException e) {
			printException(e);
		}
	}

	/**
	 * Remove all of the limits
	 */
	public void unregisterProtectionLimits() {
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM limits");

			statement.close();

		} catch (SQLException e) {
			printException(e);
		}
	}

	/**
	 * Remove all protection rights
	 */
	public void unregisterProtectionRights() {
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM rights");
			statement.close();
		} catch (Exception e) {
			printException(e);
		}
	}

	/**
	 * Remove all of the rights from a chest
	 * 
	 * @param chestID
	 *            the chest ID
	 */
	public void unregisterProtectionRights(int chestID) {
		try {
			PreparedStatement statement = prepare("DELETE FROM rights WHERE chest = ?");

			statement.setInt(1, chestID);
			statement.executeUpdate();

		} catch (SQLException e) {
			printException(e);
		}
	}

	/**
	 * Remove all of the rights from a chest
	 * 
	 * @param chestID
	 *            the chest ID
	 */
	public void unregisterProtectionRights(int chestID, String entity) {
		try {
			PreparedStatement statement = prepare("DELETE FROM rights WHERE chest = ? AND entity = ?");

			statement.setInt(1, chestID);
			statement.setString(2, entity.toLowerCase());

			statement.executeUpdate();

		} catch (SQLException e) {
			printException(e);
		}
	}

	/**
	 * Remove all protection rights for a player
	 * 
	 * @param player
	 */
	public void unregisterProtectionRightsByPlayer(String player) {
		try {
			PreparedStatement statement = prepare("DELETE FROM rights WHERE entity = ?");

			statement.setString(1, player);

			statement.executeUpdate();

		} catch (Exception e) {
			printException(e);
		}
	}

	/**
	 * Remove all of the registered chests
	 */
	public void unregisterProtections() {
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM protections");
			statement.close();

		} catch (SQLException e) {
			printException(e);
		}
	}

	/**
	 * Update the inventories in lwc.db
	 * 
	 * @param id
	 */
	public void updateInventory(int id) {
		try {
			throw new SQLException("Not supported");
			// _insert_inventories_id_protectionid.setInt(1, 1);

		} catch (Exception e) {
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
				statement.executeUpdate("CREATE INDEX IF NOT EXISTS in1 ON protections (owner, x, y, z)");
				statement.executeUpdate("CREATE INDEX IF NOT EXISTS in2 ON limits (type, entity)");
				statement.executeUpdate("CREATE INDEX IF NOT EXISTS in3 ON rights (chest, entity)");

				statement.executeUpdate("CREATE INDEX IF NOT EXISTS in4 ON players (username)");
				statement.executeUpdate("CREATE INDEX IF NOT EXISTS in5 ON jobs (type, owner)");
				statement.executeUpdate("CREATE INDEX IF NOT EXISTS in6 ON inventory (protectionId, slots)");
			} else {
				statement.executeUpdate("CREATE INDEX in1 ON protections (x, y, z)");
				statement.executeUpdate("CREATE INDEX in2 ON limits (type)");
				statement.executeUpdate("CREATE INDEX in3 ON rights (chest)");

				// statement.executeUpdate("CREATE INDEX in4 ON players (username)");
				statement.executeUpdate("CREATE INDEX in5 ON jobs (type)");
				statement.executeUpdate("CREATE INDEX in6 ON inventory (protectionId, slots)");
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
			executeQueryNoException("UPDATE protections SET type='1'");
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
		// if false is returned, it was dropped
		if(!dropTable("players")) {
			dropTable("inventory");
			dropTable("jobs");
		}
		
		// check limits table
		try {
			Statement statement = connection.createStatement();
			statement.executeQuery("SELECT * FROM limits LIMIT 1");
			statement.close();
		} catch(Exception e) {
			return;
		}
		
		// Convert limits
		
		// dropTable("limits");
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
