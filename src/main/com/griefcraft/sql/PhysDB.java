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
import java.util.logging.Level;

import com.griefcraft.model.AccessRight;
import com.griefcraft.model.Job;
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

	/**
	 * Load every protection, use _sparingly_
	 * 
	 * @return
	 */
	public List<Protection> loadProtections() {
		List<Protection> protections = new ArrayList<Protection>();

		try {
			Statement statement = connection.createStatement();

			ResultSet set = statement.executeQuery("SELECT id,type,x,y,z FROM protections");

			while (set.next()) {
				/*
				 * Only grab relevent data .. We don't want the overhead of owner/password/date !
				 */
				int id = set.getInt("id");
				int type = set.getInt("type");
				int x = set.getInt("x");
				int y = set.getInt("y");
				int z = set.getInt("z");

				Protection protection = new Protection();
				protection.setId(id);
				protection.setType(type);
				protection.setX(x);
				protection.setY(y);
				protection.setZ(z);

				protections.add(protection);
			}

			set.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return protections;
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
			Performance.addPhysDBQuery();
		} catch (Exception e) {
			e.printStackTrace();
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
			Performance.addPhysDBQuery();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load the whole job queue
	 * 
	 * @return
	 */
	public List<Job> getJobQueue() {
		return getJobQueue(0);
	}

	/**
	 * Load a job queue of size n. if size is 0, will return all jobs
	 * 
	 * @param size
	 * @return
	 */
	public List<Job> getJobQueue(int size) {
		List<Job> jobs = new ArrayList<Job>();

		try {
			String where = size > 0 ? (" LIMIT " + size) : "";

			PreparedStatement statement = prepare("SELECT * FROM jobs" + where);

			ResultSet set = statement.executeQuery();

			while (set.next()) {
				Job job = new Job();

				int id = set.getInt("id");
				int type = set.getInt("type");
				String owner = set.getString("owner");
				String payload = set.getString("payload");
				long timestamp = set.getLong("timestamp");

				job.setId(id);
				job.setType(type);
				job.setOwner(owner);
				job.setPayload(payload);
				job.setTimestamp(timestamp);

				jobs.add(job);
			}

			set.close();
			Performance.addPhysDBQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return jobs;
	}

	/**
	 * Get the amount of chests a player has
	 * 
	 * @param user
	 *            the player to check
	 * @return the amount of chests they have locked
	 */
	public boolean doesChestExist(int chestID) {
		boolean retur = false;

		try {
			PreparedStatement statement = prepare("SELECT COUNT(*) AS count FROM `protections` WHERE `id` = ?");
			statement.setInt(1, chestID);

			final ResultSet set = statement.executeQuery();

			retur = set.getInt("count") > 0;

			set.close();
			Performance.addPhysDBQuery();

		} catch (final SQLException e) {
			e.printStackTrace();
		}

		return retur;
	}

	/**
	 * Update process from 1.00 -> 1.10
	 */
	public void doUpdate100() {
		try {
			final Statement statement = connection.createStatement();
			statement.executeQuery("SELECT `type` FROM `protections`");
			statement.close();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			logger.log("Outdated database!", Level.CONFIG);
			logger.log("UPGRADING FROM 1.00 TO 1.10", Level.CONFIG);
			logger.log("ALTERING TABLE `protections` AND FILLING WITH DEFAULT DATA", Level.CONFIG);

			try {
				final Statement statement = connection.createStatement();
				statement.addBatch("ALTER TABLE `protections` ADD `type` INTEGER");
				statement.addBatch("UPDATE `protections` SET `type`='1'");
				statement.executeBatch();
				statement.close();
				Performance.addPhysDBQuery();
			} catch (final SQLException ex) {
				log("Oops! Something went wrong: ");
				ex.printStackTrace();
				System.exit(0);
			}

			log("Update completed!");
		}
	}

	/**
	 * Update to 150, altered a table
	 */
	public void doUpdate150() {
		try {
			Statement statement = connection.createStatement();
			statement.executeQuery("SELECT blockId FROM protections");
			statement.close();
			Performance.addPhysDBQuery();
		} catch (SQLException e) {
			try {
				Statement statement = connection.createStatement();
				statement.executeUpdate("ALTER TABLE `protections` ADD `blockId` INTEGER");
				statement.close();
				Performance.addPhysDBQuery();
			} catch (SQLException ex) {
			}
		}
	}

	/**
	 * @return the number of protected chests
	 */
	public int getProtectionCount() {
		int count = 0;

		try {
			final Statement statement = connection.createStatement();
			final ResultSet set = statement.executeQuery("SELECT `id` FROM `protections`");

			while (set.next()) {
				count++;
			}

			statement.close();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
		}

		return count;
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
				return set.getObject(column);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Count the rights
	 * 
	 * @param protectionId
	 * @return
	 */
	public int countRights(int protectionId) {
		return (Integer) fetch("SELECT COUNT(*) AS COUNT FROM rights WHERE chest=?", "count", protectionId);
	}

	/**
	 * Get the rights for a protection id ranging from start-max
	 * 
	 * @param protectionId
	 * @param start
	 * @param max
	 * @return
	 */
	public List<AccessRight> getAccessRights(int protectionId, int start, int max) {
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
				accessRight.setEntity(set.getString("entity"));
				accessRight.setRights(set.getInt("rights"));
				accessRight.setType(set.getInt("type"));

				accessRights.add(accessRight);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return accessRights;
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
			PreparedStatement statement = prepare("SELECT `id` FROM `protections` WHERE `owner` = ?");
			statement.setString(1, user);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				amount++;
			}

			set.close();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
		}

		return amount;
	}

	/**
	 * Retrieve a group's chest limit
	 * 
	 * @param group
	 *            the group to check
	 * @return the amount of chests they are limited to. -1 = infinite
	 */
	public int getGroupLimit(String group) {
		return getLimit(Limit.GROUP, group);
	}

	/**
	 * @return the Global limit for anyone without explicit limits
	 */
	public int getGlobalLimit() {
		return getLimit(Limit.GLOBAL, "");
	}

	/**
	 * Retrieve a limit for a given type
	 * 
	 * @param type
	 *            0 = group, 1 = user. The latter overrides the former
	 * @param entity
	 *            the group or user to get
	 * @return the amount of chests the entitiy is limited to. -1 = infinite
	 */
	public int getLimit(int type, String entity) {
		int limit = -1;

		try {
			PreparedStatement statement = prepare("SELECT `amount` FROM `limits` WHERE `type` = ? AND `entity` = ?");
			statement.setInt(1, type);
			statement.setString(2, entity.toLowerCase());

			final ResultSet set = statement.executeQuery();

			if (set.next()) {
				limit = set.getInt("amount");
			}

			set.close();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
		}

		return limit;
	}

	/**
	 * Get the access level of a player to a chest -1 = no access 0 = normal access 1 = chest admin
	 * 
	 * @param player
	 *            the player to check
	 * @param chestID
	 *            the chest ID
	 * @return the player's access level
	 */
	public int getPrivateAccess(int type, int chestID, String... entities) {
		int access = -1;

		try {
			PreparedStatement statement = prepare("SELECT `entity`, `rights` FROM `rights` WHERE `type` = ? AND `chest` = ?");
			statement.setInt(1, type);
			statement.setInt(2, chestID);

			final ResultSet set = statement.executeQuery();

			_main: while (set.next()) {
				final String entity = set.getString("entity");

				for (final String str : entities) {
					if (str.equalsIgnoreCase(entity)) {
						access = set.getInt("rights");
						break _main;
					}
				}
			}

			set.close();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
		}

		return access;
	}

	/**
	 * Retrieve a player's chest limit
	 * 
	 * @param user
	 *            the user to check
	 * @return the amount of chests they are limited to. -1 = infinite
	 */
	public int getPlayerLimit(String user) {
		return getLimit(Limit.PLAYER, user);
	}

	/**
	 * @return the number of limits
	 */
	public int getLimitCount() {
		int count = 0;

		try {
			final Statement statement = connection.createStatement();
			final ResultSet set = statement.executeQuery("SELECT `id` FROM `limits`");

			while (set.next()) {
				count++;
			}

			statement.close();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
		}

		return count;
	}

	/**
	 * Pushed an LWC pre-1.5 tables with a mis-spelled column name. Damn me
	 */
	private void fixJobsTable() {
		try {
			Statement statement = connection.createStatement();
			statement.executeQuery("SELECT payload FROM jobs");
			statement.executeQuery("SELECT timestamp FROM jobs");
			statement.close();
		} catch (SQLException e) {
			logger.log("Fixing jobs table", Level.CONFIG);

			try {
				Statement statement = connection.createStatement();
				statement.executeUpdate("DROP TABLE jobs");
				statement.close();
			} catch (SQLException ex) {
			}
		}
	}

	/**
	 * Rename users table
	 */
	private void fixPlayerTable() {
		try {
			Statement statement = connection.createStatement();
			statement.executeQuery("SELECT * from players");
			statement.close();
		} catch (SQLException e) {
			logger.log("Fixing players table", Level.CONFIG);

			try {
				Statement statement = connection.createStatement();
				statement.executeUpdate("DROP TABLE users");
				statement.close();
			} catch (SQLException ex) {
			}
		}

		try {
			Statement statement = connection.createStatement();
			statement.executeQuery("SELECT mcusername from players");
			statement.close();
		} catch (SQLException e) {
			logger.log("Fixing players table", Level.CONFIG);

			try {
				Statement statement = connection.createStatement();
				statement.executeUpdate("DROP TABLE players");
				statement.close();
			} catch (SQLException ex) {
			}
		}
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

		fixJobsTable();
		fixPlayerTable();

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

				column = new Column("owner");
				column.setType("TEXT");
				protections.addColumn(column);

				column = new Column("password");
				column.setType("TEXT");
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
				column.setType("TEXT");
				protections.addColumn(column);
			}

			Table limits = new Table(this, "limits");

			{
				column = new Column("id");
				column.setType("INTEGER");
				column.setPrimary(true);
				limits.addColumn(column);

				column = new Column("type");
				column.setType("INTEGER");
				limits.addColumn(column);

				column = new Column("amount");
				column.setType("INTEGER");
				limits.addColumn(column);

				column = new Column("entity");
				column.setType("TEXT");
				limits.addColumn(column);
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

			Table players = new Table(this, "players");

			{
				column = new Column("id");
				column.setType("INTEGER");
				column.setPrimary(true);
				players.addColumn(column);

				column = new Column("username");
				column.setType("TEXT");
				players.addColumn(column);

				column = new Column("password");
				column.setType("TEXT");
				players.addColumn(column);

				column = new Column("mcusername");
				column.setType("TEXT");
				players.addColumn(column);

				column = new Column("rights");
				column.setType("INTEGER");
				players.addColumn(column);

				column = new Column("timestamp");
				column.setType("TEXT");
				players.addColumn(column);

				column = new Column("salt");
				column.setType("TEXT");
				players.addColumn(column);
			}

			Table inventory = new Table(this, "inventory");

			{
				column = new Column("protectionId");
				column.setType("INTEGER");
				column.setPrimary(true);
				inventory.addColumn(column);

				column = new Column("blockId");
				column.setType("INTEGER");
				inventory.addColumn(column);

				column = new Column("slots");
				column.setType("INTEGER");
				inventory.addColumn(column);

				column = new Column("stacks");
				column.setType("TEXT");
				inventory.addColumn(column);

				column = new Column("items");
				column.setType("TEXT");
				inventory.addColumn(column);

				column = new Column("durability");
				column.setType("TEXT");
				inventory.addColumn(column);

				column = new Column("last_transaction");
				column.setType("TEXT");
				inventory.addColumn(column);

				column = new Column("last_update");
				column.setType("TEXT");
				inventory.addColumn(column);
			}

			Table jobs = new Table(this, "jobs");

			{
				column = new Column("id");
				column.setType("INTEGER");
				column.setPrimary(true);
				jobs.addColumn(column);

				column = new Column("type");
				column.setType("INTEGER");
				jobs.addColumn(column);

				column = new Column("owner");
				column.setType("TEXT");
				jobs.addColumn(column);

				column = new Column("payload");
				column.setType("TEXT");
				jobs.addColumn(column);

				column = new Column("timestamp");
				column.setType("TEXT");
				jobs.addColumn(column);
			}

			protections.execute();
			limits.execute();
			rights.execute();
			players.execute();
			inventory.execute();
			jobs.execute();

			connection.commit();
			connection.setAutoCommit(true);

			loadIndexes();
		} catch (final SQLException e) {
			e.printStackTrace();
		}

		doUpdate100();

		loaded = true;
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
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Instead of "updating indexes", let's just use IF NOT EXISTS each time
	 */
	private void loadIndexes() {
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
			connection.setAutoCommit(true);

			statement.close();
		} catch (SQLException e) {
			// e.printStackTrace();
		}
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
	public List<Protection> loadProtectedEntities(int _x, int _y, int _z, int radius) {
		List<Protection> chests = new ArrayList<Protection>();

		try {
			PreparedStatement statement = prepare("SELECT * FROM `protections` WHERE x >= ? AND x <= ? AND y >= ? AND y <= ? AND z >= ? AND z <= ?");

			statement.setInt(1, _x - radius);
			statement.setInt(2, _x + radius);
			statement.setInt(3, _y - radius);
			statement.setInt(4, _y + radius);
			statement.setInt(5, _z - radius);
			statement.setInt(6, _z + radius);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				final int id = set.getInt("id");
				int blockId = set.getInt("blockId");
				final int type = set.getInt("type");
				final String owner = set.getString("owner");
				final String password = set.getString("password");
				int x = set.getInt("x");
				int y = set.getInt("y");
				int z = set.getInt("z");
				final String date = set.getString("date");

				final Protection protection = new Protection();
				protection.setId(id);
				protection.setBlockId(blockId);
				protection.setType(type);
				protection.setOwner(owner);
				protection.setData(password);
				protection.setX(x);
				protection.setY(y);
				protection.setZ(z);
				protection.setDate(date);

				chests.add(protection);
			}

			set.close();
			Performance.addPhysDBQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return chests;
	}

	/**
	 * Load a chest at a given tile
	 * 
	 * @param chestID
	 *            the chest's ID
	 * @return the Chest object
	 */
	public Protection loadProtectedEntity(int chestID) {
		try {
			PreparedStatement statement = prepare("SELECT * FROM `protections` WHERE `id` = ?");
			statement.setInt(1, chestID);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				final int id = set.getInt("id");
				final int blockId = set.getInt("blockId");
				final int type = set.getInt("type");
				final String owner = set.getString("owner");
				final String password = set.getString("password");
				final int x = set.getInt("x");
				final int y = set.getInt("y");
				final int z = set.getInt("z");
				final String date = set.getString("date");

				final Protection protection = new Protection();
				protection.setId(id);
				protection.setBlockId(blockId);
				protection.setType(type);
				protection.setOwner(owner);
				protection.setData(password);
				protection.setX(x);
				protection.setY(y);
				protection.setZ(z);
				protection.setDate(date);

				set.close();
				Performance.addPhysDBQuery();

				return protection;
			}

			set.close();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Update a protection's block id
	 * 
	 * @param protectionId
	 * @param blockId
	 */
	public void updateProtectionBlockId(int protectionId, int blockId) {
		try {
			PreparedStatement statement = prepare("UPDATE protections SET blockId = ? WHERE id = ?");

			statement.setInt(1, blockId);
			statement.setInt(2, protectionId);

			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load a chest at a given tile
	 * 
	 * @param x
	 *            the x tile
	 * @param y
	 *            the y tile
	 * @param z
	 *            the z tile
	 * @return the Chest object
	 */
	public Protection loadProtectedEntity(int x, int y, int z) {
		try {
			PreparedStatement statement = prepare("SELECT * FROM `protections` WHERE `x` = ? AND `y` = ? AND `z` = ?");
			statement.setInt(1, x);
			statement.setInt(2, y);
			statement.setInt(3, z);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				final int id = set.getInt("id");
				final int blockId = set.getInt("blockId");
				final int type = set.getInt("type");
				final String owner = set.getString("owner");
				final String password = set.getString("password");
				final String date = set.getString("date");

				final Protection protection = new Protection();
				protection.setId(id);
				protection.setBlockId(blockId);
				protection.setType(type);
				protection.setOwner(owner);
				protection.setData(password);
				protection.setX(x);
				protection.setY(y);
				protection.setZ(z);
				protection.setDate(date);

				set.close();
				Performance.addPhysDBQuery();
				return protection;
			}

			set.close();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
		}

		return null;
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
	public void registerProtectedEntity(int blockId, int type, String player, String password, int x, int y, int z) {
		try {
			PreparedStatement statement = prepare("INSERT INTO `protections` (blockId, type, owner, password, x, y, z, date) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");

			statement.setInt(1, blockId);
			statement.setInt(2, type);
			statement.setString(3, player);
			statement.setString(4, password);
			statement.setInt(5, x);
			statement.setInt(6, y);
			statement.setInt(7, z);
			statement.setString(8, new Timestamp(new Date().getTime()).toString());

			statement.executeUpdate();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
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

			PreparedStatement statement = prepare("INSERT INTO `limits` (type, amount, entity) VALUES(?, ?, ?)");

			statement.setInt(1, type);
			statement.setInt(2, amount);
			statement.setString(3, entity.toLowerCase());

			statement.executeUpdate();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Register rights to a chest
	 * 
	 * @param chestID
	 *            the chest ID to register
	 * @param entity
	 *            the entity to register
	 * @param rights
	 *            the rights to register
	 * @param type
	 *            the type to register
	 */
	public void registerProtectionRights(int chestID, String entity, int rights, int type) {
		try {
			PreparedStatement statement = prepare("INSERT INTO `rights` (chest, entity, rights, type) VALUES (?, ?, ?, ?)");

			statement.setInt(1, chestID);
			statement.setString(2, entity.toLowerCase());
			statement.setInt(3, rights);
			statement.setInt(4, type);

			statement.executeUpdate();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Free a chest from protection
	 * 
	 * @param chestID
	 *            the chest ID
	 */
	public void unregisterProtection(int chestID) {
		try {
			PreparedStatement statement = prepare("DELETE FROM `protections` WHERE `id` = ?");

			statement.setInt(1, chestID);

			statement.executeUpdate();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
		}

		unregisterProtectionRights(chestID);
	}

	/**
	 * Remove all protections made by a player
	 * 
	 * @param player
	 */
	public void removeProtectionByPlayer(String player) {
		try {
			PreparedStatement statement = prepare("DELETE FROM `protections` WHERE `owner` = ?");

			statement.setString(1, player);

			statement.executeUpdate();
			Performance.addPhysDBQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		removeProtectionRightsByPlayer(player);
	}

	/**
	 * Remove all protection rights for a player
	 * 
	 * @param player
	 */
	public void removeProtectionRightsByPlayer(String player) {
		try {
			PreparedStatement statement = prepare("DELETE FROM `rights` WHERE `entity` = ?");

			statement.setString(1, player);

			statement.executeUpdate();
			Performance.addPhysDBQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Free a chest from protection
	 * 
	 * @param chestID
	 *            the chest ID
	 */
	public void unregisterProtectedEntity(int x, int y, int z) {
		try {
			PreparedStatement statement = prepare("DELETE FROM `protections` WHERE `x` = ? AND `y` = ? AND `z` = ?");

			statement.setInt(1, x);
			statement.setInt(2, y);
			statement.setInt(3, z);

			statement.executeUpdate();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove all of the registered chests
	 */
	public void unregisterProtectionEntities() {
		try {
			final Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM `protections`");
			statement.close();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
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
			PreparedStatement statement = prepare("DELETE FROM `limits` WHERE `type` = ? AND `entity` = ?");

			statement.setInt(1, type);
			statement.setString(2, entity.toLowerCase());

			statement.executeUpdate();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove all of the limits
	 */
	public void unregisterProtectionLimits() {
		try {
			final Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM `limits`");

			statement.close();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove all protection rights
	 */
	public void unregisterProtectionRights() {
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM `rights`");
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
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
			PreparedStatement statement = prepare("DELETE FROM `rights` WHERE `chest` = ?");

			statement.setInt(1, chestID);
			statement.executeUpdate();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
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
			PreparedStatement statement = prepare("DELETE FROM `rights` WHERE `chest` = ? AND `entity` = ?");

			statement.setInt(1, chestID);
			statement.setString(2, entity.toLowerCase());

			statement.executeUpdate();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Upgrade process for 1.40, rename table `protections` to `protections`
	 */
	private void doUpdate140() {
		try {
			Statement statement = connection.createStatement();
			statement.executeQuery("SELECT `id` FROM `protections`");
			statement.close();
			Performance.addPhysDBQuery();
		} catch (SQLException e) {
			logger.log("Outdated database!", Level.CONFIG);
			logger.log("UPGRADING FROM 1.30 TO 1.40", Level.CONFIG);

			logger.log("Renaming table `chests` to `protections`", Level.CONFIG);

			try {
				Statement statement = connection.createStatement();
				statement.executeUpdate("ALTER TABLE `chests` RENAME TO `protections`");
				statement.close();
				Performance.addPhysDBQuery();
			} catch (SQLException e_) {
			}
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

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
