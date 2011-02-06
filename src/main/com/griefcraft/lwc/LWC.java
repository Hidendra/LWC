package com.griefcraft.lwc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.griefcraft.model.AccessRight;
import com.griefcraft.sql.MemDB;
import com.griefcraft.sql.PhysDB;
import com.griefcraft.util.Colors;
import com.griefcraft.util.ConfigValues;
import com.griefcraft.util.Performance;
import com.griefcraft.util.StringUtils;
import com.nijiko.permissions.Control;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.RegionManager;

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
	 * Development logging
	 */
	private Logger devLogger = Logger.getLogger("Dev");

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
	 * Print a line if development mode is enabled
	 * 
	 * @param str
	 */
	public void dev(String str) {
		if(LWCInfo.DEVELOPMENT) {
			devLogger.info(str);
		}
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
	 * Get a string representation of a block's material
	 * 
	 * @param block
	 * @return
	 */
	public String blockToString(Block block) {
		if (block != null) {
			return StringUtils.capitalizeFirstLetter(block.getType().toString().replaceAll("_", " "));
		}

		return "";
	}

	/**
	 * Enforce access to a protection block
	 * 
	 * @param player
	 * @param block
	 * @return
	 */
	public boolean enforceAccess(Player player, Block block) {
		if (block == null) {
			return true;
		}

		List<Block> protectionSet = getProtectionSet(block.getWorld(), block.getX(), block.getY(), block.getZ());
		boolean hasAccess = true;

		for (final Block _block : protectionSet) {
			if (_block == null) {
				continue;
			}

			final Protection protection = getPhysicalDatabase().loadProtectedEntity(_block.getX(), _block.getY(), _block.getZ());

			if (protection == null) {
				continue;
			}

			hasAccess = canAccessChest(player, protection);

			switch (protection.getType()) {
			case ProtectionTypes.PASSWORD:
				if (!hasAccess) {
					getMemoryDatabase().unregisterUnlock(player.getName());
					getMemoryDatabase().registerUnlock(player.getName(), protection.getID());

					player.sendMessage(Colors.Red + "This " + blockToString(block) + " is locked.");
					player.sendMessage(Colors.Red + "Type " + Colors.Gold + "/lwc -u <password>" + Colors.Red + " to unlock it");
				}

				break;

			case ProtectionTypes.PRIVATE:
				if (!hasAccess) {
					player.sendMessage(Colors.Red + "This " + blockToString(block) + " is locked with a magical spell.");
				}

				break;
			}
		}

		return hasAccess;
	}

	/**
	 * Enforce world guard regions
	 * 
	 * @param player
	 * @return true if a protection should be stopped
	 */
	public boolean enforceWorldGuard(Player player, Block block) {
		/*
		 * Check the configuration
		 */
		if (!ConfigValues.ENFORCE_WORLDGUARD_REGIONS.getBool()) {
			return false;
		}

		Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("WorldGuard");

		try {
			if (plugin != null) {
				/*
				 * World guard is enabled.. let's boogie
				 */
				WorldGuardPlugin worldGuard = (WorldGuardPlugin) plugin;

				/*
				 * Reflect our way in. The values we want are.. protected
				 */
				Field useRegions = worldGuard.getClass().getDeclaredField("useRegions");
				Field regionManager = worldGuard.getClass().getDeclaredField("regionManager");

				/*
				 * Make the fields/methods we want accessible
				 */
				useRegions.setAccessible(true);
				regionManager.setAccessible(true);

				/*
				 * Now check if we're using regions
				 */
				boolean isUsingRegions = useRegions.getBoolean(worldGuard);

				if (!isUsingRegions) {
					return false;
				}

				/*
				 * Now get the region manager
				 */
				RegionManager regions = (RegionManager) regionManager.get(worldGuard);

				/*
				 * We need to reflect into BukkitUtil.toVector
				 */
				Class<?> bukkitUtil = worldGuard.getClass().getClassLoader().loadClass("com.sk89q.worldguard.bukkit.BukkitUtil");
				Method toVector = bukkitUtil.getMethod("toVector", Block.class);
				Vector blockVector = (Vector) toVector.invoke(null, block);

				/*
				 * Now let's get the list of regions at the block we're clicking
				 */
				List<String> regionSet = regions.getApplicableRegionsIDs(blockVector);
				List<String> allowedRegions = Arrays.asList(ConfigValues.WORLDGUARD_ALLOWED_REGIONS.getString().split(","));
				
				boolean deny = true;
				
				/*
				 * Check for *
				 */
				if(ConfigValues.WORLDGUARD_ALLOWED_REGIONS.getString().equals("*")) {
					if(regionSet.size() > 0) {
						 return false;
					}
				}
				
				/*
				 * If there are no regions, we need to deny them
				 */
				for(String region : regionSet) {
					if(allowedRegions.contains(region)) {
						deny = false;
						break;
					}
				}

				if(deny) {
					player.sendMessage(Colors.Red + "You cannot protect that " + blockToString(block) + " outside of WorldGuard regions");
					
					return true;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
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

		if (permissionsPlugin != null) {
			logger.info("Using Nijikokun's permissions plugin for permissions");
			permissions = (Permissions) permissionsPlugin;
		}

		if (ConfigValues.ENFORCE_WORLDGUARD_REGIONS.getBool() && plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
			logger.info("Using WorldGuard protected regions");
		}

		log("Loading SQLite");
		try {
			physicalDatabase.connect();
			memoryDatabase.connect();

			physicalDatabase.load();
			memoryDatabase.load();

			Logger.getLogger("SQLite").info("Using: " + StringUtils.capitalizeFirstLetter(physicalDatabase.getConnection().getMetaData().getDriverVersion()));
		} catch (Exception e) {
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
		} catch (Exception e) {

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
	 * Get a command represented by a specific class
	 * 
	 * @param clazz
	 * @return
	 */
	public ICommand getCommand(Class<?> clazz) {
		for(ICommand command : commands) {
			if(command.getClass() == clazz) {
				return command;
			}
		}

		return null;
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
	 * Check if a block is blacklisted
	 * 
	 * @param block
	 * @return
	 */
	public boolean isBlockBlacklisted(Block block) {
		String blacklist = ConfigValues.PROTECTION_BLACKLIST.getString();

		if (blacklist.isEmpty()) {
			return false;
		}

		String[] values = blacklist.split(",");

		if (values == null || values.length == 0) {
			return false;
		}

		String blockName = blockToString(block).replaceAll(" ", "");
		blockName = blockName.replaceAll("Block", "").trim();

		for (String value : values) {
			String noSpaces = value.replaceAll(" ", "");

			try {
				int id = Integer.parseInt(value);

				if (id == block.getTypeId()) {
					return true;
				}
			} catch (Exception e) {
			}

			if (value.equalsIgnoreCase(blockName) || noSpaces.equalsIgnoreCase(blockName)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check a block to see if it is protectable
	 * 
	 * @param block
	 * @return
	 */
	public boolean isProtectable(Block block) {
		if (isBlockBlacklisted(block)) {
			return false;
		}

		switch (block.getTypeId()) {

		/*
		 * Chest
		 */
		case 54:
			return true;

			/*
			 * Dispenser
			 */
		case 23:
			return true;

			/*
			 * Furnaces
			 */
		case 61:
		case 62:

			return true;

			/*
			 * Bonus: doors
			 */
		case 64:
		case 71:
			return true;

			/*
			 * Bonus: sign
			 */
		case 63:
		case 68:
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
	 * Check for protection limits on a given player and return true if they are limited
	 * We also assume they are not an LWC admin
	 * 
	 * @param player the player to check
	 * @return true if they are limited
	 */
	public boolean enforceProtectionLimits(Player player) {
		int limit;

		/*
		 * Apply player limits
		 */
		limit = physicalDatabase.getPlayerLimit(player.getName());
		
		/*
		 * Apply group limits.. can't be one line however
		 */
		if(limit != -1) {
			Control control = (Control) Permissions.Security;
			String[] groups = control.getGroups(player.getName());

			for (String group : groups) {
				if(limit >= 0) {
					break;
				}
				
				limit = physicalDatabase.getGroupLimit(group);
			}
		}
		
		/*
		 * Apply global limits if need be
		 */
		limit = limit != -1 ? limit : physicalDatabase.getGlobalLimit();

		/*
		 * Alert the user if they're above or at the limit
		 */
		if(limit != -1) {
			int protections = physicalDatabase.getProtectionCount(player.getName());
			
			if(protections >= limit) {
				player.sendMessage(Colors.Red + "You have exceeded your allowed amount of protections!");
				return true;
			}
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
	public List<Block> getProtectionSet(World world, int x, int y, int z) {
		List<Block> entities = new ArrayList<Block>(2);

		Block baseBlock = world.getBlockAt(x, y, z);
		Material baseType = baseBlock.getType();

		/*
		 * First check the block they clicked
		 */
		entities = _validateBlock(entities, baseBlock);

		/*
		 * First check if it's a door
		 */
		if(baseType == Material.WOODEN_DOOR || baseType == Material.IRON_DOOR_BLOCK) {
			entities = _validateBlock(entities, world.getBlockAt(x, y + 1, z));
			entities = _validateBlock(entities, world.getBlockAt(x, y - 1, z));

			return entities;
		}

		int dev = -1;
		boolean isXDir = true;

		while (true) {
			Block block = world.getBlockAt(x + (isXDir ? dev : 0), y, z + (isXDir ? 0 : dev));
			entities = _validateBlock(entities, block);

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
	private List<Block> _validateBlock(List<Block> entities, Block block) {
		if (block == null) {
			return entities;
		}

		if (entities.size() > 2) {
			return entities;
		}

		Material type = block.getType();

		if (type == Material.FURNACE || type == Material.DISPENSER || type == Material.SIGN || type == Material.SIGN_POST) {
			if (entities.size() == 0) {

				if (!entities.contains(block)) {
					entities.add(block);
				}

			}

			return entities;
		} else {
			if (entities.size() == 1) {
				Block other = entities.get(0);

				switch(other.getTypeId()) {

				/*
				 * Furnace
				 */
				case 61:
				case 62:
					return entities;

					/*
					 * Dispensers
					 */
				case 23:
					return entities;

					/*
					 * Sign
					 */
				case 63:
				case 68:
					return entities;

					/*
					 * Chest
					 */
				case 54:
					if (other.getType() != Material.CHEST) {
						return entities;
					}

					break;

					/*
					 * Wooden door
					 */
				case 64:
					if (other.getType() != Material.WOODEN_DOOR) {
						return entities;
					}

					break;

					/*
					 * Iron door
					 */
				case 71:
					if (other.getType() != Material.IRON_DOOR_BLOCK) {
						return entities;
					}

					break;

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
		player.sendMessage("/lwc -c  " + Colors.Blue + "View creation help");
		player.sendMessage("/lwc -c " + Colors.LightBlue + "<public|private|password>");
		player.sendMessage("/lwc -m  " + Colors.Blue + "Modify an existing private protection");
		player.sendMessage("/lwc -u  " + Colors.Blue + "Unlock a passworded protection");
		player.sendMessage("/lwc -i   " + Colors.Blue + "View information on a protected Chest or Furnace");
		player.sendMessage("/lwc -r " + Colors.LightBlue + "<protection|modes>");

		player.sendMessage("/lwc -p " + Colors.LightBlue + "<persist|" + Colors.Black + "droptransfer" + Colors.LightBlue + ">"); // TODO: dynamic

		if (isAdmin(player)) {
			player.sendMessage("");
			player.sendMessage(Colors.Red + "/lwc admin - Administration");
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
			return player.getName().equalsIgnoreCase(chest.getOwner()) || physicalDatabase.getPrivateAccess(AccessRight.PLAYER, chest.getID(), player.getName()) >= 0;
			// return player.getName().equalsIgnoreCase(chest.getOwner()) || physicalDatabase.getPrivateAccess(RightTypes.PLAYER, chest.getID(), player.getName()) >= 0 ||
			// physicalDatabase.getPrivateAccess(RightTypes.GROUP, chest.getID(), player.getGroups()) >= 0;

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
			return player.getName().equalsIgnoreCase(chest.getOwner()) || physicalDatabase.getPrivateAccess(AccessRight.PLAYER, chest.getID(), player.getName()) == 1;
			// return player.getName().equalsIgnoreCase(chest.getOwner()) || physicalDatabase.getPrivateAccess(RightTypes.PLAYER, chest.getID(), player.getName()) == 1 ||
			// physicalDatabase.getPrivateAccess(RightTypes.GROUP, chest.getID(), player.getGroups()) == 1;

		default:
			return false;
		}
	}

}
