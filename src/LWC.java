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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
	 * The updater instance
	 */
	private Updater updater;

	/**
	 * List of commands
	 */
	private List<Command> commands;

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
		log("Stopping LWC");
		Config.getInstance().save();
		Config.destroy();
		
		etc.getInstance().removeCommand("/lwc");

		try {
			physicalDatabase.connection.close();
			memoryDatabase.connection.close();

			physicalDatabase = null;
			memoryDatabase = null;
		} catch (Exception e) {

		}
	}
	
	/**
	 * @return the updater instance
	 */
	public Updater getUpdater() {
		return updater;
	}

	@Override
	public void enable() {
		try {
			log("Initializing LWC");

			Performance.init();

			commands = new ArrayList<Command>();
			physicalDatabase = new PhysDB();
			memoryDatabase = new MemDB();
			
			log("Binding commands");
			loadCommands();
			etc.getInstance().addCommand("/lwc", "- Chest/Furnace protection");

			Config.init();

			updater = new Updater();
			updater.check();
			updater.update();
			
			if(ConfigValues.AUTO_UPDATE.getBool()) {
				if(updater.checkDist()) {
					log("Reloading LWC");
					etc.getLoader().reloadPlugin("LWC");
					return;
				}
			}

			log("LWC config:      " + LWCInfo.CONF_FILE);
			log("SQLite jar:      lib/sqlite.jar");
			log("SQLite library:  lib/" + updater.getOSSpecificFileName());
			log("DB location:     " + physicalDatabase.getDatabasePath());

			log("Opening sqlite databases");

			physicalDatabase.connect();
			memoryDatabase.connect();

			physicalDatabase.load();
			memoryDatabase.load();

			log("Protections:\t" + physicalDatabase.entityCount());
			log("Limits:\t\t" + physicalDatabase.limitCount());
			
			if(ConfigValues.CUBOID_SAFE_AREAS.getBool()) {
				log("Only allowing chests to be protected in Cuboid-protected zones that DO NOT have PvP toggled!");
			}
			
			Config.getInstance().save();
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
			List<String> inheritedGroups = new ArrayList<String>();
			String groupName = player.getGroups().length > 0 ? player.getGroups()[0] : etc.getInstance().getDefaultGroup().Name;

			inheritedGroups.add(groupName);

			/**
			 * Recurse down the user's group tree
			 */
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
		}

		return false;
	}

	/**
	 * @return the commands
	 */
	public List<Command> getCommands() {
		return commands;
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

		while (true) {
			ComplexBlock block = etc.getServer().getComplexBlock(x + (isXDir ? dev : 0), y, z + (isXDir ? 0 : dev));
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
		// registerHook(PluginLoader.Hook.BLOCK_RIGHTCLICKED);
		registerHook(PluginLoader.Hook.BLOCK_BROKEN);
		registerHook(PluginLoader.Hook.BLOCK_DESTROYED);
		registerHook(PluginLoader.Hook.OPEN_INVENTORY);
		registerHook(PluginLoader.Hook.EXPLODE);
		registerHook(PluginLoader.Hook.ITEM_DROP);
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
	public boolean isPlayerDropTransferring(String player) {
		return memoryDatabase.hasMode(player, "dropTransfer") && memoryDatabase.getModeData(player, "dropTransfer").startsWith("t");
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
	 * Check if a player is in a cuboid safe zone. When calling this, you can assume
	 * the result WILL be false if CuboidPlugin is not enabled OR the config option
	 * CUBOID_SAFE_AREAS is FALSE
	 * 
	 * @param player
	 * @return
	 */
	public boolean isInCuboidSafeZone(Player player) {
		/* Check if the config option is enabled. If it isn't.. well, don't continue! */
		if(!ConfigValues.CUBOID_SAFE_AREAS.getBool()) {
			return false;
		}
		
		Class<?> cuboidClass, cuboidPluginClass;
		
		try {
			Plugin cuboidPlugin = etc.getLoader().getPlugin("CuboidPlugin");
			
			if(cuboidPlugin == null) {
				player.sendMessage("CuboidPlugin is not activated");
				return false;
			}
			
			/* Load Cuboid's areas class that checks for protected areas */
			cuboidClass = cuboidPlugin.getClass().getClassLoader().loadClass("CuboidAreas");
			
			/* Call the static method CuboidAreas.findCuboidArea */
			Method findCuboidArea = cuboidClass.getMethod("findCuboidArea", int.class, int.class, int.class);
			
			/* Get the cuboid object. If this is null, we either have a problem or it's just not protected! */
			Object cuboidC = findCuboidArea.invoke(null, (int) player.getX(), (int) player.getY(), (int) player.getZ());
			
			/* They're in a protected area.. Let's check to see if it's PvP enabled or not */
			if(cuboidC != null) {
				/* Make it accessible.. By default, it's the default accessor :( */
				Field pvp = cuboidC.getClass().getDeclaredField("PvP");
				pvp.setAccessible(true);
				
				boolean isPvP = pvp.getBoolean(cuboidC);
				
				return isPvP;
			} else {
				/* We need the CuboidPlugin class now, let's load it! */
				cuboidPluginClass = cuboidPlugin.getClass().getClassLoader().loadClass("CuboidPlugin");
				
				/* Now we need to check if global pvp is on or off (It's static!!) */
				Field globalDisablePvP = cuboidPluginClass.getDeclaredField("globalDisablePvP");
				globalDisablePvP.setAccessible(true);
				
				boolean isPvP = !globalDisablePvP.getBoolean(null);
				
				return isPvP;
			}
		} catch(Exception e) {
			return false;
		}
	}

	/**
	 * Inform the player they already have a pening request
	 * 
	 * @param player
	 */
	public void sendPendingRequest(Player player) {
		player.sendMessage(Colors.Red + "You already have a pending chest request.");
		player.sendMessage(Colors.Red + "To remove it, type /lwc free pending");
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
	 * Temporary ..
	 * 
	 * @param block
	 * @param size
	 * @return
	 */
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

	/***
	 * Load all of the commands
	 * TODO: commands
	 */
	private void loadCommands() {
		registerCommand(Command_Admin.class);
		registerCommand(Command_Create.class);
		registerCommand(Command_Free.class);
		registerCommand(Command_Info.class);
		registerCommand(Command_Modes.class);
		registerCommand(Command_Modify.class);
		registerCommand(Command_Unlock.class);
	}

	/**
	 * Register a command
	 * 
	 * @param command
	 */
	private void registerCommand(Class<?> clazz) {
		try {
			Command command = (Command) clazz.newInstance();
			commands.add(command);
			log("Loaded command : " + clazz.getSimpleName());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
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
