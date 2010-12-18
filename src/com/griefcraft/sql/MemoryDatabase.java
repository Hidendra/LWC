package com.griefcraft.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.griefcraft.model.Action;
import com.griefcraft.model.Chest;

public class MemoryDatabase extends Database {

	/**
	 * Static instance
	 */
	private static MemoryDatabase instance;

	/**
	 * @return an instance of Database
	 */
	public static MemoryDatabase getInstance() {
		if (instance == null) {
			instance = new MemoryDatabase();
		}

		return instance;
	}

	public Action getAction(String action, String player) {
		try {
			final PreparedStatement statement = connection.prepareStatement("SELECT * FROM `actions` WHERE `player` = ? AND `action` = ?");
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

			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
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

			final PreparedStatement statement = connection.prepareStatement("SELECT `chest` FROM `actions` WHERE `action` = ? AND `player` = ?");
			statement.setString(1, action);
			statement.setString(2, player);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				chestID = set.getInt("chest");
			}

			statement.close();

			return chestID;
		} catch (final Exception e) {
			e.printStackTrace();
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
			final PreparedStatement statement = connection.prepareStatement("SELECT `action` FROM `actions` WHERE `player` = ?");
			statement.setString(1, player);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				final String action = set.getString("action");

				actions.add(action);
			}

		} catch (final Exception e) {
			e.printStackTrace();
		}

		return actions;
	}

	/**
	 * @return the path where the database file should be saved
	 */
	@Override
	public String getDatabasePath() {
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

			final PreparedStatement statement = connection.prepareStatement("SELECT `password` FROM `locks` WHERE `player` = ?");
			statement.setString(1, player);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				password = set.getString("password");
			}

			statement.close();

			return password;
		} catch (final Exception e) {
			e.printStackTrace();
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
			final PreparedStatement statement = connection.prepareStatement("SELECT `data` from `modes` WHERE `player` = ? AND `mode` = ?");
			statement.setString(1, player);
			statement.setString(2, mode);

			final ResultSet set = statement.executeQuery();
			if (set.next()) {
				ret = set.getString("data");
			}
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
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
			final PreparedStatement statement = connection.prepareStatement("SELECT * from `modes` WHERE `player` = ?");
			statement.setString(1, player);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				final String mode = set.getString("mode");

				modes.add(mode);
			}

			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
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
			final PreparedStatement statement = connection.prepareStatement("SELECT `player` FROM `sessions` WHERE `chest` = ?");
			statement.setInt(1, chestID);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				final String player = set.getString("player");

				sessionUsers.add(player);
			}

		} catch (final Exception e) {
			e.printStackTrace();
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
	 * @param chest
	 *            the chest to check
	 * @return true if the player has access
	 */
	public boolean hasAccess(String player, Chest chest) {
		if (chest == null) {
			return true;
		}

		return hasAccess(player, chest.getID());
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
			final PreparedStatement statement = connection.prepareStatement("SELECT `player` FROM `sessions` WHERE `chest` = ?");
			statement.setInt(1, chestID);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				final String player2 = set.getString("player");

				if (player.equals(player2)) {
					statement.close();
					return true;
				}
			}

			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return false;
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
			final PreparedStatement statement = connection.prepareStatement("SELECT `id` FROM `locks` WHERE `player` = ?");
			statement.setString(1, player);

			final ResultSet set = statement.executeQuery();

			while (set.next()) {
				statement.close();
				return true;
			}

			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
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
	 * create the in-memory table which hold sessions, users that have activated
	 * a chest. Not needed past a restart, so no need for extra disk i/o
	 */
	@Override
	public void load() {
		try {
			final Statement statement = connection.createStatement();

			log("Creating memory table 'sessions'");

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS 'sessions' (" //
					+ "id INTEGER PRIMARY KEY," //
					+ "player TEXT," //
					+ "chest INTEGER" + ");"); //

			log("Creating memory table 'locks'");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS 'locks' ("
					+ "id INTEGER PRIMARY KEY," //
					+ "player TEXT," //
					+ "password TEXT" + ");"); //

			log("Creating memory table 'actions'");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS 'actions' (" //
					+ "id INTEGER PRIMARY KEY," //
					+ "action TEXT," //
					+ "player TEXT," //
					+ "chest INTEGER," //
					+ "data TEXT" //
					+ ");"); //

			log("Creating memory table 'modes'");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS 'modes' ("
					+ "id INTEGER PRIMARY KEY," //
					+ "player TEXT," //
					+ "mode TEXT," //
					+ "data TEXT" //
					+ ");");

			/**
			 * unlocks -> actions add 'action'
			 * 
			 * unlock = unlock remove chest = unregister
			 * 
			 */

			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Log a string to stdout
	 * 
	 * @param str
	 *            The string to log
	 */
	@Override
	public void log(String str) {
		System.out.println("[LWC->sqlite->memory] " + str);
	}

	/**
	 * @return the number of pending chest locks
	 */
	public int pendingCount() {
		int count = 0;

		try {
			final Statement statement = connection.createStatement();
			final ResultSet set = statement.executeQuery("SELECT `id` FROM `locks`");

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

			final PreparedStatement statement = connection.prepareStatement("INSERT INTO `actions` (action, player, chest) VALUES (?, ?, ?)");
			statement.setString(1, action);
			statement.setString(2, player);
			statement.setInt(3, chestID);

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
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

			final PreparedStatement statement = connection.prepareStatement("INSERT INTO `actions` (action, player, data) VALUES (?, ?, ?)");
			statement.setString(1, action);
			statement.setString(2, player);
			statement.setString(3, data);

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Register a pending chest request to a player
	 * 
	 * @param player
	 *            the player to assign the chest to
	 * @param password
	 *            the password to register with
	 */
	public void registerChest(String player, String password) {
		try {
			final PreparedStatement statement = connection.prepareStatement("INSERT INTO `locks` (player, password) VALUES (?, ?)");
			statement.setString(1, player);
			statement.setString(2, password);

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
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
			final PreparedStatement statement = connection.prepareStatement("INSERT INTO `modes` (player, mode) VALUES (?, ?)");
			statement.setString(1, player);
			statement.setString(2, mode);

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
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
			final PreparedStatement statement = connection.prepareStatement("INSERT INTO `modes` (player, mode, data) VALUES (?, ?, ?)");
			statement.setString(1, player);
			statement.setString(2, mode);
			statement.setString(3, data);

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
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
			final PreparedStatement statement = connection.prepareStatement("INSERT INTO `sessions` (player, chest) VALUES(?, ?)");
			statement.setString(1, player);
			statement.setInt(2, chestID);

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
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
			final Statement statement = connection.createStatement();
			final ResultSet set = statement.executeQuery("SELECT `id` FROM `sessions`");

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
	 * Unregister a pending chest unlock
	 * 
	 * @param player
	 *            the player to unregister
	 */
	public void unregisterAction(String action, String player) {
		try {
			final PreparedStatement statement = connection.prepareStatement("DELETE FROM `actions` WHERE `action` = ? AND `player` = ?");
			statement.setString(1, action);
			statement.setString(2, player);

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
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
			final PreparedStatement statement = connection.prepareStatement("DELETE FROM `actions` WHERE `player` = ?");
			statement.setString(1, player);

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove all the pending chest requests
	 */
	public void unregisterAllChests() {
		try {
			final Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM `locks`");
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
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
			final PreparedStatement statement = connection.prepareStatement("DELETE FROM `modes` WHERE `player` = ?");
			statement.setString(1, player);

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove a pending chest request from a player
	 * 
	 * @param player
	 *            the player to remove
	 */
	public void unregisterChest(String player) {
		try {
			final PreparedStatement statement = connection.prepareStatement("DELETE FROM `locks` WHERE `player` = ?");
			statement.setString(1, player);

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
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
			final PreparedStatement statement = connection.prepareStatement("DELETE FROM `modes` WHERE `player` = ? AND `mode` = ?");
			statement.setString(1, player);
			statement.setString(2, mode);

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
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
			final PreparedStatement statement = connection.prepareStatement("DELETE FROM `sessions` WHERE `player` = ?");
			statement.setString(1, player);

			statement.executeUpdate();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
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
