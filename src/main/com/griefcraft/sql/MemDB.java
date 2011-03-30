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
import java.util.ArrayList;
import java.util.List;

import com.griefcraft.model.Action;
import com.griefcraft.model.Protection;
import com.griefcraft.util.Performance;

public class MemDB extends Database {

	public MemDB() {
		super();
	}

	public MemDB(Type currentType) {
		super(currentType);
	}

	public Action getAction(String action, String player) {
		try {
			PreparedStatement statement = prepare("SELECT * FROM `actions` WHERE `player` = ? AND `action` = ?");
			statement.setString(1, player);
			statement.setString(2, action);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				final int id = set.getInt("id");
				final String actionString = set.getString("action");
				final String playerString = set.getString("player");
				final int chestID = set.getInt("chest");
				final String data = set.getString("data");

				final Action act = new Action();
				act.setID(id);
				act.setAction(actionString);
				act.setPlayer(playerString);
				act.setChestID(chestID);
				act.setData(data);

				return act;
			}

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}

		return null;
	}

	/**
	 * Get the chest ID associated with a player's unlock request
	 * 
	 * @param player
	 *            the player to lookup
	 * @return the chest ID
	 */
	public int getActionID(String action, String player) {
		try {
			int chestID = -1;

			PreparedStatement statement = prepare("SELECT `chest` FROM `actions` WHERE `action` = ? AND `player` = ?");
			statement.setString(1, action);
			statement.setString(2, player);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				chestID = set.getInt("chest");
			}

			Performance.addMemDBQuery();

			return chestID;
		} catch (final Exception e) {
			printException(e);
		}

		return -1;
	}

	/**
	 * Get all the active actions for a player
	 * 
	 * @param player
	 *            the player to get actions for
	 * @return the List<String> of actions
	 */
	public List<String> getActions(String player) {
		final List<String> actions = new ArrayList<String>();

		try {
			PreparedStatement statement = prepare("SELECT `action` FROM `actions` WHERE `player` = ?");
			statement.setString(1, player);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				final String action = set.getString("action");

				actions.add(action);
			}

		} catch (final Exception e) {
			printException(e);
		}

		return actions;
	}

	/**
	 * @return the path where the database file should be saved
	 */
	@Override
	public String getDatabasePath() {
		// if we're using mysql, just open another connection
		if (currentType == Type.MySQL) {
			return super.getDatabasePath();
		}

		return ":memory:";
	}

	/**
	 * Get the password submitted for a pending chest lock
	 * 
	 * @param player
	 *            the player to lookup
	 * @return the password for the pending lock
	 */
	public String getLockPassword(String player) {
		try {
			String password = "";

			PreparedStatement statement = prepare("SELECT `password` FROM `locks` WHERE `player` = ?");
			statement.setString(1, player);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				password = set.getString("password");
			}

			Performance.addMemDBQuery();

			return password;
		} catch (final Exception e) {
			printException(e);
		}

		return null;
	}

	/**
	 * Get the mode data for a player's mode
	 * 
	 * @param player
	 * @param mode
	 * @return
	 */
	public String getModeData(String player, String mode) {
		String ret = null;
		try {
			PreparedStatement statement = prepare("SELECT `data` from `modes` WHERE `player` = ? AND `mode` = ?");
			statement.setString(1, player);
			statement.setString(2, mode);

			final ResultSet set = statement.executeQuery();
			if (set.next()) {
				ret = set.getString("data");
			}

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}
		return ret;
	}

	/**
	 * Get the modes a player has activated
	 * 
	 * @param player
	 *            the player to get
	 * @return the List of modes the player is using
	 */
	public List<String> getModes(String player) {
		final List<String> modes = new ArrayList<String>();

		try {
			PreparedStatement statement = prepare("SELECT * from `modes` WHERE `player` = ?");
			statement.setString(1, player);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				final String mode = set.getString("mode");

				modes.add(mode);
			}

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}

		return modes;
	}

	/**
	 * Get all of the users "logged in" to a chest
	 * 
	 * @param chestID
	 *            the chest ID to look at
	 * @return
	 */
	public List<String> getSessionUsers(int chestID) {
		final List<String> sessionUsers = new ArrayList<String>();

		try {
			PreparedStatement statement = prepare("SELECT `player` FROM `sessions` WHERE `chest` = ?");
			statement.setInt(1, chestID);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				final String player = set.getString("player");

				sessionUsers.add(player);
			}

		} catch (final Exception e) {
			printException(e);
		}

		return sessionUsers;
	}

	/**
	 * Get the chest ID associated with a player's unlock request
	 * 
	 * @param player
	 *            the player to lookup
	 * @return the chest ID
	 */
	public int getUnlockID(String player) {
		return getActionID("unlock", player);
	}

	/**
	 * Check if a player has an active chest session
	 * 
	 * @param player
	 *            the player to check
	 * @param chestID
	 *            the chest ID to check
	 * @return true if the player has access
	 */
	public boolean hasAccess(String player, int chestID) {
		try {
			PreparedStatement statement = prepare("SELECT `player` FROM `sessions` WHERE `chest` = ?");
			statement.setInt(1, chestID);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				final String player2 = set.getString("player");

				if (player.equals(player2)) {

					Performance.addMemDBQuery();
					return true;
				}
			}

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}

		return false;
	}

	/**
	 * Check if a player has an active chest session
	 * 
	 * @param player
	 *            the player to check
	 * @param chest
	 *            the chest to check
	 * @return true if the player has access
	 */
	public boolean hasAccess(String player, Protection chest) {
		if (chest == null) {
			return true;
		}

		return hasAccess(player, chest.getId());
	}

	/**
	 * Return if a player has the mode
	 * 
	 * @param player
	 *            the player to check
	 * @param mode
	 *            the mode to check
	 */
	public boolean hasMode(String player, String mode) {
		List<String> modes = getModes(player);

		return modes.size() > 0 && modes.contains(mode);
	}

	/**
	 * Check if a player has a pending action
	 * 
	 * @param player
	 *            the player to check
	 * @param action
	 *            the action to check
	 * @return true if they have a record
	 */
	public boolean hasPendingAction(String action, String player) {
		return getAction(action, player) != null;
	}

	/**
	 * Check if a player has a pending chest request
	 * 
	 * @param player
	 *            The player to check
	 * @return true if the player has a pending chest request
	 */
	public boolean hasPendingChest(String player) {
		try {
			PreparedStatement statement = prepare("SELECT `id` FROM `locks` WHERE `player` = ?");
			statement.setString(1, player);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {

				Performance.addMemDBQuery();
				return true;
			}

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}

		return false;
	}

	/**
	 * Check if a player has a pending unlock request
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player has a pending unlock request
	 */
	public boolean hasPendingUnlock(String player) {
		return getUnlockID(player) != -1;
	}

	/**
	 * create the in-memory table which hold sessions, users that have activated a chest. Not needed past a restart, so no need for extra disk i/o
	 */
	@Override
	public void load() {
		try {
			// reusable column
			Column column;

			Table sessions = new Table(this, "sessions");
			sessions.setMemory(true);

			{
				column = new Column("id");
				column.setType("INTEGER");
				column.setPrimary(true);
				sessions.addColumn(column);

				column = new Column("player");
				column.setType("VARCHAR(255)");
				sessions.addColumn(column);

				column = new Column("chest");
				column.setType("INTEGER");
				sessions.addColumn(column);
			}

			Table locks = new Table(this, "locks");
			locks.setMemory(true);

			{
				column = new Column("id");
				column.setType("INTEGER");
				column.setPrimary(true);
				locks.addColumn(column);

				column = new Column("player");
				column.setType("VARCHAR(255)");
				locks.addColumn(column);

				column = new Column("password");
				column.setType("VARCHAR(100)");
				locks.addColumn(column);
			}

			Table actions = new Table(this, "actions");
			actions.setMemory(true);

			{
				column = new Column("id");
				column.setType("INTEGER");
				column.setPrimary(true);
				actions.addColumn(column);

				column = new Column("action");
				column.setType("VARCHAR(255)");
				actions.addColumn(column);

				column = new Column("player");
				column.setType("VARCHAR(255)");
				actions.addColumn(column);

				column = new Column("chest");
				column.setType("INTEGER");
				actions.addColumn(column);

				column = new Column("data");
				column.setType("VARCHAR(255)");
				actions.addColumn(column);
			}

			Table modes = new Table(this, "modes");
			modes.setMemory(true);

			{
				column = new Column("id");
				column.setType("INTEGER");
				column.setPrimary(true);
				modes.addColumn(column);

				column = new Column("player");
				column.setType("VARCHAR(255)");
				modes.addColumn(column);

				column = new Column("mode");
				column.setType("VARCHAR(255)");
				modes.addColumn(column);

				column = new Column("data");
				column.setType("VARCHAR(255)");
				modes.addColumn(column);
			}

			// now create all of the tables
			sessions.execute();
			locks.execute();
			actions.execute();
			modes.execute();

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}
	}

	/**
	 * @return the number of pending chest locks
	 */
	public int pendingCount() {
		int count = 0;

		try {
			Statement statement = connection.createStatement();
			final ResultSet set = statement.executeQuery("SELECT `id` FROM `locks`");

			while (set.next()) {
				count++;
			}

			statement.close();
			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}

		return count;
	}

	/**
	 * Register a pending chest unlock, for when the player does /unlock <pass>
	 * 
	 * @param player
	 *            the player to register
	 * @param chestID
	 *            the chestID to unlock
	 */
	public void registerAction(String action, String player) {
		try {
			/*
			 * We only want 1 action per player, no matter what!
			 */
			unregisterAction(action, player);

			PreparedStatement statement = prepare("INSERT INTO `actions` (action, player, chest) VALUES (?, ?, ?)");
			statement.setString(1, action);
			statement.setString(2, player);
			statement.setInt(3, -1);

			statement.executeUpdate();

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}
	}

	/**
	 * Register a pending chest unlock, for when the player does /unlock <pass>
	 * 
	 * @param player
	 *            the player to register
	 * @param chestID
	 *            the chestID to unlock
	 */
	public void registerAction(String action, String player, int chestID) {
		try {
			/*
			 * We only want 1 action per player, no matter what!
			 */
			unregisterAction(action, player);

			PreparedStatement statement = prepare("INSERT INTO `actions` (action, player, chest) VALUES (?, ?, ?)");
			statement.setString(1, action);
			statement.setString(2, player);
			statement.setInt(3, chestID);

			statement.executeUpdate();

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}
	}

	/**
	 * Register an action, used for various actions (stating the obvious here)
	 * 
	 * @param player
	 *            the player to register
	 * @param data
	 *            data
	 */
	public void registerAction(String action, String player, String data) {
		try {
			/*
			 * We only want 1 action per player, no matter what!
			 */
			unregisterAction(action, player);

			PreparedStatement statement = prepare("INSERT INTO `actions` (action, player, data) VALUES (?, ?, ?)");
			statement.setString(1, action);
			statement.setString(2, player);
			statement.setString(3, data);

			statement.executeUpdate();

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}
	}

	/**
	 * Register a mode to a player (temporary)
	 * 
	 * @param player
	 *            the player to register the mode to
	 * @param mode
	 *            the mode to register
	 */
	public void registerMode(String player, String mode) {
		try {
			PreparedStatement statement = prepare("INSERT INTO `modes` (player, mode) VALUES (?, ?)");
			statement.setString(1, player);
			statement.setString(2, mode);

			statement.executeUpdate();

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}
	}

	/**
	 * Register a mode with data to a player (temporary)
	 * 
	 * @param player
	 *            the player to register the mode to
	 * @param mode
	 *            the mode to register
	 * @param data
	 *            additional data
	 */
	public void registerMode(String player, String mode, String data) {
		try {
			PreparedStatement statement = prepare("INSERT INTO `modes` (player, mode, data) VALUES (?, ?, ?)");
			statement.setString(1, player);
			statement.setString(2, mode);
			statement.setString(3, data);

			statement.executeUpdate();

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}
	}

	/**
	 * Register a pending lock request to a player
	 * 
	 * @param player
	 *            the player to assign the chest to
	 * @param password
	 *            the password to register with
	 */
	public void registerPendingLock(String player, String password) {
		try {
			PreparedStatement statement = prepare("INSERT INTO `locks` (player, password) VALUES (?, ?)");
			statement.setString(1, player);
			statement.setString(2, password);

			statement.executeUpdate();

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}
	}

	/**
	 * Add a player to be allowed to access a chest
	 * 
	 * @param player
	 *            the player to add
	 * @param chestID
	 *            the chest ID to allow them to access
	 */
	public void registerPlayer(String player, int chestID) {
		try {
			PreparedStatement statement = prepare("INSERT INTO `sessions` (player, chest) VALUES(?, ?)");
			statement.setString(1, player);
			statement.setInt(2, chestID);

			statement.executeUpdate();

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}
	}

	/**
	 * Register a pending chest unlock, for when the player does /unlock <pass>
	 * 
	 * @param player
	 *            the player to register
	 * @param chestID
	 *            the chestID to unlock
	 */
	public void registerUnlock(String player, int chestID) {
		registerAction("unlock", player, chestID);
	}

	/**
	 * @return the number of active session
	 */
	public int sessionCount() {
		int count = 0;

		try {
			Statement statement = connection.createStatement();
			final ResultSet set = statement.executeQuery("SELECT `id` FROM `sessions`");

			while (set.next()) {
				count++;
			}

			statement.close();
			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}

		return count;
	}

	/**
	 * Unregister a pending chest unlock
	 * 
	 * @param player
	 *            the player to unregister
	 */
	public void unregisterAction(String action, String player) {
		try {
			PreparedStatement statement = prepare("DELETE FROM `actions` WHERE `action` = ? AND `player` = ?");
			statement.setString(1, action);
			statement.setString(2, player);

			statement.executeUpdate();

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}
	}

	/**
	 * Unregister all of the actions for a player
	 * 
	 * @param player
	 *            the player to unregister
	 */
	public void unregisterAllActions(String player) {
		try {
			PreparedStatement statement = prepare("DELETE FROM `actions` WHERE `player` = ?");
			statement.setString(1, player);

			statement.executeUpdate();

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}
	}

	/**
	 * Remove all the pending chest requests
	 */
	public void unregisterAllChests() {
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM `locks`");

			statement.close();
			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}
	}

	/**
	 * Unregister all of the modes from a player
	 * 
	 * @param player
	 *            the player to unregister all modes from
	 */
	public void unregisterAllModes(String player) {
		try {
			PreparedStatement statement = prepare("DELETE FROM `modes` WHERE `player` = ?");
			statement.setString(1, player);

			statement.executeUpdate();

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}
	}

	/**
	 * Unregister a mode from a player
	 * 
	 * @param player
	 *            the player to register the mode to
	 * @param mode
	 *            the mode to unregister
	 */
	public void unregisterMode(String player, String mode) {
		try {
			PreparedStatement statement = prepare("DELETE FROM `modes` WHERE `player` = ? AND `mode` = ?");
			statement.setString(1, player);
			statement.setString(2, mode);

			statement.executeUpdate();

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}
	}

	/**
	 * Remove a pending lock request from a player
	 * 
	 * @param player
	 *            the player to remove
	 */
	public void unregisterPendingLock(String player) {
		try {
			PreparedStatement statement = prepare("DELETE FROM `locks` WHERE `player` = ?");
			statement.setString(1, player);

			statement.executeUpdate();

			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}
	}

	/**
	 * Remove a player from any sessions
	 * 
	 * @param player
	 *            the player to remove
	 */
	public void unregisterPlayer(String player) {
		try {
			PreparedStatement statement = prepare("DELETE FROM `sessions` WHERE `player` = ?");
			statement.setString(1, player);

			statement.executeUpdate();
			Performance.addMemDBQuery();
		} catch (final Exception e) {
			printException(e);
		}
	}

	/**
	 * Unregister a pending chest unlock
	 * 
	 * @param player
	 *            the player to unregister
	 */
	public void unregisterUnlock(String player) {
		unregisterAction("unlock", player);
	}

}
