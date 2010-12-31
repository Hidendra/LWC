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

import com.griefcraft.model.Entity;
import com.griefcraft.util.Performance;

public class PhysDB extends Database {

	/**
	 * If the database was already loaded
	 */
	private boolean loaded = false;

	/**
	 * Queries
	 */
	private PreparedStatement _select_protectedEntity_ID;
	private PreparedStatement _select_chestExist_ID;
	private PreparedStatement _select_chestCount_user;
	private PreparedStatement _select_limit_type_entity;
	private PreparedStatement _select_privateAccess_type_ID_entities;
	private PreparedStatement _select_protectedEntity_x_y_z_radius;
	private PreparedStatement _select_protectedEntity_x_y_z;
	private PreparedStatement _insert_protectedEntity_type_player_password_x_y_z;
	private PreparedStatement _insert_protectedLimit_type_amount_entity;
	private PreparedStatement _insert_rights_ID_entity_rights_type;
	private PreparedStatement _delete_protectedEntity_ID;
	private PreparedStatement _delete_protectedEntity_x_y_z;
	private PreparedStatement _delete_limit_type_entity;
	private PreparedStatement _delete_rights_ID;
	private PreparedStatement _delete_rights_ID_entity;

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
			_select_chestExist_ID.setInt(1, chestID);

			final ResultSet set = _select_chestExist_ID.executeQuery();

			retur = set.getInt("count") > 0;

			set.close();
			Performance.addPhysDBQuery();

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
			statement.executeQuery("SELECT `type` FROM `protections`");
			statement.close();
			Performance.addPhysDBQuery();
		} catch (final Exception e) {
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
			} catch (final Exception e_) {
				log("Oops! Something went wrong: ");
				e.printStackTrace();
				System.exit(0);
			}

			log("Update completed!");
		}
	}

	public void doUpdate130() {
		boolean needsUpdate = true;

		try {
			Statement statement = connection.createStatement();
			ResultSet set = statement.executeQuery("PRAGMA INDEX_LIST('protections')");

			while (set.next()) {
				needsUpdate = false;
			}

			statement.close();
			Performance.addPhysDBQuery();
		} catch (Exception e) {

		}

		if (!needsUpdate) {
			return;
		}

		log("Outdated database!");
		log("UPGRADING FROM 1.10 TO 1.30");

		log("CREATING INDEXES!");

		try {
			Statement statement = connection.createStatement();
			statement.addBatch("BEGIN TRANSACTION");
			statement.addBatch("CREATE INDEX in1 ON `protections` (owner, x, y, z)");
			statement.addBatch("CREATE INDEX in2 ON `limits` (type, entity)");
			statement.addBatch("CREATE INDEX in3 ON `rights` (chest, entity)");
			statement.addBatch("END TRANSACTION");
			statement.executeBatch();
			statement.close();
			Performance.addPhysDBQuery();
		} catch (Exception e) {
			log("Oops! Something went wrong: ");
			e.printStackTrace();
		}

		log("Update complete!");
	}

	/**
	 * @return the number of protected chests
	 */
	public int entityCount() {
		int count = 0;

		try {
			final Statement statement = connection.createStatement();
			final ResultSet set = statement.executeQuery("SELECT `id` FROM `protections`");

			while (set.next()) {
				count++;
			}

			statement.close();
			Performance.addPhysDBQuery();
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
	public int getChestCount(String user) {
		int amount = 0;

		try {
			_select_chestCount_user.setString(1, user);

			final ResultSet set = _select_chestCount_user.executeQuery();

			while (set.next()) {
				amount++;
			}

			set.close();
			Performance.addPhysDBQuery();
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
			_select_limit_type_entity.setInt(1, type);
			_select_limit_type_entity.setString(2, entity.toLowerCase());

			final ResultSet set = _select_limit_type_entity.executeQuery();

			while (set.next()) {
				limit = set.getInt("amount");
			}

			set.close();
			Performance.addPhysDBQuery();
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
			_select_privateAccess_type_ID_entities.setInt(1, type);
			_select_privateAccess_type_ID_entities.setInt(2, chestID);

			final ResultSet set = _select_privateAccess_type_ID_entities.executeQuery();

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
			Performance.addPhysDBQuery();
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

		/**
		 * 1.40 renamed a table, so it needs to be renamed before LWC attempts to create it
		 */
		doUpdate140();

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

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS 'rights' (" + "id INTEGER PRIMARY KEY," //
					+ "chest INTEGER," //
					+ "entity TEXT," //
					+ "rights INTEGER," //
					+ "type INTEGER" //
					+ ");");

			connection.commit();
			connection.setAutoCommit(true);

			statement.close();
			Performance.addPhysDBQuery();

			loadPreparedStatements();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		doUpdate100();
		doUpdate130();

		loaded = true;
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
	public List<Entity> loadProtectedEntities(int _x, int _y, int _z, int radius) {
		List<Entity> chests = new ArrayList<Entity>();

		try {
			_select_protectedEntity_x_y_z_radius.setInt(1, _x - radius);
			_select_protectedEntity_x_y_z_radius.setInt(2, _x + radius);
			_select_protectedEntity_x_y_z_radius.setInt(3, _y - radius);
			_select_protectedEntity_x_y_z_radius.setInt(4, _y + radius);
			_select_protectedEntity_x_y_z_radius.setInt(5, _z - radius);
			_select_protectedEntity_x_y_z_radius.setInt(6, _z + radius);

			final ResultSet set = _select_protectedEntity_x_y_z_radius.executeQuery();

			while (set.next()) {
				final int id = set.getInt("id");
				final int type = set.getInt("type");
				final String owner = set.getString("owner");
				final String password = set.getString("password");
				int x = set.getInt("x");
				int y = set.getInt("y");
				int z = set.getInt("z");
				final String date = set.getString("date");

				final Entity chest = new Entity();
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
		} catch (Exception e) {
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
	public Entity loadProtectedEntity(int chestID) {
		try {
			_select_protectedEntity_ID.setInt(1, chestID);

			final ResultSet set = _select_protectedEntity_ID.executeQuery();

			while (set.next()) {
				final int id = set.getInt("id");
				final int type = set.getInt("type");
				final String owner = set.getString("owner");
				final String password = set.getString("password");
				final int x = set.getInt("x");
				final int y = set.getInt("y");
				final int z = set.getInt("z");
				final String date = set.getString("date");

				final Entity chest = new Entity();
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
	public Entity loadProtectedEntity(int x, int y, int z) {
		try {
			_select_protectedEntity_x_y_z.setInt(1, x);
			_select_protectedEntity_x_y_z.setInt(2, y);
			_select_protectedEntity_x_y_z.setInt(3, z);

			final ResultSet set = _select_protectedEntity_x_y_z.executeQuery();

			while (set.next()) {
				final int id = set.getInt("id");
				final int type = set.getInt("type");
				final String owner = set.getString("owner");
				final String password = set.getString("password");
				final String date = set.getString("date");

				final Entity chest = new Entity();
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
		} catch (final Exception e) {
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
			_insert_protectedEntity_type_player_password_x_y_z.setInt(1, type);
			_insert_protectedEntity_type_player_password_x_y_z.setString(2, player);
			_insert_protectedEntity_type_player_password_x_y_z.setString(3, password);
			_insert_protectedEntity_type_player_password_x_y_z.setInt(4, x);
			_insert_protectedEntity_type_player_password_x_y_z.setInt(5, y);
			_insert_protectedEntity_type_player_password_x_y_z.setInt(6, z);
			_insert_protectedEntity_type_player_password_x_y_z.setString(7, new Timestamp(new Date().getTime()).toString());

			_insert_protectedEntity_type_player_password_x_y_z.executeUpdate();
			Performance.addPhysDBQuery();
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
	public void registerProtectionLimit(int type, int amount, String entity) {
		try {
			unregisterProtectionLimit(type, entity.toLowerCase());

			_insert_protectedLimit_type_amount_entity.setInt(1, type);
			_insert_protectedLimit_type_amount_entity.setInt(2, amount);
			_insert_protectedLimit_type_amount_entity.setString(3, entity.toLowerCase());

			_insert_protectedLimit_type_amount_entity.executeUpdate();
			Performance.addPhysDBQuery();
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
	public void registerProtectionRights(int chestID, String entity, int rights, int type) {
		try {
			_insert_rights_ID_entity_rights_type.setInt(1, chestID);
			_insert_rights_ID_entity_rights_type.setString(2, entity.toLowerCase());
			_insert_rights_ID_entity_rights_type.setInt(3, rights);
			_insert_rights_ID_entity_rights_type.setInt(4, type);

			_insert_rights_ID_entity_rights_type.executeUpdate();
			Performance.addPhysDBQuery();
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
	public void unregisterProtectedEntity(int chestID) {
		try {
			_delete_protectedEntity_ID.setInt(1, chestID);

			_delete_protectedEntity_ID.executeUpdate();
			Performance.addPhysDBQuery();
		} catch (final Exception e) {
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
			_delete_protectedEntity_x_y_z.setInt(1, x);
			_delete_protectedEntity_x_y_z.setInt(2, y);
			_delete_protectedEntity_x_y_z.setInt(3, z);

			_delete_protectedEntity_x_y_z.executeUpdate();
			Performance.addPhysDBQuery();
		} catch (final Exception e) {
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
	public void unregisterProtectionLimit(int type, String entity) {
		try {
			_delete_limit_type_entity.setInt(1, type);
			_delete_limit_type_entity.setString(2, entity.toLowerCase());

			_delete_limit_type_entity.executeUpdate();
			Performance.addPhysDBQuery();
		} catch (final Exception e) {
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
		} catch (final Exception e) {
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
		} catch(Exception e) {
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
			_delete_rights_ID.setInt(1, chestID);
			_delete_rights_ID.executeUpdate();
			Performance.addPhysDBQuery();
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
	public void unregisterProtectionRights(int chestID, String entity) {
		try {
			_delete_rights_ID_entity.setInt(1, chestID);
			_delete_rights_ID_entity.setString(2, entity.toLowerCase());

			_delete_rights_ID_entity.executeUpdate();
			Performance.addPhysDBQuery();
		} catch (final Exception e) {
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
		} catch (Exception e) {
			log("Outdated database!");
			log("UPGRADING FROM 1.30 TO 1.40");

			log("Renaming table `chests` to `protections`");

			// Sexy.
			try {
				Statement statement = connection.createStatement();
				statement.executeUpdate("ALTER TABLE `chests` RENAME TO `protections`");
				statement.close();
				Performance.addPhysDBQuery();
			} catch (Exception e_) {
				e_.printStackTrace();
			}
		}
	}

	/**
	 * Load all of the prepared statements
	 * 
	 * @throws SQLException
	 */
	private void loadPreparedStatements() throws SQLException {
		_select_protectedEntity_ID = connection.prepareStatement("SELECT * FROM `protections` WHERE `id` = ?");
		_select_chestExist_ID = connection.prepareStatement("SELECT COUNT(*) AS count FROM `protections` WHERE `id` = ?");
		_select_chestCount_user = connection.prepareStatement("SELECT `id` FROM `protections` WHERE `owner` = ?");
		_select_limit_type_entity = connection.prepareStatement("SELECT `amount` FROM `limits` WHERE `type` = ? AND `entity` = ?");
		_select_privateAccess_type_ID_entities = connection.prepareStatement("SELECT `entity`, `rights` FROM `rights` WHERE `type` = ? AND `chest` = ?");
		_select_protectedEntity_x_y_z_radius = connection.prepareStatement("SELECT * FROM `protections` WHERE x >= ? AND x <= ? AND y >= ? AND y <= ? AND z >= ? AND z <= ?");
		_select_protectedEntity_x_y_z = connection.prepareStatement("SELECT `id`, `type`, `owner`, `password`, `date` FROM `protections` WHERE `x` = ? AND `y` = ? AND `z` = ?");

		_insert_protectedEntity_type_player_password_x_y_z = connection.prepareStatement("INSERT INTO `protections` (type, owner, password, x, y, z, date) VALUES(?, ?, ?, ?, ?, ?, ?)");
		_insert_protectedLimit_type_amount_entity = connection.prepareStatement("INSERT INTO `limits` (type, amount, entity) VALUES(?, ?, ?)");
		_insert_rights_ID_entity_rights_type = connection.prepareStatement("INSERT INTO `rights` (chest, entity, rights, type) VALUES (?, ?, ?, ?)");

		_delete_protectedEntity_ID = connection.prepareStatement("DELETE FROM `protections` WHERE `id` = ?");
		_delete_protectedEntity_x_y_z = connection.prepareStatement("DELETE FROM `protections` WHERE `x` = ? AND `y` = ? AND `z` = ?");
		_delete_limit_type_entity = connection.prepareStatement("DELETE FROM `limits` WHERE `type` = ? AND `entity` = ?");
		_delete_rights_ID = connection.prepareStatement("DELETE FROM `rights` WHERE `chest` = ?");
		_delete_rights_ID_entity = connection.prepareStatement("DELETE FROM `rights` WHERE `chest` = ? AND `entity` = ?");
	}

}
