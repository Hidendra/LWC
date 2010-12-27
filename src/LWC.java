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

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import com.griefcraft.LWCInfo;
import com.griefcraft.logging.Logger;
import com.griefcraft.model.Entity;
import com.griefcraft.model.EntityTypes;
import com.griefcraft.model.RightTypes;

import com.griefcraft.sql.Database;
import com.griefcraft.sql.MemDB;
import com.griefcraft.sql.PhysDB;
import com.griefcraft.util.Config;
import com.griefcraft.util.ConfigValues;
import com.griefcraft.util.Performance;
import com.griefcraft.util.Updater;

public class LWC extends Plugin {

	/**
	 * The logging object
	 */
	private Logger logger = Logger.getLogger(getClass().getSimpleName());

	/**
	 * The PluginListener
	 */
	private LWCListener listener;

	/**
	 * Physical database instance
	 */
	private PhysDB physicalDatabase;

	/**
	 * Memory database instance
	 */
	private MemDB memoryDatabase;

	/**
	 * Check if a player can access a chest
	 * 
	 * @param player
	 *            the player to check
	 * @param Entity
	 *            the chest to check
	 * @return if the player can access the chest
	 */
	public boolean canAccessChest(Player player, Entity chest) {
		if (chest == null) {
			return true;
		}

		if (isAdmin(player)) {
			return true;
		}

		if (isMod(player)) {
			Player chestOwner = etc.getDataSource().getPlayer(chest.getOwner());

			if (chestOwner == null) {
				return true;
			}

			if (!isAdmin(chestOwner)) {
				return true;
			}
		}

		switch (chest.getType()) {
		case EntityTypes.PUBLIC:
			return true;

		case EntityTypes.PASSWORD:
			return memoryDatabase.hasAccess(player.getName(), chest);

		case EntityTypes.PRIVATE:
			final PhysDB instance = physicalDatabase;
			return player.getName().equalsIgnoreCase(chest.getOwner()) || instance.getPrivateAccess(RightTypes.PLAYER, chest.getID(), player.getName()) != -1 || instance.getPrivateAccess(RightTypes.GROUP, chest.getID(), player.getGroups()) != -1;

		default:
			return false;
		}
	}

	/**
	 * Check if a player can access a chest
	 * 
	 * @param player
	 *            the player to check
	 * @param x
	 *            x coordinate of the chest
	 * @param y
	 *            y coordinate of the chest
	 * @param z
	 *            z coordinate of the chest
	 * @return if the player can access the chest
	 */
	public boolean canAccessChest(Player player, int x, int y, int z) {
		return canAccessChest(player, physicalDatabase.loadProtectedEntity(x, y, z));
	}

	/**
	 * Check if a player can administrate a chest
	 * 
	 * @param player
	 *            the player to check
	 * @param Entity
	 *            the chest to check
	 * @return if the player can administrate the chest
	 */
	public boolean canAdminChest(Player player, Entity chest) {
		if (chest == null) {
			return true;
		}

		if (isAdmin(player)) {
			return true;
		}

		switch (chest.getType()) {
		case EntityTypes.PUBLIC:
			return player.getName().equalsIgnoreCase(chest.getOwner());

		case EntityTypes.PASSWORD:
			return player.getName().equalsIgnoreCase(chest.getOwner()) && memoryDatabase.hasAccess(player.getName(), chest);

		case EntityTypes.PRIVATE:
			final PhysDB instance = physicalDatabase;
			return player.getName().equalsIgnoreCase(chest.getOwner()) || instance.getPrivateAccess(RightTypes.PLAYER, chest.getID(), player.getName()) == 1 || instance.getPrivateAccess(RightTypes.GROUP, chest.getID(), player.getGroups()) == 1;

		default:
			return false;
		}
	}

	@Override
	public void disable() {
		Config.getInstance().save();
	}

	@Override
	public void enable() {
		try {
			log("Initializing LWC");
			
			Performance.init();

			physicalDatabase = new PhysDB();
			memoryDatabase = new MemDB();

			Config.init();

			Updater updater = new Updater();
			updater.check();
			updater.update();

			log("LWC config:      " + LWCInfo.CONF_FILE);
			log("SQLite jar:      lib/sqlite.jar");
			log("SQLite library:  lib/" + updater.getOSSpecificFileName());
			log("DB location:     " + physicalDatabase.getDatabasePath());

			log("Opening sqlite databases");

			physicalDatabase.connect();
			memoryDatabase.connect();

			physicalDatabase.load();
			memoryDatabase.load();

			log("Entity count: " + physicalDatabase.entityCount());
			log("Limit count: " + physicalDatabase.limitCount());
		} catch (Exception e) {
			log("Error occured while initializing LWC : " + e.getMessage());
			e.printStackTrace();
			log("LWC will now be disabled");
			etc.getLoader().disablePlugin("LWC");
		}
	}

	public String encrypt(String plaintext) {
		MessageDigest md = null;

		try {
			md = MessageDigest.getInstance("SHA");
			md.update(plaintext.getBytes("UTF-8"));

			final byte[] raw = md.digest();
			return byteArray2Hex(raw);
		} catch (final Exception e) {

		}

		return "";
	}

	/**
	 * Check for chest limits on a given player and return true if they are limited
	 * 
	 * @param player
	 *            the player to check
	 * @return true if they are limited
	 */
	public boolean enforceChestLimits(Player player) {
		final int userLimit = physicalDatabase.getUserLimit(player.getName());

		/*
		 * Sort of redundant, but use the least amount of queries we can!
		 */
		if (userLimit != -1) {
			final int chests = physicalDatabase.getChestCount(player.getName());

			if (chests >= userLimit) {
				player.sendMessage(Colors.Red + "You have exceeded the amount of chests you can lock!");
				return true;
			}
		} else {
			final int groupLimit = physicalDatabase.getGroupLimit(player.getGroups().length > 0 ? player.getGroups()[0] : "default");

			if (groupLimit != -1) {
				final int chests = physicalDatabase.getChestCount(player.getName());

				if (chests >= groupLimit) {
					player.sendMessage(Colors.Red + "You have exceeded the amount of chests you can lock!");
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Useful for getting double chests TODO: rewrite
	 * 
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 * @param z
	 *            the z coordinate
	 * @return the Chest[] array of chests
	 */
	public List<ComplexBlock> getEntitySet(int x, int y, int z) {
		List<ComplexBlock> entities = new ArrayList<ComplexBlock>(2);

		/*
		 * First check the block they actually clicked
		 */
		ComplexBlock baseBlock = etc.getServer().getComplexBlock(x, y, z);
		int dev = -1;
		boolean isXDir = true;

		entities = _validateChest(entities, baseBlock);
		
		while(true) {
			ComplexBlock block = etc.getServer().getComplexBlock(x + (isXDir ? dev : 0), y, z + (isXDir ? 0 : dev));
			entities = _validateChest(entities, block);
			
			if(dev == 1) {
				if(isXDir) {
					isXDir = false;
					dev = -1;
					continue;
				} else {
					break;
				}
			}
			
			dev = 1;
		}

		return entities;
	}

	/**
	 * @return memory database object
	 */
	public MemDB getMemoryDatabase() {
		return memoryDatabase;
	}

	/**
	 * @return physical database object
	 */
	public PhysDB getPhysicalDatabase() {
		return physicalDatabase;
	}

	public int getPlayerDropTransferTarget(String player) {
		String rawTarget = memoryDatabase.getModeData(player, "dropTransfer");

		try {
			int ret = Integer.parseInt(rawTarget.substring(1));
			return ret;
		} catch (final Throwable t) {
		}

		return -1;
	}

	@Override
	public void initialize() {
		if (!Database.isConnected()) {
			return;
		}

		log("Registering hooks");

		listener = new LWCListener(this);

		registerHook(PluginLoader.Hook.DISCONNECT);
		registerHook(PluginLoader.Hook.COMMAND);
		registerHook(PluginLoader.Hook.BLOCK_RIGHTCLICKED);
		registerHook(PluginLoader.Hook.BLOCK_BROKEN);
		registerHook(PluginLoader.Hook.BLOCK_DESTROYED);
		registerHook(PluginLoader.Hook.OPEN_INVENTORY);
		registerHook(PluginLoader.Hook.EXPLODE);
		
		// registerHook(PluginLoader.Hook.ITEM_DROP); // can't modify inventories correctly at the moment ?
	}

	/**
	 * Check if a player can do admin functions on LWC
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player is an LWC admin
	 */
	public boolean isAdmin(Player player) {
		return player.canUseCommand("/lwcadmin");
	}

	/**
	 * Check if a player can do mod functions on LWC
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player is an LWC mod
	 */
	public boolean isMod(Player player) {
		return player.canUseCommand("/lwcmod");
	}

	/**
	 * Check if a mode is disabled
	 * 
	 * @param mode
	 * @return
	 */
	public boolean isModeBlacklisted(String mode) {
		String blacklistedModes = ConfigValues.BLACKLISTED_MODES.getString();

		if (blacklistedModes.isEmpty()) {
			return false;
		}

		String[] modes = blacklistedModes.split(",");

		for (String _mode : modes) {
			if (mode.equalsIgnoreCase(_mode)) {
				return true;
			}
		}

		return false;
	}

	public void log(String str) {
		logger.log(str);
	}

	/**
	 * Return if the player is in persistent mode
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player is NOT in persistent mode
	 */
	public boolean notInPersistentMode(String player) {
		return !memoryDatabase.hasMode(player, "persist");
	}

	/**
	 * Check if the player is currently drop transferring
	 * 
	 * @param player
	 * @return
	 */
	public boolean playerIsDropTransferring(String player) {
		return memoryDatabase.hasMode(player, "dropTransfer") && memoryDatabase.getModeData(player, "dropTransfer").startsWith("t");
	}

	/**
	 * Send the full help to a player
	 * 
	 * @param player
	 *            the player to send to
	 */
	public void sendFullHelp(Player player) {
		player.sendMessage(Colors.Green + "Welcome to LWC, a Protection mod");
		player.sendMessage("");

		player.sendMessage(Colors.Green + " Commands:");

		player.sendMessage(Colors.LightGreen + "/lwc create - View detailed info on protection types");
		player.sendMessage(Colors.LightGreen + "/lwc create public - Create a public protection");
		player.sendMessage(Colors.LightGreen + "/lwc create password - Create a password protected entity");
		player.sendMessage(Colors.LightGreen + "/lwc create private - Create a private protection");
		player.sendMessage("");

		player.sendMessage(Colors.LightGreen + "/lwc modify - Modify a protected entity");
		player.sendMessage("");

		player.sendMessage(Colors.LightGreen + "/lwc free entity - Remove a protected entity");
		player.sendMessage(Colors.LightGreen + "/lwc free modes - Remove temporary modes on you");
		player.sendMessage("");

		player.sendMessage(Colors.LightGreen + "/lwc unlock - Unlock a password protected entity");
		player.sendMessage(Colors.LightGreen + "/lwc info - View information on a protected entity");
		player.sendMessage("");

		if (!isModeBlacklisted("persist")) {
			player.sendMessage(Colors.LightGreen + "/lwc persist - Allow use of 1 command multiple times");
		}

		if (!isModeBlacklisted("dropTransfer")) {
			player.sendMessage(Colors.LightGreen + "/lwc droptransfer - View Drop Transfer help");
		}

		player.sendMessage("");
		player.sendMessage(Colors.Red + "/lwc admin - (LWC ADMIN) Admin functions");
	}

	public void sendPendingRequest(Player player) {
		player.sendMessage(Colors.Red + "You already have a pending chest request.");
		player.sendMessage(Colors.Red + "To remove it, type /lwc free pending");
	}

	/**
	 * Transform a string into one char
	 * 
	 * @param str
	 *            The string to transform
	 * @param chr
	 *            The char to transform all chars to (ie '*')
	 * @return the transformed string
	 */
	public String transform(String str, char chr) {
		final char[] charArray = str.toCharArray();

		for (int i = 0; i < charArray.length; i++) {
			charArray[i] = chr;
		}

		return new String(charArray);
	}

	/**
	 * Temporary ..
	 * 
	 * @param block
	 * @param size
	 * @deprecated Removal: when getEntitySet( ) is made better (or keep this, it's not too bad!)
	 * @return
	 */
	@Deprecated
	private List<ComplexBlock> _validateChest(List<ComplexBlock> entities, ComplexBlock block) {
		if (block == null) {
			return entities;
		}

		if (entities.size() > 2) {
			return entities;
		}

		if (block instanceof Furnace) {
			if (entities.size() == 0) {

				if (!entities.contains(block)) {
					entities.add(block);
				}

			}

			return entities;
		} else {
			if (entities.size() == 1) {
				ComplexBlock other = entities.get(0);

				if (!(other instanceof Chest) && !(other instanceof DoubleChest)) {
					return entities;
				}
			}

			if (!entities.contains(block)) {
				entities.add(block);
			}
		}
		
		return entities;
	}

	/**
	 * Convert a byte array to hex
	 * 
	 * @param hash
	 *            the hash to convert
	 * @return the converted hash
	 */
	private String byteArray2Hex(byte[] hash) {
		final Formatter formatter = new Formatter();
		for (final byte b : hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	/**
	 * Register a hook with default priority
	 * 
	 * @param hook
	 *            the hook to register
	 */
	private void registerHook(PluginLoader.Hook hook) {
		registerHook(hook, PluginListener.Priority.MEDIUM);
	}

	/**
	 * Register a hook
	 * 
	 * @param hook
	 *            the hook to register
	 * @priority the priority to use
	 */
	private void registerHook(PluginLoader.Hook hook, PluginListener.Priority priority) {
		log("LWCListener -> " + hook.toString());

		etc.getLoader().addListener(hook, listener, this, priority);
	}

}
