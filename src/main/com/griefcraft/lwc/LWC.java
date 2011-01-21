package com.griefcraft.lwc;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.griefcraft.commands.ICommand;
import com.griefcraft.logging.Logger;
import com.griefcraft.model.InventoryCache;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.model.RightTypes;
import com.griefcraft.sql.MemDB;
import com.griefcraft.sql.PhysDB;
import com.griefcraft.util.Colors;
import com.griefcraft.util.ConfigValues;
import com.griefcraft.util.Performance;
import com.griefcraft.util.StringUtils;
import com.nijikokun.bukkit.Permissions.Permissions;

public class LWC {

	/**
	 * Plugin instance
	 */
	private LWCPlugin plugin;
	
	/**
	 * Logging instance
	 */
	private Logger logger = Logger.getLogger("LWC");

	/**
	 * Checks for updates that need to be pushed to the sql database
	 */
	private UpdateThread updateThread;
	
	/**
	 * The inventory cache (their contents)
	 */
	private InventoryCache inventoryCache;

	/**
	 * Physical database instance
	 */
	private PhysDB physicalDatabase;

	/**
	 * Memory database instance
	 */
	private MemDB memoryDatabase;
	
	/**
	 * Permissions plugin
	 */
	private Permissions permissions;
	
	/**
	 * List of commands
	 */
	private List<ICommand> commands;
	
	public LWC(LWCPlugin plugin) {
		this.plugin = plugin;
		commands = new ArrayList<ICommand>();
	}
	
	/**
	 * @return the inventory cache
	 */
	public InventoryCache getInventoryCache() {
		return inventoryCache;
	}
	
	/**
	 * @return the update thread
	 */
	public UpdateThread getUpdateThread() {
		return updateThread;
	}
	
	/**
	 * @return the plugin version
	 */
	public double getVersion() {
		return Double.parseDouble(plugin.getDescription().getVersion());
	}
	
	/**
	 * Load sqlite (done only when LWC is loaded so memory isn't used unnecessarily)
	 */
	public void load() {
		Performance.init();
		
		log("Dev mode: " + Boolean.toString(LWCInfo.DEVELOPMENT).toUpperCase());
		
		inventoryCache = new InventoryCache();
		physicalDatabase = new PhysDB();
		memoryDatabase = new MemDB();
		updateThread = new UpdateThread(this);
		
		Plugin permissionsPlugin = plugin.getServer().getPluginManager().getPlugin("Permissions");
		
		if(permissionsPlugin != null) {
			logger.info("Using Nijikokun's permissions plugin for permissions");
			permissions = (Permissions) permissionsPlugin;
		}

		log("Loading SQLite");
		try {
			physicalDatabase.connect();
			memoryDatabase.connect();
			
			physicalDatabase.load();
			memoryDatabase.load();

			Logger.getLogger("SQLite").info("Using: " + StringUtils.capitalizeFirstLetter(physicalDatabase.getConnection().getMetaData().getDriverVersion()));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Log a string
	 * 
	 * @param str
	 */
	private void log(String str) {
		logger.info(str);
	}
	
	/**
	 * Free some memory (LWC was disabled)
	 */
	public void destruct() {
		log("Freeing SQLite");
		
		try {
			physicalDatabase.getConnection().close();
			memoryDatabase.getConnection().close();
		} catch(Exception e) {
			
		}
		
		updateThread.stop();
		updateThread = null;
		inventoryCache = null;
		physicalDatabase = null;
		memoryDatabase = null;
	}
	
	/**
	 * @return the plugin class
	 */
	public LWCPlugin getPlugin() {
		return plugin;
	}
	
	/**
	 * @return the list of commands
	 */
	public List<ICommand> getCommands() {
		return commands;
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

	/**
	 * Check if a player can do admin functions on LWC
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player is an LWC admin
	 */
	public boolean isAdmin(Player player) {
		return (ConfigValues.OP_IS_LWCADMIN.getBool() && player.isOp()) || (permissions != null && Permissions.Security.permission(player, "lwc.admin"));
		// return player.canUseCommand("/lwcadmin");
	}

	/**
	 * Check if a player can do mod functions on LWC
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player is an LWC mod
	 */
	public boolean isMod(Player player) {
		return (permissions != null && Permissions.Security.permission(player, "lwc.mod"));
		// return player.canUseCommand("/lwcmod");
	}
	
	/**
	 * @return the permissions
	 */
	public Permissions getPermissions() {
		return permissions;
	}

	/**
	 * Send simple usage of a command
	 * 
	 * @param player
	 * @param command
	 */
	public void sendSimpleUsage(Player player, String command) {
		player.sendMessage(Colors.Red + "Usage:" + Colors.Gold + " " + command);
	}

	/**
	 * Check a block to see if it is protectable
	 * 
	 * @param block
	 * @return
	 */
	public boolean isProtectable(Block block) {
		switch (block.getTypeId()) {

		case 54: /* Chest */
			return true;
			
		case 23: /* Dispenser! */
			return true;

		case 61: /* Furnace */
		case 62: /* Lit furnace */
			/* if (ALLOW_FURNACE_PROTECTION.getBool()) {
				return true;
			} */

			return true;

		}

		return false;
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
	public boolean isPlayerDropTransferring(String player) {
		return memoryDatabase.hasMode(player, "dropTransfer") && memoryDatabase.getModeData(player, "dropTransfer").startsWith("t");
	}

	/**
	 * Encrypt a string using SHA1
	 * 
	 * @param plaintext
	 * @return
	 */
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
		if (isAdmin(player)) {
			return false;
		}

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
			// no groups yet
			// TODO: fix when applicable
			/*
			
			List<String> inheritedGroups = new ArrayList<String>();
			String groupName = player.getGroups().length > 0 ? player.getGroups()[0] : etc.getInstance().getDefaultGroup().Name;

			inheritedGroups.add(groupName);

			while (true) {
				Group group = etc.getDataSource().getGroup(groupName);

				if (group == null) {
					break;
				}

				String[] inherited = group.InheritedGroups;

				if (inherited == null || inherited.length == 0) {
					break;
				}

				groupName = inherited[0];

				for (String _groupName : inherited) {
					_groupName = _groupName.trim();

					if (_groupName.isEmpty()) {
						continue;
					}

					inheritedGroups.add(_groupName);
				}
			}

			for (String group : inheritedGroups) {
				final int groupLimit = physicalDatabase.getGroupLimit(group);

				if (groupLimit != -1) {
					final int chests = physicalDatabase.getChestCount(player.getName());

					if (chests >= groupLimit) {
						player.sendMessage(Colors.Red + "You have exceeded the amount of chests you can lock!");
						return true;
					}
					
					return false;
				}
			}
			
			*/
		}

		return false;
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
	public List<Block> getEntitySet(World world, int x, int y, int z) {
		List<Block> entities = new ArrayList<Block>(2);

		/*
		 * First check the block they actually clicked
		 */
		Block baseBlock = world.getBlockAt(x, y, z);
		int dev = -1;
		boolean isXDir = true;

		entities = _validateChest(entities, baseBlock);
		// entities = _validateChest(entities, world.getBlockAt(x + 1, y, z));
		// entities = _validateChest(entities, world.getBlockAt(x - 1, y, z));
		// entities = _validateChest(entities, world.getBlockAt(x, y, z + 1));
		// entities = _validateChest(entities, world.getBlockAt(x, y, z - 1));

		while (true) {
			Block block = world.getBlockAt(x + (isXDir ? dev : 0), y, z + (isXDir ? 0 : dev));
			entities = _validateChest(entities, block);

			if (dev == 1) {
				if (isXDir) {
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
	 * Ensure a chest/furnace is protectable where it's at
	 * 
	 * @param block
	 * @param size
	 * @return
	 */
	private List<Block> _validateChest(List<Block> entities, Block block) {
		if (block == null) {
			return entities;
		}

		if (entities.size() > 2) {
			return entities;
		}

		Material type = block.getType();

		if (type == Material.FURNACE || type == Material.DISPENSER) {
			if (entities.size() == 0) {

				if (!entities.contains(block)) {
					entities.add(block);
				}

			}

			return entities;
		} 
		else {
			if (entities.size() == 1) {
				Block other = entities.get(0);

				if (other.getType() != Material.CHEST) {
					return entities;
				}
			}

			if (!entities.contains(block) && isProtectable(block)) {
				entities.add(block);
			}
		}

		return entities;
	}

	/**
	 * Get the drop transfer target for a player
	 * 
	 * @param player
	 * @return
	 */
	public int getPlayerDropTransferTarget(String player) {
		String rawTarget = memoryDatabase.getModeData(player, "dropTransfer");

		try {
			int ret = Integer.parseInt(rawTarget.substring(1));
			return ret;
		} catch (final Throwable t) {
		}

		return -1;
	}

	/**
	 * Send the full help to a player
	 * 
	 * @param player
	 *            the player to send to
	 */
	public void sendFullHelp(Player player) {
		player.sendMessage(" ");
		player.sendMessage(Colors.Green + "Welcome to LWC, a Protection mod");
		player.sendMessage(" ");
		player.sendMessage(Colors.LightGreen + "/lwc -c - View creation help");
		player.sendMessage(Colors.LightGreen + "/lwc -c <public|private|password>");
		player.sendMessage(Colors.LightGreen + "/lwc -m - Modify an existing private protection");
		player.sendMessage(Colors.LightGreen + "/lwc -u - Unlock a password protected entity");
		player.sendMessage(Colors.LightGreen + "/lwc -i  - View information on a protected Chest or Furnace");
		player.sendMessage(Colors.LightGreen + "/lwc -r <chest|furnace|modes>");

		player.sendMessage(Colors.LightGreen + "/lwc -p <persist|droptransfer>"); // TODO: dynamic

		if (isAdmin(player)) {
			player.sendMessage("");
			player.sendMessage(Colors.Red + "/lwc admin - Admin functions");
		}
	}

	/**
	 * Check if a player can access a chest
	 * 
	 * @param player
	 *            the player to check
	 * @param Entity
	 *            the chest to check
	 * @return if the player can access the chest
	 */
	public boolean canAccessChest(Player player, Protection chest) {
		if (chest == null) {
			return true;
		}

		if (isAdmin(player)) {
			return true;
		}

		if (isMod(player)) {
			Player chestOwner = plugin.getServer().getPlayer(chest.getOwner());

			if (chestOwner == null) {
				return true;
			}

			if (!isAdmin(chestOwner)) {
				return true;
			}
		}

		switch (chest.getType()) {
		case ProtectionTypes.PUBLIC:
			return true;

		case ProtectionTypes.PASSWORD:
			return memoryDatabase.hasAccess(player.getName(), chest);

		case ProtectionTypes.PRIVATE:
			return player.getName().equalsIgnoreCase(chest.getOwner()) || physicalDatabase.getPrivateAccess(RightTypes.PLAYER, chest.getID(), player.getName()) >= 0;
			// return player.getName().equalsIgnoreCase(chest.getOwner()) || physicalDatabase.getPrivateAccess(RightTypes.PLAYER, chest.getID(), player.getName()) >= 0 || physicalDatabase.getPrivateAccess(RightTypes.GROUP, chest.getID(), player.getGroups()) >= 0;

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
	public boolean canAdminChest(Player player, Protection chest) {
		if (chest == null) {
			return true;
		}

		if (isAdmin(player)) {
			return true;
		}

		switch (chest.getType()) {
		case ProtectionTypes.PUBLIC:
			return player.getName().equalsIgnoreCase(chest.getOwner());

		case ProtectionTypes.PASSWORD:
			return player.getName().equalsIgnoreCase(chest.getOwner()) && memoryDatabase.hasAccess(player.getName(), chest);

		case ProtectionTypes.PRIVATE:
			return player.getName().equalsIgnoreCase(chest.getOwner()) || physicalDatabase.getPrivateAccess(RightTypes.PLAYER, chest.getID(), player.getName()) == 1;
			// return player.getName().equalsIgnoreCase(chest.getOwner()) || physicalDatabase.getPrivateAccess(RightTypes.PLAYER, chest.getID(), player.getName()) == 1 || physicalDatabase.getPrivateAccess(RightTypes.GROUP, chest.getID(), player.getGroups()) == 1;

		default:
			return false;
		}
	}
	
}
