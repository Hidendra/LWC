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
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.griefcraft.model.Chest;

public class PhysicalDatabase extends Database {

	/**
	 * Static instance
	 */
	private static PhysicalDatabase instance;

	/**
	 * @return an instance of Database
	 */
	public static PhysicalDatabase getInstance() {
		if (instance == null) {
			instance = new PhysicalDatabase();
		}

		return instance;
	}

	/**
	 * If the database was already loaded
	 */
	private boolean loaded = false;

	/**
	 * @return the number of protected chests
	 */
	public int chestCount() {
		int count = 0;

		try {
			final Statement statement = connection.createStatement();
			final ResultSet set = statement.executeQuery("SELECT `id` FROM `chests`");

			while (set.next()) {
				count++;
			}

			statement.close();
		} catch (final Exception e) {
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
	public boolean doesChestExist(int chestID) {
		boolean retur = false;

		try {
			final PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS count FROM `chests` WHERE `id` = ?");
			statement.setInt(1, chestID);

			final ResultSet set = statement.executeQuery();

			retur = set.getInt("count") > 0;

			statement.close();

		} catch (final Exception e) {
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
			statement.executeQuery("SELECT `type` FROM `chests`");
			statement.close();
		} catch (final Exception e) {
			/*
			 * This means we need to commit the update!
			 */

			log("Outdated!");
			log("UPGRADING FROM 1.00 TO 1.10");
			log("ALTERING TABLE `chests` AND FILLING WITH DEFAULT DATA");

			try {
				final Statement statement = connection.createStatement();
				statement.addBatch("ALTER TABLE `chests` ADD `type` INTEGER");
				statement.addBatch("UPDATE `chests` SET `type`='1'"); // OLD
				// CHESTS
				// WERE
				// PASSWORD
				// PROTECTED
				// !
				statement.executeBatch();
				statement.close();
			} catch (final Exception e_) {
				log("Oops! Something went wrong: ");
				e.printStackTrace();
				System.exit(0);
			}

			log("Update completed!");
		}
	}

	public void doUpdate103() {
		boolean needsUpdate = true;

		try {
			Statement statement = connection.createStatement();
			ResultSet set = statement.executeQuery("PRAGMA INDEX_LIST('chests')");

			while (set.next()) {
				needsUpdate = false;
			}

			statement.close();
		} catch (Exception e) {

		}

		if (!needsUpdate) {
			return;
		}

		log("Outdated!");
		log("UPGRADING FROM 1.10 TO 1.30");

		log("CREATING INDEXES!");

		try {
			Statement statement = connection.createStatement();
			statement.addBatch("BEGIN TRANSACTION");
			statement.addBatch("CREATE INDEX in1 ON `chests` (owner, x, y, z)");
			statement.addBatch("CREATE INDEX in2 ON `limits` (type, entity)");
			statement.addBatch("CREATE INDEX in3 ON `rights` (chest, entity)");
			statement.addBatch("END TRANSACTION");
			statement.executeBatch();
			statement.close();
		} catch (Exception e) {
			log("Oops! Something went wrong: ");
			e.printStackTrace();
		}

		log("Update complete!");
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
			final PreparedStatement statement = connection.prepareStatement("SELECT `id` FROM `chests` WHERE `owner` = ?");
			statement.setString(1, user);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				amount++;
			}

			statement.close();
		} catch (final Exception e) {
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
			final PreparedStatement statement = connection.prepareStatement("SELECT `amount` FROM `limits` WHERE `type` = ? AND `entity` = ?");
			statement.setInt(1, type);
			statement.setString(2, entity.toLowerCase());

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				limit = set.getInt("amount");
			}

			statement.close();
		} catch (final Exception e) {
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
			final PreparedStatement statement = connection.prepareStatement("SELECT `entity`, `rights` FROM `rights` WHERE `type` = ? AND `chest` = ?");
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

			statement.close();
		} catch (final Exception e) {
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
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return count;
	}

	/**
	 * Create the table needed if it does not already exist
	 */
	@Override
	public void load() {
		if (loaded) {
			return;
		}

		try {
			final Statement statement = connection.createStatement();

			log("Creating physical table 'chests' (If it's not there!)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS 'chests' (" //
					+ "id INTEGER PRIMARY KEY," //
					+ "type INTEGER," //
					+ "owner TEXT," //
					+ "password TEXT," //
					+ "x INTEGER," //
					+ "y INTEGER," //
					+ "z INTEGER," //
					+ "date TEXT" //
					+ ");");

			log("Creating physical table 'limits' (If it's not there!)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS 'limits' (" //
					+ "id INTEGER PRIMARY KEY," //
					+ "type INTEGER," //
					+ "amount INTEGER," //
					+ "entity TEXT" //
					+ ");");

			log("Creating physical table 'rights' (If it's not there!)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS 'rights' (" + "id INTEGER PRIMARY KEY," //
					+ "chest INTEGER," //
					+ "entity TEXT," //
					+ "rights INTEGER," //
					+ "type INTEGER" //
					+ ");");

			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		doUpdate100();
		doUpdate103();

		loaded = true;
	}

	/**
	 * Load a chest at a given tile
	 * 
	 * @param chestID
	 *            the chest's ID
	 * @return the Chest object
	 */
	public Chest loadChest(int chestID) {
		try {
			final PreparedStatement statement = connection.prepareStatement("SELECT * FROM `chests` WHERE `id` = ?");

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

				final Chest chest = new Chest();
				chest.setID(id);
				chest.setType(type);
				chest.setOwner(owner);
				chest.setPassword(password);
				chest.setX(x);
				chest.setY(y);
				chest.setZ(z);
				chest.setDate(date);

				statement.close();
				return chest;
			}

			statement.close();
		} catch (final Exception e) {
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
	public Chest loadChest(int x, int y, int z) {
		try {
			final PreparedStatement statement = connection.prepareStatement("SELECT `id`, `type`, `owner`, `password`, `date` FROM `chests` WHERE `x` = ? AND `y` = ? AND `z` = ?");

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

				final Chest chest = new Chest();
				chest.setID(id);
				chest.setType(type);
				chest.setOwner(owner);
				chest.setPassword(password);
				chest.setX(x);
				chest.setY(y);
				chest.setZ(z);
				chest.setDate(date);

				statement.close();
				return chest;
			}

			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return null;
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
	public List<Chest> loadChests(int _x, int _y, int _z, int radius) {
		List<Chest> chests = new ArrayList<Chest>();

		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM `chests` WHERE x >= ? AND x <= ? AND y >= ? AND y <= ? AND z >= ? AND z <= ?");
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

				final Chest chest = new Chest();
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

			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return chests;
	}

	/**
	 * Log a string to stdout
	 * 
	 * @param str
	 *            The string to log
	 */
	@Override
	public void log(String str) {
		System.out.println("[LWC->sqlite->physical] " + str);
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
	public void registerChest(int type, String player, String password, int x, int y, int z) {
		try {
			final PreparedStatement statement = connection.prepareStatement("INSERT INTO `chests` (type, owner, password, x, y, z, date) VALUES(?, ?, ?, ?, ?, ?, ?)");
			statement.setInt(1, type);
			statement.setString(2, player);
			statement.setString(3, password);
			statement.setInt(4, x);
			statement.setInt(5, y);
			statement.setInt(6, z);
			statement.setString(7, new Timestamp(new Date().getTime()).toString());

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
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
	public void registerLimit(int type, int amount, String entity) {
		try {
			unregisterLimit(type, entity.toLowerCase());

			final PreparedStatement statement = connection.prepareStatement("INSERT INTO `limits` (type, amount, entity) VALUES(?, ?, ?)");
			statement.setInt(1, type);
			statement.setInt(2, amount);
			statement.setString(3, entity.toLowerCase());

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
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
	public void registerRights(int chestID, String entity, int rights, int type) {
		try {
			final PreparedStatement statement = connection.prepareStatement("INSERT INTO `rights` (chest, entity, rights, type) VALUES (?, ?, ?, ?)");
			statement.setInt(1, chestID);
			statement.setString(2, entity.toLowerCase());
			statement.setInt(3, rights);
			statement.setInt(4, type);

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove all of the registered chests
	 */
	public void unregisterAllChests() {
		try {
			final Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM `chests`");
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove all of the limits
	 */
	public void unregisterAllLimits() {
		try {
			final Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM `limits`");
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove all of the rights from a chest
	 * 
	 * @param chestID
	 *            the chest ID
	 */
	public void unregisterAllRights(int chestID) {
		try {
			final PreparedStatement statement = connection.prepareStatement("DELETE FROM `rights` WHERE `chest` = ?");
			statement.setInt(1, chestID);

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Free a chest from protection
	 * 
	 * @param chestID
	 *            the chest ID
	 */
	public void unregisterChest(int chestID) {
		try {
			final PreparedStatement statement = connection.prepareStatement("DELETE FROM `chests` WHERE `id` = ?");
			statement.setInt(1, chestID);

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		unregisterAllRights(chestID);
	}

	/**
	 * Free a chest from protection
	 * 
	 * @param chestID
	 *            the chest ID
	 */
	public void unregisterChest(int x, int y, int z) {
		try {
			final PreparedStatement statement = connection.prepareStatement("DELETE FROM `chests` WHERE `x` = ? AND `y` = ? AND `z` = ?");
			statement.setInt(1, x);
			statement.setInt(2, y);
			statement.setInt(3, z);

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
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
	public void unregisterLimit(int type, String entity) {
		try {
			final PreparedStatement statement = connection.prepareStatement("DELETE FROM `limits` WHERE `type` = ? AND `entity` = ?");
			statement.setInt(1, type);
			statement.setString(2, entity.toLowerCase());

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove all of the rights from a chest
	 * 
	 * @param chestID
	 *            the chest ID
	 */
	public void unregisterRights(int chestID, String entity) {
		try {
			final PreparedStatement statement = connection.prepareStatement("DELETE FROM `rights` WHERE `chest` = ? AND `entity` = ?");
			statement.setInt(1, chestID);
			statement.setString(2, entity.toLowerCase());

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
