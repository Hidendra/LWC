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

import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.model.Job;
import com.griefcraft.model.Protection;
import com.griefcraft.util.Performance;

public class PhysDB extends Database {

	/**
	 * If the database was already loaded
	 */
	private boolean loaded = false;
	
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
			
			while(set.next()) {
				/*
				 * Only grab relevent data ..
				 * We don't want the overhead of owner/password/date !
				 */
				int id = set.getInt("id");
				int type = set.getInt("type");
				int x = set.getInt("x");
				int y = set.getInt("y");
				int z = set.getInt("z");

				Protection protection = new Protection();
				protection.setID(id);
				protection.setType(type);
				protection.setX(x);
				protection.setY(y);
				protection.setZ(z);
				
				protections.add(protection);
			}
		
			set.close();
			statement.close();
		} catch(SQLException e) {
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
		} catch(Exception e) {
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
		} catch(Exception e) {
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
			
			while(set.next()) {
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
		} catch(SQLException e) {
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
			/*
			 * This means we need to commit the update!
			 */

			log("Outdated database!");
			log("UPGRADING FROM 1.00 TO 1.10");
			log("ALTERING TABLE `protections` AND FILLING WITH DEFAULT DATA");

			try {
				final Statement statement = connection.createStatement();
				statement.addBatch("ALTER TABLE `protections` ADD `type` INTEGER");
				statement.addBatch("UPDATE `protections` SET `type`='1'");
				statement.executeBatch();
				statement.close();
				Performance.addPhysDBQuery();
			} catch (final SQLException e_) {
				log("Oops! Something went wrong: ");
				e.printStackTrace();
				System.exit(0);
			}

			log("Update completed!");
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
	 * Get the amount of chests a player has
	 * 
	 * @param user
	 *            the player to check
	 * @return the amount of chests they have locked
	 */
	public int getChestCount(String user) {
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
		return getLimit(0, group);
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

			while (set.next()) {
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
	 * Retrieve a user's chest limit
	 * 
	 * @param user
	 *            the user to check
	 * @return the amount of chests they are limited to. -1 = infinite
	 */
	public int getUserLimit(String user) {
		return getLimit(1, user);
	}

	/**
	 * @return the number of limits
	 */
	public int limitCount() {
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
		} catch(SQLException e) {
			log("Fixing jobs table");
			
			try {
				Statement statement = connection.createStatement();
				statement.executeUpdate("DROP TABLE jobs");
				statement.close();
			} catch(SQLException ex) {
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
		} catch(SQLException e) {
			log("Fixing players table");
			
			try {
				Statement statement = connection.createStatement();
				statement.executeUpdate("DROP TABLE users");
				statement.close();
			} catch(SQLException ex) {
			}
		}
		
		try {
			Statement statement = connection.createStatement();
			statement.executeQuery("SELECT mcusername from players");
			statement.close();
		} catch(SQLException e) {
			log("Fixing players table");
			
			try {
				Statement statement = connection.createStatement();
				statement.executeUpdate("DROP TABLE players");
				statement.close();
			} catch(SQLException ex) {
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
		 * 1.40 renamed a table, so it needs to be renamed before LWC attempts to create it
		 */
		doUpdate140();

		fixJobsTable();
		fixPlayerTable();

		try {
			final Statement statement = connection.createStatement();
			connection.setAutoCommit(false);

			log("Creating physical tables if needed");

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS 'protections' (" //
					+ "id INTEGER PRIMARY KEY," //
					+ "type INTEGER," //
					+ "owner TEXT," //
					+ "password TEXT," //
					+ "x INTEGER," //
					+ "y INTEGER," //
					+ "z INTEGER," //
					+ "date TEXT" //
					+ ");");

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS 'limits' (" //
					+ "id INTEGER PRIMARY KEY," //
					+ "type INTEGER," //
					+ "amount INTEGER," //
					+ "entity TEXT" //
					+ ");");

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS 'rights' (" //
					+ "id INTEGER PRIMARY KEY," //
					+ "chest INTEGER," //
					+ "entity TEXT," //
					+ "rights INTEGER," //
					+ "type INTEGER" //
					+ ");");
			
			/* Tables used in 1.50 */
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS 'players' (" //
					+ "id INTEGER PRIMARY KEY," //
					+ "username TEXT," //
					+ "password TEXT," //
					+ "mcusername TEXT," //
					+ "rights INTEGER," //
					+ "timestamp TEXT," //
					+ "salt TEXT" //
					+ ");");
			
			/**
			 * TODO:
			 * 
			 * protections table update
			 * inventories
			 * inventory_contents
			 */
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS jobs (" //
					+ "id INTEGER PRIMARY KEY," //
					+ "type INTEGER,"
					+ "owner TEXT,"
					+ "payload TEXT,"
					+ "timestamp TEXT"
					+ ");");

			connection.commit();
			connection.setAutoCommit(true);

			statement.close();
			Performance.addPhysDBQuery();

			loadIndexes();
		} catch (final SQLException e) {
			e.printStackTrace();
		}

		doUpdate100();

		loaded = true;
	}
	
	/**
	 * Instead of "updating indexes", let's just use IF NOT EXISTS each time
	 */
	private void loadIndexes() {
		try {
			connection.setAutoCommit(false);
			Statement statement = connection.createStatement();
			
			statement.executeUpdate("CREATE INDEX IF NOT EXISTS in1 ON `protections` (owner, x, y, z)");
			statement.executeUpdate("CREATE INDEX IF NOT EXISTS in2 ON `limits` (type, entity)");
			statement.executeUpdate("CREATE INDEX IF NOT EXISTS in3 ON `rights` (chest, entity)");

			statement.executeUpdate("CREATE INDEX IF NOT EXISTS in4 ON `players` (username)");
			statement.executeUpdate("CREATE INDEX IF NOT EXISTS in5 ON `jobs` (type, owner)");

			connection.commit();
			connection.setAutoCommit(true);
			
			statement.close();
		} catch(SQLException e) {
			e.printStackTrace();
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
				final int type = set.getInt("type");
				final String owner = set.getString("owner");
				final String password = set.getString("password");
				int x = set.getInt("x");
				int y = set.getInt("y");
				int z = set.getInt("z");
				final String date = set.getString("date");

				final Protection chest = new Protection();
				chest.setID(id);
				chest.setType(type);
				chest.setOwner(owner);
				chest.setPassword(password);
				chest.setX(x);
				chest.setY(y);
				chest.setZ(z);
				chest.setDate(date);

				chests.add(chest);
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
				final int type = set.getInt("type");
				final String owner = set.getString("owner");
				final String password = set.getString("password");
				final int x = set.getInt("x");
				final int y = set.getInt("y");
				final int z = set.getInt("z");
				final String date = set.getString("date");

				final Protection chest = new Protection();
				chest.setID(id);
				chest.setType(type);
				chest.setOwner(owner);
				chest.setPassword(password);
				chest.setX(x);
				chest.setY(y);
				chest.setZ(z);
				chest.setDate(date);

				set.close();
				Performance.addPhysDBQuery();

				return chest;
			}

			set.close();
			Performance.addPhysDBQuery();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return null;
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
			PreparedStatement statement = prepare("SELECT `id`, `type`, `owner`, `password`, `date` FROM `protections` WHERE `x` = ? AND `y` = ? AND `z` = ?");
			statement.setInt(1, x);
			statement.setInt(2, y);
			statement.setInt(3, z);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				final int id = set.getInt("id");
				final int type = set.getInt("type");
				final String owner = set.getString("owner");
				final String password = set.getString("password");
				final String date = set.getString("date");

				final Protection chest = new Protection();
				chest.setID(id);
				chest.setType(type);
				chest.setOwner(owner);
				chest.setPassword(password);
				chest.setX(x);
				chest.setY(y);
				chest.setZ(z);
				chest.setDate(date);

				set.close();
				Performance.addPhysDBQuery();
				return chest;
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
	public void registerProtectedEntity(int type, String player, String password, int x, int y, int z) {
		try {
			PreparedStatement statement = prepare("INSERT INTO `protections` (type, owner, password, x, y, z, date) VALUES(?, ?, ?, ?, ?, ?, ?)");
			
			statement.setInt(1, type);
			statement.setString(2, player);
			statement.setString(3, password);
			statement.setInt(4, x);
			statement.setInt(5, y);
			statement.setInt(6, z);
			statement.setString(7, new Timestamp(new Date().getTime()).toString());
			
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
		} catch(SQLException e) {
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
			log("Outdated database!");
			log("UPGRADING FROM 1.30 TO 1.40");

			log("Renaming table `chests` to `protections`");

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
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

}
