package com.griefcraft.lwc;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.griefcraft.modules.admin.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.firestar.mcbans.mcbans;
import com.griefcraft.cache.CacheSet;
import com.griefcraft.logging.Logger;
import com.griefcraft.migration.ConfigPost300;
import com.griefcraft.migration.MySQLPost200;
import com.griefcraft.model.AccessRight;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.modules.create.CreateModule;
import com.griefcraft.modules.destroy.DestroyModule;
import com.griefcraft.modules.flag.FlagModule;
import com.griefcraft.modules.free.FreeModule;
import com.griefcraft.modules.info.InfoModule;
import com.griefcraft.modules.limits.LimitsModule;
import com.griefcraft.modules.lists.ListsModule;
import com.griefcraft.modules.menu.MenuModule;
import com.griefcraft.modules.modes.DropTransferModule;
import com.griefcraft.modules.modes.MagnetModule;
import com.griefcraft.modules.modes.PersistModule;
import com.griefcraft.modules.modify.ModifyModule;
import com.griefcraft.modules.owners.OwnersModule;
import com.griefcraft.modules.redstone.RedstoneModule;
import com.griefcraft.modules.unlock.UnlockModule;
import com.griefcraft.modules.worldguard.WorldGuardModule;
import com.griefcraft.scripting.Module.Result;
import com.griefcraft.scripting.Module;
import com.griefcraft.scripting.ModuleLoader;
import com.griefcraft.scripting.ModuleLoader.Event;
import com.griefcraft.sql.Database;
import com.griefcraft.sql.MemDB;
import com.griefcraft.sql.PhysDB;
import com.griefcraft.util.Colors;
import com.griefcraft.util.Performance;
import com.griefcraft.util.StringUtils;
import com.griefcraft.util.config.Configuration;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class LWC {

	/**
	 * The current instance of LWC (( should only be one ! if 2 are someone made, the first takes precedence ))
	 */
	private static LWC instance;
	
	/**
	 * If LWC is currently enabled
	 */
	public static boolean ENABLED = false;
	
	/**
	 * Core LWC configuration
	 */
	private Configuration configuration;
	
	/**
	 * The module loader
	 */
	private ModuleLoader moduleLoader;

	/**
	 * Logging instance
	 */
	private Logger logger = Logger.getLogger("LWC");
	
	/**
	 * The set of caches
	 */
	private CacheSet caches;

	/**
	 * Memory database instance
	 */
	private MemDB memoryDatabase;

	/**
	 * Physical database instance
	 */
	private PhysDB physicalDatabase;

	/**
	 * Plugin instance
	 */
	private LWCPlugin plugin;

	/**
	 * Checks for updates that need to be pushed to the sql database
	 */
	private UpdateThread updateThread;

	/**
	 * Permissions plugin
	 */
	private PermissionHandler permissions;

	public LWC(LWCPlugin plugin) {
		this.plugin = plugin;
		
		if(instance == null) {
			instance = this;
		}
		
		configuration = Configuration.load("core.yml");
		caches = new CacheSet();
	}
	
	/**
	 * Get the currently loaded LWC instance
	 * 
	 * @return
	 */
	public static LWC getInstance() {
		return instance;
	}
	
	/**
	 * @return the module loader
	 */
	public ModuleLoader getModuleLoader() {
		return moduleLoader;
	}
	
	/**
	 * @return the caches
	 */
	public CacheSet getCaches() {
		return caches;
	}
	
	/**
	 * Remove all modes if the player is not in persistent mode
	 * 
	 * @param player
	 */
	public void removeModes(Player player) {
		if (notInPersistentMode(player.getName())) {
			memoryDatabase.unregisterAllActions(player.getName());
		}
	}
	
	/**
	 * Deposit items into an inventory chest
	 * Works with double chests.
	 * 
	 * @param block
	 * @param itemStack
	 * @return remaining items (if any)
	 */
	public Map<Integer, ItemStack> depositItems(Block block, ItemStack itemStack) {
		BlockState blockState = null;

		if ((blockState = block.getState()) != null && (blockState instanceof ContainerBlock)) {
			Block doubleChestBlock = findAdjacentBlock(block, Material.CHEST);
			ContainerBlock containerBlock = (ContainerBlock) blockState;

			Map<Integer, ItemStack> remaining = containerBlock.getInventory().addItem(itemStack);

			// we have remainders, deal with it
			if (remaining.size() > 0) {
				int key = remaining.keySet().iterator().next();
				ItemStack remainingItemStack = remaining.get(key);

				// is it a double chest ?????
				if (doubleChestBlock != null) {
					ContainerBlock containerBlock2 = (ContainerBlock) doubleChestBlock.getState();
					remaining = containerBlock2.getInventory().addItem(remainingItemStack);
				}

				// recheck remaining in the event of double chest being used
				if (remaining.size() > 0) {
					return remaining;
				}
			}
		}
		
		return new HashMap<Integer, ItemStack>();
	}

	/**
	 * Check if a player has the ability to access a protection
	 * 
	 * @param player
	 * @param block
	 * @return
	 */
	public boolean canAccessProtection(Player player, Block block) {
		Protection protection = findProtection(block);

		if (protection != null) {
			return canAccessProtection(player, protection);
		}

		return false;
	}

	/**
	 * Check if a player has the ability to access a protection
	 * 
	 * @param player
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public boolean canAccessProtection(Player player, int x, int y, int z) {
		return canAccessProtection(player, physicalDatabase.loadProtection(player.getWorld().getName(), x, y, z));
	}

	/**
	 * Check if a player has the ability to access a protection
	 * 
	 * @param player
	 * @param protection
	 * @return
	 */
	public boolean canAccessProtection(Player player, Protection protection) {
		if (protection == null || player == null) {
			return true;
		}

		// call the canAccessProtection hook
		Result canAccess = moduleLoader.dispatchEvent(Event.ACCESS_PROTECTION, player, protection);
		
		if(canAccess != Result.DEFAULT) {
			return canAccess == Result.ALLOW;
		}
		
		if (isAdmin(player)) {
			return true;
		}

		if (isMod(player)) {
			Player protectionOwner = plugin.getServer().getPlayer(protection.getOwner());

			if (protectionOwner == null) {
				return true;
			}

			if (!isAdmin(protectionOwner)) {
				return true;
			}
		}
		
		String playerName = player.getName();

		switch (protection.getType()) {
		case ProtectionTypes.PUBLIC:
			return true;

		case ProtectionTypes.PASSWORD:
			return memoryDatabase.hasAccess(player.getName(), protection);

		case ProtectionTypes.PRIVATE:
			if(playerName.equalsIgnoreCase(protection.getOwner())) {
				return true;
			}
			
			if(protection.getAccess(AccessRight.PLAYER, playerName) >= 0) {
				return true;
			}
			
			if(permissions != null) {
				// TODO: Replace with getGroupProperName sometime, but only supported by Permissions 3.00+
				String groupName = permissions.getGroup(player.getWorld().getName(), playerName);
				
				if(protection.getAccess(AccessRight.GROUP, groupName) >= 0) {
					return true;
				}
			}
			
			return false;

		default:
			return false;
		}
	}

	/**
	 * Check if a player has the ability to administrate a protection
	 * 
	 * @param player
	 * @param block
	 * @return
	 */
	public boolean canAdminProtection(Player player, Block block) {
		Protection protection = findProtection(block);

		if (protection != null) {
			return canAdminProtection(player, protection);
		}

		return false;
	}

	/**
	 * Check if a player has the ability to administrate a protection
	 * 
	 * @param player
	 * @param protection
	 * @return
	 */
	public boolean canAdminProtection(Player player, Protection protection) {
		if (protection == null || player == null) {
			return true;
		}

		// call the canAccessProtection hook
		Result canAdmin = moduleLoader.dispatchEvent(Event.ADMIN_PROTECTION, player, protection);
		
		if(canAdmin != Result.DEFAULT) {
			return canAdmin == Result.ALLOW;
		}

		if (isAdmin(player)) {
			return true;
		}
		
		String playerName = player.getName();

		switch (protection.getType()) {
		case ProtectionTypes.PUBLIC:
			return player.getName().equalsIgnoreCase(protection.getOwner());

		case ProtectionTypes.PASSWORD:
			return player.getName().equalsIgnoreCase(protection.getOwner()) && memoryDatabase.hasAccess(player.getName(), protection);

		case ProtectionTypes.PRIVATE:
			if(playerName.equalsIgnoreCase(protection.getOwner())) {
				return true;
			}
			
			if(protection.getAccess(AccessRight.PLAYER, playerName) == 1) {
				return true;
			}
			
			if(permissions != null) {
				// TODO: Replace with getGroupProperName sometime, but only supported by Permissions 3.00+
				String groupName = permissions.getGroup(player.getWorld().getName(), playerName);
				
				if(protection.getAccess(AccessRight.GROUP, groupName) == 1) {
					return true;
				}
			}
			
			return false;
			
		default:
			return false;
		}
	}

	/**
	 * Free some memory (LWC was disabled)
	 */
	public void destruct() {
		log("Freeing " + Database.DefaultType);

		if (physicalDatabase != null) {
			physicalDatabase.dispose();
		}

		if (memoryDatabase != null) {
			memoryDatabase.dispose();
		}

		if (updateThread != null) {
			updateThread.stop();
			updateThread = null;
		}

		physicalDatabase = null;
		memoryDatabase = null;
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
	 * Enforce access to a protection block
	 * 
	 * @param player
	 * @param block
	 * @return true if the player was granted access
	 */
	public boolean enforceAccess(Player player, Block block) {
		if (block == null) {
			return true;
		}

		Protection protection = findProtection(block);
		boolean hasAccess = canAccessProtection(player, protection);
		// boolean canAdmin = canAdminProtection(player, protection);

		if (protection == null) {
			return true;
		}

		// support for old protection dbs that do not contain the block id
		if (protection.getBlockId() == 0) {
			protection.setBlockId(block.getTypeId());
			updateThread.queueProtectionUpdate(protection);
		}

		// multi-world, update old protections
		if (protection.getWorld() == null || protection.getWorld().isEmpty()) {
			protection.setWorld(block.getWorld().getName());
			updateThread.queueProtectionUpdate(protection);
		}

		if (configuration.getBoolean("core.showNotices", true) && (isAdmin(player) || isMod(player))) {
			boolean isOwner = protection.isOwner(player);
			boolean showMyNotices = configuration.getBoolean("core.showMyNotices", true);
			
			if(!isOwner || (isOwner && showMyNotices)) {
				sendLocale(player, "protection.general.notice.protected", "type", getLocale(protection.typeToString().toLowerCase()), "block", materialToString(block), "owner", protection.getOwner());
			}
		}

		switch (protection.getType()) {
		case ProtectionTypes.PASSWORD:
			if (!hasAccess) {
				getMemoryDatabase().unregisterUnlock(player.getName());
				getMemoryDatabase().registerUnlock(player.getName(), protection.getId());

				sendLocale(player, "protection.general.locked.password", "block", materialToString(block));
			}

			break;

		case ProtectionTypes.PRIVATE:
			if (!hasAccess) {
				sendLocale(player, "protection.general.locked.private", "block", materialToString(block));
			}

			break;

		case ProtectionTypes.TRAP_KICK:
			if (!hasAccess) {
				player.kickPlayer(protection.getData());
				log(player.getName() + " triggered the kick trap: " + protection.toString());
			}
			break;

		case ProtectionTypes.TRAP_BAN:
			if (!hasAccess) {
				Plugin mcbansPlugin;

				/*
				 * See if we have mcbans
				 */
				if ((mcbansPlugin = plugin.getServer().getPluginManager().getPlugin("MCBans")) != null) {
					mcbans mcbans = (mcbans) mcbansPlugin;

					/*
					 * good good, ban them
					 */
					mcbans.mcb_handler.ban(player.getName(), "LWC", protection.getData(), "");
				}

				log(player.getName() + " triggered the ban trap: " + protection.toString());
			}
			break;
		}

		return hasAccess;
	}

	/**
	 * Find a block that is adjacent to another block given a Material
	 * 
	 * @param block
	 * @param material
	 * @param ignore
	 * @return
	 */
	public Block findAdjacentBlock(Block block, Material material, Block... ignore) {
		BlockFace[] faces = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
		List<Block> ignoreList = Arrays.asList(ignore);

		for (BlockFace face : faces) {
			Block adjacentBlock = block.getFace(face);

			if (adjacentBlock.getType() == material && !ignoreList.contains(adjacentBlock)) {
				return adjacentBlock;
			}
		}

		return null;
	}

	/**
	 * Look for a double chest adjacent to a block
	 * 
	 * @param block
	 * @return
	 */
	public Block findAdjacentDoubleChest(Block block) {
		Block adjacentBlock = null;
		Block lastBlock = null;
		List<Block> attempts = new ArrayList<Block>(5);
		attempts.add(block);
		
		int found = 0;

		for (int attempt = 0; attempt < 4; attempt++) {
			Block[] attemptsArray = attempts.toArray(new Block[attempts.size()]);
			
			if((adjacentBlock = findAdjacentBlock(block, Material.CHEST, attemptsArray)) != null) {
				if(findAdjacentBlock(adjacentBlock, Material.CHEST, block) != null) {
					return adjacentBlock;
				}
				
				found ++;
				lastBlock = adjacentBlock;
				attempts.add(adjacentBlock);
			}
		}
		
		if(found > 1) {
			return lastBlock;
		}

		return null;
	}

	/**
	 * Find a protection linked to the block
	 * 
	 * @param block
	 * @return
	 */
	public Protection findProtection(Block block) {
		return findProtection(block.getWorld(), block.getX(), block.getY(), block.getZ());
	}

	/**
	 * Find a protection linked to the block at [x, y, z]
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public Protection findProtection(World world, int x, int y, int z) {
		if (world == null) {
			return null;
		}

		Block block = world.getBlockAt(x, y, z);

		if (block == null) {
			return null;
		}

		// get the possible protections for the selected block
		List<Block> protections = getProtectionSet(world, x, y, z);

		// loop through and check for protected blocks
		for (Block protectableBlock : protections) {
			Protection protection = physicalDatabase.loadProtection(world.getName(), protectableBlock.getX(), protectableBlock.getY(), protectableBlock.getZ());

			if (protection != null) {
				return protection;
			}
		}

		return null;
	}

	/**
	 * Get the locale value for a given key
	 * 
	 * @param key
	 * @param args
	 * @return
	 */
	public String getLocale(String key, Object... args) {
		key = key.replaceAll(" ", "_");
		
		if (!plugin.getLocale().containsKey(key)) {
			return "UNKNOWN_LOCALE_" + key;
		}

		Map<String, Object> bind = parseBinds(args);
		String value = plugin.getLocale().getString(key);

		// apply colors
		for (String colorKey : Colors.localeColors.keySet()) {
			String color = Colors.localeColors.get(colorKey);

			if (value.contains(colorKey)) {
				value = value.replaceAll(colorKey, color);
			}
		}

		// apply binds
		for (String bindKey : bind.keySet()) {
			Object object = bind.get(bindKey);

			value = value.replaceAll("%" + bindKey + "%", object.toString());
		}

		return value;
	}

	/**
	 * @return memory database object
	 */
	public MemDB getMemoryDatabase() {
		return memoryDatabase;
	}

	/**
	 * @return the permissions
	 */
	public PermissionHandler getPermissions() {
		return permissions;
	}

	/**
	 * @return physical database object
	 */
	public PhysDB getPhysicalDatabase() {
		return physicalDatabase;
	}

	/**
	 * @return the plugin class
	 */
	public LWCPlugin getPlugin() {
		return plugin;
	}

	/**
	 * Useful for getting double chests
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

		/*
		 * First check the block they clicked
		 */
		entities = _validateBlock(entities, baseBlock, true);

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
	 * Check if a player is an LWC admin -- Console defaults to *YES*
	 * 
	 * @param sender
	 * @return
	 */
	public boolean isAdmin(CommandSender sender) {
		if (sender instanceof Player) {
			return isAdmin((Player) sender);
		}

		return true;
	}

	/**
	 * @return the configuration object
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

    /**
     * Check if a player has a permissions node
     *
     * @param player
     * @param node
     * @return
     */
    public boolean hasPermission(Player player, String node) {
        if(permissions != null) {
            return permissions.permission(player, node);
        }

        return false;
    }

	/**
	 * Check if a player can do admin functions on LWC
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player is an LWC admin
	 */
	public boolean isAdmin(Player player) {
		if(player.isOp()) {
			if(configuration.getBoolean("core.opIsLWCAdmin", true)) {
				return true;
			}
		}
		
		return hasPermission(player, "lwc.admin");
	}

	/**
	 * Check if a player can do mod functions on LWC
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player is an LWC mod
	 */
	public boolean isMod(Player player) {
		if(permissions != null) {
			if(permissions.has(player, "lwc.mod")) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Check if a mode is enabled
	 * 
	 * @param mode
	 * @return
	 */
	public boolean isModeEnabled(String mode) {
		return configuration.getBoolean("modes." + mode + ".enabled", true);
	}
	
	/**
	 * Check if a mode is whitelisted for a player
	 * 
	 * @param mode
	 * @return
	 */
	public boolean isModeWhitelisted(Player player, String mode) {
		if(permissions == null) {
			return false;
		}
		
		return permissions.permission(player, "lwc.mode." + mode);
	}

	/**
	 * Check a block to see if it is protectable
	 * 
	 * @param block
	 * @return
	 */
	public boolean isProtectable(Block block) {
		return isProtectable(block.getType());
	}
	
	public boolean isProtectable(Material material) {
		return Boolean.parseBoolean(resolveProtectionConfiguration(material, "enabled"));
	}

	/**
	 * Load sqlite (done only when LWC is loaded so memory isn't used unnecessarily)
	 */
	public void load() {
		configuration = Configuration.load("core.yml");
		moduleLoader = new ModuleLoader();
		registerCoreModules();
		
		// check for upgrade before everything else
		ConfigPost300.checkConfigConversion(this);
		plugin.loadDatabase();
		
		Performance.init();
		
		if (LWCInfo.DEVELOPMENT) {
			log("Development mode is ON");
		}

		physicalDatabase = new PhysDB();
		memoryDatabase = new MemDB();
		updateThread = new UpdateThread(this);

		Plugin permissionsPlugin = resolvePlugin("Permissions");

		if (permissionsPlugin != null) {
			permissions = ((Permissions) permissionsPlugin).getHandler();
			logger.log("Using Permissions API...");
		}

		log("Loading " + Database.DefaultType);
		try {
			physicalDatabase.connect();
			memoryDatabase.connect();

			physicalDatabase.load();
			memoryDatabase.load();

			Logger.getLogger(Database.DefaultType.toString()).log("Using: " + StringUtils.capitalizeFirstLetter(physicalDatabase.getConnection().getMetaData().getDriverVersion()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// tell all modules we're loaded
		moduleLoader.loadAll();
		
		// check any major conversions
		MySQLPost200.checkDatabaseConversion(this);
	}
	
	/**
	 * Register the core modules for LWC
	 */
	private void registerCoreModules() {
		// core
		registerModule(new LimitsModule());
		registerModule(new CreateModule());
		registerModule(new ModifyModule());
		registerModule(new DestroyModule());
		registerModule(new FreeModule());
		registerModule(new InfoModule());
		registerModule(new MenuModule());
		registerModule(new UnlockModule());
		registerModule(new OwnersModule());
		
		// admin commands
		registerModule(new BaseAdminModule());
		registerModule(new AdminCache());
		registerModule(new AdminCleanup());
		registerModule(new AdminClear());
		registerModule(new AdminConfig());
		registerModule(new AdminConvert());
		
		registerModule(new AdminFind());
		registerModule(new AdminFlush());
		registerModule(new AdminForceOwner());
		registerModule(new AdminLocale());
		registerModule(new AdminPurge());
		registerModule(new AdminReload());
		registerModule(new AdminRemove());
		registerModule(new AdminReport());
		registerModule(new AdminUpdate());
		registerModule(new AdminVersion());
		registerModule(new AdminView());
        registerModule(new AdminDebug());
		
		// flags
		registerModule(new FlagModule());
		registerModule(new RedstoneModule());
		
		// modes
		registerModule(new PersistModule());
		registerModule(new DropTransferModule());
		registerModule(new MagnetModule());
		
		// non-core modules but are included with LWC anyway
		registerModule(new ListsModule());
		registerModule(new WorldGuardModule());
	}
	
	/**
	 * Register a module
	 * 
	 * @param module
	 */
	private void registerModule(Module module) {
		moduleLoader.registerModule(plugin, module);
	}
	
	/**
	 * Get a plugin by the name and if it is disabled, enable the plugin
	 * 
	 * @param name
	 * @return
	 */
	private Plugin resolvePlugin(String name) {
		Plugin temp = plugin.getServer().getPluginManager().getPlugin(name);
		
		if(temp == null) {
			return null;
		}
		
		if(!temp.isEnabled()) {
			plugin.getServer().getPluginManager().enablePlugin(temp);
		}
		
		return temp;
	}

	/**
	 * Merge inventories into one
	 * 
	 * @param blocks
	 * @return
	 */
	public ItemStack[] mergeInventories(List<Block> blocks) {
		ItemStack[] stacks = new ItemStack[54];
		int index = 0;

		try {
			for (Block block : blocks) {
				if (!(block.getState() instanceof ContainerBlock)) {
					continue;
				}

				ContainerBlock containerBlock = (ContainerBlock) block.getState();
				Inventory inventory = containerBlock.getInventory();

				/*
				 * Add all the items from this inventory
				 */
				for (ItemStack stack : inventory.getContents()) {
					stacks[index] = stack;
					index++;
				}
			}
		} catch (Exception e) {
			return mergeInventories(blocks);
		}

		return stacks;
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
	 * Send the full help to a player
	 * 
	 * @param sender
	 *            the player to send to
	 */
	public void sendFullHelp(CommandSender sender) {
		boolean isPlayer = (sender instanceof Player);
		String menuStyle = "advanced"; // default for console

		if (isPlayer) {
			menuStyle = physicalDatabase.getMenuStyle(((Player) sender).getName()).toLowerCase();
		}

		if (menuStyle.equals("advanced")) {
			sendLocale(sender, "help.advanced");
		} else {
			sendLocale(sender, "help.basic");
		}

		if (isAdmin(sender)) {
			sender.sendMessage("");
			sender.sendMessage(Colors.Red + "/lwc admin - Administration");
		}
	}

	/**
	 * Send a locale to a player or console
	 * 
	 * @param sender
	 * @param key
	 * @param args
	 */
	public void sendLocale(CommandSender sender, String key, Object... args) {
		String message = getLocale(key, args);
		String menuStyle = null; // null unless required!

		if (message == null) {
			sender.sendMessage(Colors.Red + "LWC: " + Colors.White + "Undefined locale: \"" + Colors.Gray + key + Colors.White + "\"");
			return;
		}

		String[] aliasvars = new String[] { "cprivate", "cpublic", "cpassword", "cmodify", "cunlock", "cinfo", "cremove" };

		// apply command name modification depending on menu style
		for (String alias : aliasvars) {
			String replace = "%" + alias + "%";

			if (!message.contains(replace)) {
				continue;
			}

			if (menuStyle == null) {
				menuStyle = (sender instanceof Player) ? physicalDatabase.getMenuStyle(((Player) sender).getName()) : "advanced";
			}

			String localeName = alias + "." + menuStyle;

			message = message.replace(replace, getLocale(localeName));
		}
		// split the lines
		for (String line : message.split("\\n")) {
			if(line.isEmpty()) {
				line = " ";
			}
			
			sender.sendMessage(line);
		}
	}

	/**
	 * Send the simple usage of a command
	 * 
	 * @param player
	 * @param command
	 */
	public void sendSimpleUsage(CommandSender player, String command) {
		// player.sendMessage(Colors.Red + "Usage:" + Colors.Gold + " " +
		// command);
		sendLocale(player, "help.simpleusage", "command", command);
	}

	/**
	 * Ensure a chest/furnace is protectable where it's at
	 * 
	 * @param entities
	 * @param block
	 * @return
	 */
	private List<Block> _validateBlock(List<Block> entities, Block block) {
		return _validateBlock(entities, block, false);
	}

	/**
	 * Ensure a chest/furnace is protectable where it's at
	 * 
	 * @param block
	 * @param block
	 * @param isBaseBlock
	 * @return
	 */
	private List<Block> _validateBlock(List<Block> entities, Block block, boolean isBaseBlock) {
		if (block == null) {
			return entities;
		}

		if (entities.size() > 2) {
			return entities;
		}

		Material type = block.getType();
		Block up = block.getFace(BlockFace.UP);

		if (entities.size() == 1) {
			Block other = entities.get(0);

			switch (other.getTypeId()) {

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
				if (type != Material.CHEST) {
					return entities;
				}

				break;

			/*
			 * Wooden door
			 */
			case 64:
				if (type != Material.WOODEN_DOOR) {
					return entities;
				}

				break;

			/*
			 * Iron door
			 */
			case 71:
				if (type != Material.IRON_DOOR_BLOCK) {
					return entities;
				}

				break;

			}

			if (!entities.contains(block)) {
				entities.add(block);
			}
		} else if (isProtectable(block) && isBaseBlock && !isComplexBlock(block)) {
			entities.add(block);
		} else if (isBaseBlock && (up.getType() == Material.WOODEN_DOOR || up.getType() == Material.IRON_DOOR_BLOCK || type == Material.WOODEN_DOOR || type == Material.IRON_DOOR_BLOCK)) {
			/*
			 * check if they're clicking the block under the door
			 */
			if (type != Material.WOODEN_DOOR && type != Material.IRON_DOOR_BLOCK) {
				entities.clear();
				entities.add(block); // block under the door
				entities.add(block.getFace(BlockFace.UP)); // bottom half
				entities.add(block.getWorld().getBlockAt(block.getX(), block.getY() + 2, block.getZ())); // top
																											// half
			} else {
				entities.clear();
				if (up.getType() == Material.WOODEN_DOOR || up.getType() == Material.IRON_DOOR_BLOCK) {
					entities.add(block); // bottom half
					entities.add(up); // top half
				} else {
					entities.add(block.getFace(BlockFace.DOWN)); // bottom half
					entities.add(block); // top half
				}
			}
		} else if (isBaseBlock && (up.getType() == Material.SIGN_POST || up.getType() == Material.WALL_SIGN || type == Material.SIGN_POST || type == Material.WALL_SIGN)) {
			/*
			 * If it's a wall sign, also protect the wall it's attached to!
			 */

			if (entities.size() == 0) {
				/*
				 * Check if we're clicking on the sign itself, otherwise it's the block above it
				 */
				if (type == Material.SIGN_POST || type == Material.WALL_SIGN) {
					entities.add(block);
				} else {
					entities.add(up);
				}
			}
		} else if (isBaseBlock && (type == Material.FURNACE || type == Material.DISPENSER || type == Material.JUKEBOX)) {
			// protections that are just 1 block
			if (entities.size() == 0) {
				entities.add(block);
			}

			return entities;
		} else if (!isProtectable(block) && entities.size() == 0) {
			/*
			 * Look for a ronery wall sign
			 */
			Block face = null;

			// this shortens it quite a bit, just put the possible faces into an
			// array
			BlockFace[] faces = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };

			/*
			 * Match wall signs to the wall it's attached to
			 */
			for (BlockFace blockFace : faces) {
				if ((face = block.getFace(blockFace)) != null) {
					if (face.getType() == Material.WALL_SIGN) {
						byte direction = face.getData();

						/*
						 * Protect the wall the wall sign is attached to
						 */
						switch (direction) {
						case 0x02: // east
							if (blockFace == BlockFace.EAST) {
								entities.add(face);
							}
							break;

						case 0x03: // west
							if (blockFace == BlockFace.WEST) {
								entities.add(face);
							}
							break;

						case 0x04: // north
							if (blockFace == BlockFace.NORTH) {
								entities.add(face);
							}
							break;

						case 0x05: // south
							if (blockFace == BlockFace.SOUTH) {
								entities.add(face);
							}
							break;
						}

					}

				}
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
	 * Check if a block is more than just protectable blocks (i.e signs, doors)
	 * 
	 * @param block
	 * @return
	 */
	private boolean isComplexBlock(Block block) {
		switch (block.getTypeId()) {
		case 63: // sign post
		case 64: // wood door
		case 68: // wall sign
		case 71: // iron door

			return true;
		}

		return false;
	}

	/**
	 * Log a string
	 * 
	 * @param str
	 */
	private void log(String str) {
		logger.log(str);
	}

	/**
	 * Convert an even-lengthed argument array to a map containing String keys i.e parseBinds("Test", null, "Test2", obj) = Map().put("test", null).put("test2", obj)
	 * 
	 * @param args
	 * @return
	 */
	private Map<String, Object> parseBinds(Object... args) {
		Map<String, Object> bind = new HashMap<String, Object>();

		if (args == null || args.length < 2) {
			return bind;
		}

		int size = args.length;
		for (int index = 0; index < args.length; index += 2) {
			if ((index + 2) > size) {
				break;
			}

			String key = args[index].toString();
			Object object = args[index + 1];

			bind.put(key, object);
		}

		return bind;
	}

	/**
	 * Get a string representation of a block's material
	 * 
	 * @param block
	 * @return
	 */
	public static String materialToString(Block block) {
		return materialToString(block.getType());
	}

	/**
	 * Get a string representation of a block type
	 * 
	 * @param id
	 * @return
	 */
	public static String materialToString(int id) {
		return materialToString(Material.getMaterial(id));
	}

	/**
	 * Get a string representation of a block material
	 * 
	 * @param material
	 * @return
	 */
	public static String materialToString(Material material) {
		if (material != null) {
			return StringUtils.capitalizeFirstLetter(material.toString().replaceAll("_", " "));
		}

		return "";
	}

	/**
	 * Get the appropriate config value for the block (protections.block.node)
	 * 
	 * @param material
	 * @param node
	 * @return
	 */
	public String resolveProtectionConfiguration(Material material, String node) {
		List<String> names = new ArrayList<String>();
		
		String materialName = material.toString().toLowerCase();
		
		// add the name & the block id
		names.add(materialName);
		names.add(material.getId() + "");
		
		// check for the trimmed variant
		String trimmedName = materialName.replaceAll("block", "");
		
		if(trimmedName.endsWith("_")) {
			trimmedName = trimmedName.substring(0, trimmedName.length() - 1);
		}
		
		if(!trimmedName.equals(materialName)) {
			names.add(trimmedName);
		}

        String value = configuration.getString("protections." + node);

		for(String name : names) {
			if(name.contains("sign")) {
				name = "sign";
			}

			String temp = configuration.getString("protections.blocks." + name + "." + node);
			
			if(temp != null && !temp.isEmpty()) {
				value = temp;
			}
		}
		
		return value;
	}

}
