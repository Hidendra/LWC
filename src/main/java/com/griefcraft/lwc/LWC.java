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

package com.griefcraft.lwc;

import com.firestar.mcbans.mcbans;
import com.griefcraft.cache.CacheSet;
import com.griefcraft.integration.ICurrency;
import com.griefcraft.integration.IPermissions;
import com.griefcraft.integration.currency.BOSECurrency;
import com.griefcraft.integration.currency.EssentialsCurrency;
import com.griefcraft.integration.currency.NoCurrency;
import com.griefcraft.integration.currency.iConomy5Currency;
import com.griefcraft.integration.currency.iConomy6Currency;
import com.griefcraft.integration.permissions.BukkitPermissions;
import com.griefcraft.integration.permissions.NijiPermissions;
import com.griefcraft.integration.permissions.NoPermissions;
import com.griefcraft.integration.permissions.PEXPermissions;
import com.griefcraft.integration.permissions.SuperPermsPermissions;
import com.griefcraft.jobs.JobManager;
import com.griefcraft.migration.ConfigPost300;
import com.griefcraft.migration.MySQLPost200;
import com.griefcraft.model.AccessRight;
import com.griefcraft.model.Flag;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.modules.admin.*;
import com.griefcraft.modules.create.CreateModule;
import com.griefcraft.modules.credits.CreditsModule;
import com.griefcraft.modules.debug.DebugModule;
import com.griefcraft.modules.destroy.DestroyModule;
import com.griefcraft.modules.doors.DoorsModule;
import com.griefcraft.modules.flag.BaseFlagModule;
import com.griefcraft.modules.flag.MagnetModule;
import com.griefcraft.modules.free.FreeModule;
import com.griefcraft.modules.info.InfoModule;
import com.griefcraft.modules.limits.LimitsModule;
import com.griefcraft.modules.lists.ListsModule;
import com.griefcraft.modules.menu.MenuModule;
import com.griefcraft.modules.modes.BaseModeModule;
import com.griefcraft.modules.modes.DropTransferModule;
import com.griefcraft.modules.modes.NoSpamModule;
import com.griefcraft.modules.modes.PersistModule;
import com.griefcraft.modules.modify.ModifyModule;
import com.griefcraft.modules.owners.OwnersModule;
import com.griefcraft.modules.redstone.RedstoneModule;
import com.griefcraft.modules.schedule.ScheduleModule;
import com.griefcraft.modules.towny.TownyModule;
import com.griefcraft.modules.unlock.UnlockModule;
import com.griefcraft.modules.worldguard.WorldGuardModule;
import com.griefcraft.scripting.Module;
import com.griefcraft.scripting.ModuleLoader;
import com.griefcraft.scripting.event.LWCAccessEvent;
import com.griefcraft.scripting.event.LWCSendLocaleEvent;
import com.griefcraft.sql.Database;
import com.griefcraft.sql.PhysDB;
import com.griefcraft.util.Colors;
import com.griefcraft.util.Performance;
import com.griefcraft.util.StringUtils;
import com.griefcraft.util.UpdateThread;
import com.griefcraft.util.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
     * The job manager
     */
    private JobManager jobManager;

    /**
     * Logging instance
     */
    private Logger logger = Logger.getLogger("LWC");

    /**
     * The set of caches
     */
    private CacheSet caches;

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
     * The permissions handler
     */
    private IPermissions permissions;

    /**
     * Sigh
     */
    private NijiPermissions removeMeAndRemoveNijiPermissionsButIfItIsRemovedAllHellBreaksLoose;

    /**
     * The currency handler
     */
    private ICurrency currency;

    /**
     * Whether or not we utilize logic to work around the Bukkit 656 bug.  http://leaky.bukkit.org/issues/656
     */
    private boolean bug656workaround;

    public LWC(LWCPlugin plugin) {
        this.plugin = plugin;
        LWC.instance = this;

        configuration = Configuration.load("core.yml");
        moduleLoader = new ModuleLoader();
        jobManager = new JobManager(this);
        caches = new CacheSet();

        bug656workaround = configuration.getBoolean("core.bukkitBug656workaround", false);
    }

    /**
     * Create an LWCPlayer object for a player
     *
     * @param sender
     * @return
     */
    public LWCPlayer wrapPlayer(CommandSender sender) {
        if (sender instanceof LWCPlayer) {
            return (LWCPlayer) sender;
        }

        if (!(sender instanceof Player)) {
            return null;
        }

        return LWCPlayer.getPlayer((Player) sender);
    }

    /**
     * Return true if the Bug 565 workaround flag is enabled.
     *
     * @return
     */
    public boolean isBug656WorkAround() {
        return bug656workaround;
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
     * @return the job manager
     */
    public JobManager getJobManager() {
        return jobManager;
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
     * @param sender
     */
    public void removeModes(CommandSender sender) {
        if (sender instanceof Player) {
            Player bPlayer = (Player) sender;

            if (notInPersistentMode(bPlayer.getName())) {
                wrapPlayer(bPlayer).getActions().clear();
            }
        } else if (sender instanceof LWCPlayer) {
            removeModes(((LWCPlayer) sender).getBukkitPlayer());
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
        BlockState blockState;

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

        return protection != null && canAccessProtection(player, protection);

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
        LWCAccessEvent event = new LWCAccessEvent(player, protection, AccessRight.RIGHT_NOACCESS);
        moduleLoader.dispatchEvent(event);

        if (event.getAccess() != AccessRight.RIGHT_NOACCESS) {
            return event.getAccess() >= AccessRight.RIGHT_PLAYER;
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
                return wrapPlayer(player).getAccessibleProtections().contains(protection);

            case ProtectionTypes.PRIVATE:
                if (playerName.equalsIgnoreCase(protection.getOwner())) {
                    return true;
                }

                if (protection.getAccess(AccessRight.PLAYER, playerName) >= 0) {
                    return true;
                }

                for (String groupName : permissions.getGroups(player)) {
                    if (protection.getAccess(AccessRight.GROUP, groupName) >= 0) {
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

        return protection != null && canAdminProtection(player, protection);

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
        LWCAccessEvent event = new LWCAccessEvent(player, protection, AccessRight.RIGHT_NOACCESS);
        moduleLoader.dispatchEvent(event);

        if (event.getAccess() != AccessRight.RIGHT_NOACCESS) {
            return event.getAccess() == AccessRight.RIGHT_ADMIN;
        }

        if (isAdmin(player)) {
            return true;
        }

        String playerName = player.getName();

        switch (protection.getType()) {
            case ProtectionTypes.PUBLIC:
                return player.getName().equalsIgnoreCase(protection.getOwner());

            case ProtectionTypes.PASSWORD:
                return player.getName().equalsIgnoreCase(protection.getOwner()) && wrapPlayer(player).getAccessibleProtections().contains(protection);

            case ProtectionTypes.PRIVATE:
                if (playerName.equalsIgnoreCase(protection.getOwner())) {
                    return true;
                }

                if (protection.getAccess(AccessRight.PLAYER, playerName) == 1) {
                    return true;
                }

                for (String groupName : permissions.getGroups(player)) {
                    if (protection.getAccess(AccessRight.GROUP, groupName) == 1) {
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

        // destroy the modules
        moduleLoader.shutdown();
        moduleLoader = null;

        log("Flushing final updates (" + updateThread.size() + ")");

        if (updateThread != null) {
            updateThread.stop();
            updateThread = null;
        }

        log("Freeing " + Database.DefaultType);

        if (physicalDatabase != null) {
            physicalDatabase.dispose();
        }

        physicalDatabase = null;
    }

    /**
     * Encrypt a string using SHA1
     *
     * @param text
     * @return
     */
    public String encrypt(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(text.getBytes("UTF-8"));

            final byte[] raw = md.digest();
            return byteArray2Hex(raw);
        } catch (Exception e) {

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
            protection.save();
        }

        // multi-world, update old protections
        if (protection.getWorld() == null || protection.getWorld().isEmpty()) {
            protection.setWorld(block.getWorld().getName());
            protection.save();
        }

        // update timestamp
        if (hasAccess) {
            long timestamp = System.currentTimeMillis() / 1000L;

            protection.setLastAccessed(timestamp);
            protection.save();
        }

        if (configuration.getBoolean("core.showNotices", true)) {
            boolean isOwner = protection.isOwner(player);
            boolean showMyNotices = configuration.getBoolean("core.showMyNotices", true);

            if (isAdmin(player) || isMod(player) || (isOwner && showMyNotices)) {
                String owner = protection.getOwner();

                // replace your username with "you" if you own the protection
                if (owner.equals(player.getName())) {
                    owner = getLocale("you");
                }

                String blockName = materialToString(block);

                if (!getLocale("protection." + blockName.toLowerCase() + ".notice.protected").startsWith("UNKNOWN_LOCALE")) {
                    sendLocale(player, "protection." + blockName.toLowerCase() + ".notice.protected", "type", getLocale(protection.typeToString().toLowerCase()), "block", blockName, "owner", owner);
                } else {
                    sendLocale(player, "protection.general.notice.protected", "type", getLocale(protection.typeToString().toLowerCase()), "block", blockName, "owner", owner);
                }
            }
        }

        switch (protection.getType()) {
            case ProtectionTypes.PASSWORD:
                if (!hasAccess) {
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
                    player.kickPlayer(protection.getPassword());
                    log(player.getName() + " triggered the kick trap: " + protection.toString());
                }
                break;

            case ProtectionTypes.TRAP_BAN:
                if (!hasAccess) {
                    Plugin mcbansPlugin;

                    // See if we have mcbans
                    if ((mcbansPlugin = plugin.getServer().getPluginManager().getPlugin("MCBans")) != null) {
                        mcbans mcbans = (mcbans) mcbansPlugin;

                        // ban then locally
                        mcbans.mcb_handler.ban(player.getName(), "LWC", protection.getPassword(), "");
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
        BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        List<Block> ignoreList = Arrays.asList(ignore);

        for (BlockFace face : faces) {
            Block adjacentBlock = block.getRelative(face);

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
        Block adjacentBlock;
        Block lastBlock = null;
        List<Block> attempts = new ArrayList<Block>(5);
        attempts.add(block);

        int found = 0;

        for (int attempt = 0; attempt < 4; attempt++) {
            Block[] attemptsArray = attempts.toArray(new Block[attempts.size()]);

            if ((adjacentBlock = findAdjacentBlock(block, Material.CHEST, attemptsArray)) != null) {
                if (findAdjacentBlock(adjacentBlock, Material.CHEST, block) != null) {
                    return adjacentBlock;
                }

                found++;
                lastBlock = adjacentBlock;
                attempts.add(adjacentBlock);
            }
        }

        if (found > 1) {
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
//            log("findProtection: checking protectableBlock world="+world.getName()+",x="+protectableBlock.getX()+",y="+protectableBlock.getY()+"z="+protectableBlock.getZ());
            Protection protection = physicalDatabase.loadProtection(world.getName(), protectableBlock.getX(), protectableBlock.getY(), protectableBlock.getZ());

            if (protection != null) {

                /* For the bug #656 workaround, if the protection we got from the DB was not for the
                     * block we were originally passed, then we've found an adjacent protection, such as for
                     * double chests. We record the protection we found so that if Bukkit later starts
                     * returning bogus blocks, we have already recorded the protected adjacent block and
                     * so it will stay protected.
                     */
                if (bug656workaround) {
                    // note, it's not a bug that I'm not checking the world here. getProtectionSet() above
                    // is guaranteed to return a maximum of 2 blocks and they are guaranteed to be
                    // adjacent to each other, thus we already know both blocks are in the same world.
                    if (protectableBlock.getX() != x || protectableBlock.getY() != y
                            || protectableBlock.getZ() != z) {
//            			log(" found adjacent protectableBlock: " + protectableBlock + " for x="+x+",y="+y+"z="+z);
                        if (physicalDatabase.getCachedProtection(world.getName(), x, y, z) == null) {
//            				log(": [DEBUG] caching LWC adjacent block: "+protectableBlock.toString());
                            physicalDatabase.addCachedProtection(world.getName(), x, y, z, protection);
                        }
                    }
                }

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
     * @return the Permissions handler
     */
    public IPermissions getPermissions() {
        return permissions;
    }

    /**
     * @return the Currency handler
     */
    public ICurrency getCurrency() {
        return currency;
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
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the Chest[] array of chests
     */
    public List<Block> getProtectionSet(World world, int x, int y, int z) {
        List<Block> entities = new ArrayList<Block>(3);

        if (world == null) {
            return entities;
        }

        Block baseBlock = world.getBlockAt(x, y, z);

        /*
          * First check the block they clicked either way -- incase that chunk isn't affected by bug 656
          */
        entities = _validateBlock(entities, baseBlock, true);

        /* Normal logic is to check the block they clicked to see if it's a "valid" block.
         * Since bug #656 doesn't accurately report block state, this results in valid blocks
         * getting dropped from the protection list.  The workaround just applies the protection
         * to the given x,y,z block regardless of what Bukkit says the state/material of that
         * block is.
         */
        if (bug656workaround && entities.size() == 0) {
            entities.add(baseBlock);
        } else {
        }
        int dev = -1;
        boolean isXDir = true;

        /* This loop checks each block in 1 direction on the X/Z axis, so it checks a total of
         * four blocks: (X-1, Z); (X+1,Z); (X,Z-1); (X,Z+1)
         * It is looking for any protectable blocks in that range, primarily looking for double chests.
         * - comment by morganm 6/19/2011, code by Hidendra
         */
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

//		log("getProtectionSet.preCache: entities.size()="+entities.size()+", baseblock="+baseBlock);

        /* Normal logic is to check the block they clicked to see if it's a "valid" block.
         * Since bug #656 doesn't accurately report block state, this results in valid blocks
         * getting dropped from the protection list.  The workaround just applies the protection
         * to the given x,y,z block regardless of what Bukkit says the state/material of that
         * block is.
         */
        if (entities.isEmpty()) {
            findCachedProtection(entities, baseBlock);
        }

//		log("getProtectionSet.exit: entities.size()="+entities.size()+", baseblock="+baseBlock);

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
        return !(sender instanceof Player) || isAdmin((Player) sender);

    }

    /**
     * @return the configuration object
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Check if a player has either access to lwc.admin or the specified node
     *
     * @param sender
     * @param node
     * @return
     */
    public boolean hasAdminPermission(CommandSender sender, String node) {
        return isAdmin(sender) || hasPermission(sender, node, "lwc.admin");
    }

    /**
     * Check if a player has either access to lwc.protect or the specified node
     *
     * @param sender
     * @param node
     * @return
     */
    public boolean hasPlayerPermission(CommandSender sender, String node) {
        return hasPermission(sender, node, "lwc.protect");

    }

    /**
     * Check a player for a node, using a fallback as a default (e.g lwc.protect)
     *
     * @param sender
     * @param node
     * @param fallback
     * @return
     */
    public boolean hasPermission(CommandSender sender, String node, String... fallback) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        boolean hasNode = hasPermission(player, node);

        if (!hasNode) {
            for (String temp : fallback) {
                if (hasPermission(player, temp)) {
                    return true;
                }
            }
        }

        return hasNode;
    }

    /**
     * Check if a player has a permissions node
     *
     * @param player
     * @param node
     * @return
     */
    public boolean hasPermission(Player player, String node) {
        boolean result = false;

        try {
            result = player.hasPermission(node);
        } catch (NoSuchMethodError e) {
            // their server does not support Superperms..
            return !node.contains("admin") && !node.contains("mod");
        }

        if (!result) {
            // Temporary .. (?)
            if (removeMeAndRemoveNijiPermissionsButIfItIsRemovedAllHellBreaksLoose != null) {
                result = removeMeAndRemoveNijiPermissionsButIfItIsRemovedAllHellBreaksLoose.permission(player, node);
            }
        }

        return result;
    }

    /**
     * Check if a player can do admin functions on LWC
     *
     * @param player the player to check
     * @return true if the player is an LWC admin
     */
    public boolean isAdmin(Player player) {
        if (player.isOp()) {
            if (configuration.getBoolean("core.opIsLWCAdmin", true)) {
                return true;
            }
        }

        return hasPermission(player, "lwc.admin");
    }

    /**
     * Check if a player can do mod functions on LWC
     *
     * @param player the player to check
     * @return true if the player is an LWC mod
     */
    public boolean isMod(Player player) {
        return hasPermission(player, "lwc.mod");
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
        return hasPermission(player, "lwc.mode." + mode, "lwc.allmodes");

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
        registerCoreModules();

        // check for upgrade before everything else
        new ConfigPost300().run();
        plugin.loadDatabase();

        Performance.init();

        physicalDatabase = new PhysDB();
        updateThread = new UpdateThread(this);

        // Permissions init
        permissions = new NoPermissions();

        if (resolvePlugin("PermissionsBukkit") != null) {
            permissions = new BukkitPermissions();
        } else if (resolvePlugin("PermissionsEx") != null) {
            permissions = new PEXPermissions();
        } else {
            // check for SuperPerms
            {
                try {
                    Method method = CraftHumanEntity.class.getDeclaredMethod("hasPermission", String.class);

                    if (method != null) {
                        permissions = new SuperPermsPermissions();
                    }
                } catch(NoSuchMethodException e) {
                    // server does not support SuperPerms
                }
            }
        }

        if (resolvePlugin("Permissions") != null) {
            removeMeAndRemoveNijiPermissionsButIfItIsRemovedAllHellBreaksLoose = new NijiPermissions();
        }

        // Currency init
        currency = new NoCurrency();

        if (resolvePlugin("iConomy") != null) {
            // We need to figure out which iConomy plugin we have...
            Plugin plugin = resolvePlugin("iConomy");

            // get the class name
            String className = plugin.getClass().getName();

            // check for the iConomy5 package
            try {
                if (className.startsWith("com.iConomy")) {
                    currency = new iConomy5Currency();
                } else {
                    // iConomy 6!
                    currency = new iConomy6Currency();
                }
            } catch (NoClassDefFoundError e) { }
        } else if (resolvePlugin("BOSEconomy") != null) {
            currency = new BOSECurrency();
        } else if (resolvePlugin("Essentials") != null) {
            currency = new EssentialsCurrency();
        }

        log("Permissions API: " + Colors.Red + permissions.getClass().getSimpleName());
        log("Currency API: " + Colors.Red + currency.getClass().getSimpleName());

        log("Loading " + Database.DefaultType);
        try {
            physicalDatabase.connect();
            physicalDatabase.load();

            log("Using: " + StringUtils.capitalizeFirstLetter(physicalDatabase.getConnection().getMetaData().getDriverVersion()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // check any major conversions
        new MySQLPost200().run();

        // precache lots of protections
        physicalDatabase.precache();

        // We are now done loading!
        moduleLoader.loadAll();
        jobManager.load();
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
        registerModule(new DoorsModule());
        registerModule(new DebugModule());
        registerModule(new CreditsModule());
        registerModule(new ScheduleModule());

        // admin commands
        registerModule(new BaseAdminModule());
        registerModule(new AdminCache());
        registerModule(new AdminCleanup());
        registerModule(new AdminClear());
        registerModule(new AdminConfig());
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
        registerModule(new AdminQuery());
        registerModule(new AdminPurgeBanned());
        registerModule(new AdminExpire());
        registerModule(new AdminDump());

        // flags
        registerModule(new BaseFlagModule());
        registerModule(new RedstoneModule());
        registerModule(new MagnetModule());

        // modes
        registerModule(new BaseModeModule());
        registerModule(new PersistModule());
        registerModule(new DropTransferModule());
        registerModule(new NoSpamModule());

        // non-core modules but are included with LWC anyway (not a lot of functionality in them, generally ..)
        registerModule(new ListsModule());
        registerModule(new WorldGuardModule());
        registerModule(new TownyModule());
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
     * Get a plugin by the name. Does not have to be enabled, and will remain disabled if it is disabled.
     *
     * @param name
     * @return
     */
    private Plugin resolvePlugin(String name) {
        Plugin temp = plugin.getServer().getPluginManager().getPlugin(name);

        if (temp == null) {
            return null;
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
     * @param player the player to check
     * @return true if the player is NOT in persistent mode
     */
    public boolean notInPersistentMode(String player) {
        return !wrapPlayer(Bukkit.getServer().getPlayer(player)).hasMode("persist");
    }

    /**
     * Send the full help to a player
     *
     * @param sender the player to send to
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

        // broadcast an event if they are a player
        if (sender instanceof Player) {
            LWCSendLocaleEvent evt = new LWCSendLocaleEvent((Player) sender, key);
            moduleLoader.dispatchEvent(evt);

            // did they cancel it?
            if (evt.isCancelled()) {
                return;
            }
        }

        if (message == null) {
            sender.sendMessage(Colors.Red + "LWC: " + Colors.White + "Undefined locale: \"" + Colors.Gray + key + Colors.White + "\"");
            return;
        }

        String[] aliasvars = new String[]{"cprivate", "cpublic", "cpassword", "cmodify", "cunlock", "cinfo", "cremove"};

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
            if (line.isEmpty()) {
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
     * Used only when Bukkit #656 flag is enabled, this call will look for any cached protections
     * related to the given block.
     *
     * @param block
     * @return
     */
    private boolean findCachedProtection(List<Block> entities, Block block) {
        boolean foundProtection = false;

        if (physicalDatabase.getCachedProtection(block.getWorld().getName(), block.getX(), block.getY(), block.getZ()) != null) {
            // if the block isn't part of the list, see if it should be, and if so, add it
            if (!entities.contains(block)) {
                entities.add(block);
            }

            foundProtection = true;
        }

        return foundProtection;
    }

    /**
     * This is very similar to _validateBlock(), except we return all blocks related to a given
     * protection.  The primary difference is that for a door block, this will return both door
     * blocks and the block underneath the door, whereas _validateBlock() will generally only
     * return both door blocks unless the baseBlock happens to be the the underneath block.
     * <p/>
     * The primary purpose of this method is for the bug656 workaround, to make sure we accurately
     * unlock all related "locked" blocks when /cremove is used.
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @return the blocks found related to the protection at the block passed in (if any)
     */
    public List<Block> getRelatedBlocks(World world, int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);

        // let getProtectionSet()/_validateBlock do most of the work
        List<Block> entities = getProtectionSet(world, x, y, z);

        // now check if it was a door, that we've included the block underneath the door also
        switch (block.getTypeId()) {
            case 64:    // wooden door
            case 71:    // iron door
                Block down = block.getRelative(BlockFace.DOWN);
                if (down.getTypeId() == 64 || down.getTypeId() == 71) {
                    down = down.getRelative(BlockFace.DOWN);
                }

                if (!entities.contains(down))
                    entities.add(down);
        }

        return entities;
    }

    /**
     * Check if a block is destroyed when the block below it is destroyed
     *
     * @param block
     * @return
     */
    private boolean isDestroyedByGravity(Block block) {
        switch (block.getType()) {
            case SIGN_POST:
            case RAILS:
            case POWERED_RAIL:
            case DETECTOR_RAIL:
                return true;
        }

        return false;
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
        Block up = block.getRelative(BlockFace.UP);

        if (entities.size() == 1) {
            Block other = entities.get(0);

            switch (other.getTypeId()) {

                // Furnace
                case 61:
                case 62:
                    return entities;

                // Dispenser
                case 23:
                    return entities;

                // Sign / Wall sign
                case 63:
                case 68:
                    return entities;

                // Chest
                case 54:
                    if (type != Material.CHEST) {
                        return entities;
                    }

                    break;

                // Wooden door
                case 64:
                    if (type != Material.WOODEN_DOOR) {
                        return entities;
                    }

                    break;

                // Iron door
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
            // check if they're clicking the block under the door
            if (type != Material.WOODEN_DOOR && type != Material.IRON_DOOR_BLOCK) {
                entities.clear();
                entities.add(block); // block under the door
                entities.add(block.getRelative(BlockFace.UP)); // bottom half
                entities.add(block.getWorld().getBlockAt(block.getX(), block.getY() + 2, block.getZ())); // top
                // half
            } else {
                entities.clear();
                if (up.getType() == Material.WOODEN_DOOR || up.getType() == Material.IRON_DOOR_BLOCK) {
                    entities.add(block); // bottom half
                    entities.add(up); // top half
                } else {
                    entities.add(block.getRelative(BlockFace.DOWN)); // bottom half
                    entities.add(block); // top half
                }
            }
        } else if (isBaseBlock && (isDestroyedByGravity(block) || isDestroyedByGravity(up))) {
            // If it's a block that is destroyed when the block below it is destroyed, protect it!

            if (entities.size() == 0) {
                // Check if we're clicking on the special block itself, otherwise it's the block above it
                if (isProtectable(block)) {
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
        } else if (isBaseBlock && !isProtectable(block)) {
            // Look for a ronery wall sign
            Block face;

            // this shortens it quite a bit, just put the possible faces into an
            // array
            BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

            // Match wall signs to the wall it's attached to
            for (BlockFace blockFace : faces) {
                if ((face = block.getRelative(blockFace)) != null) {
                    byte direction = face.getData();

                    if (face.getType() == Material.WALL_SIGN) {
                        // Protect the wall the sign is attached to
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
                    } else if (face.getType() == Material.TRAP_DOOR) {
                        switch (direction) {
                            case 0x00: // west
                                if (blockFace == BlockFace.EAST) {
                                    entities.add(face);
                                }
                                break;

                            case 0x01: // east
                                if (blockFace == BlockFace.WEST) {
                                    entities.add(face);
                                }
                                break;

                            case 0x02: // south
                                if (blockFace == BlockFace.NORTH) {
                                    entities.add(face);
                                }
                                break;

                            case 0x03: // north
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
     * @param hash the hash to convert
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
        str = "LWC: " + str;
        logger.info(ChatColor.stripColor(str));
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
    private static String materialToString(Material material) {
        if (material != null) {
            String materialName = normalizeName(material);

            // attempt to match the locale
            String locale = LWC.getInstance().getLocale(materialName.toLowerCase());

            // if it starts with UNKNOWN_LOCALE, use the default material name
            if (locale.startsWith("UNKNOWN_LOCALE_")) {
                locale = materialName;
            }

            return StringUtils.capitalizeFirstLetter(locale);
        }

        return "";
    }

    /**
     * Normalize a name to a more readable & usable form.
     * <p/>
     * E.g sign_post/wall_sign = Sign, furnace/burning_furnace = Furnace,
     * iron_door_block = iron_door
     *
     * @param material
     * @return
     */
    private static String normalizeName(Material material) {
        String name = material.toString().toLowerCase().replaceAll("block", "");

        // some name normalizations
        if (name.contains("sign")) {
            name = "Sign";
        }

        if (name.contains("furnace")) {
            name = "furnace";
        }

        if (name.endsWith("_")) {
            name = name.substring(0, name.length() - 1);
        }

        return name.toLowerCase();
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

        String materialName = normalizeName(material);

        // add the name & the block id
        names.add(materialName);
        names.add(material.getId() + "");

        if (!materialName.equals(material.toString().toLowerCase())) {
            names.add(material.toString().toLowerCase());
        }

        String value = configuration.getString("protections." + node);

        for (String name : names) {
            String temp = configuration.getString("protections.blocks." + name + "." + node);

            if (temp != null && !temp.isEmpty()) {
                value = temp;
            }
        }

        return value;
    }

    /**
     * @return true if history logging is enabled
     */
    public boolean isHistoryEnabled() {
        return ! configuration.getBoolean("core.disableHistory", false);
    }

    /**
     * Remove protections very quickly with raw SQL calls
     *
     * @param sender
     * @param where
     * @param shouldRemoveBlocks
     * @return
     */
    public int fastRemoveProtections(CommandSender sender, String where, boolean shouldRemoveBlocks) {
        List<Integer> toRemove = new LinkedList<Integer>();
        List<Block> removeBlocks = null;
        int totalProtections = physicalDatabase.getProtectionCount();
        int completed = 0;
        int count = 0;

        // flush all changes to the database before working on the live database
        updateThread.flush();

        if (shouldRemoveBlocks) {
            removeBlocks = new LinkedList<Block>();
        }

        if (where != null || !where.trim().isEmpty()) {
            where = " WHERE " + where.trim();
        }

        sender.sendMessage("Loading protections via STREAM mode");

        try {
            Statement resultStatement = physicalDatabase.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            if(physicalDatabase.getType() == Database.Type.MySQL) {
                resultStatement.setFetchSize(Integer.MIN_VALUE);
            }

            String prefix = physicalDatabase.getPrefix();
            ResultSet result = resultStatement.executeQuery("SELECT id, owner, type, x, y, z, data, blockId, world, password, date, last_accessed FROM " + prefix + "protections" + where);

            while (result.next()) {
                Protection protection = physicalDatabase.resolveProtection(result);
                World world = protection.getBukkitWorld();

                // check if the protection is exempt from being removed
                if (protection.hasFlag(Flag.Type.EXEMPTION)) {
                    continue;
                }

                count++;

                if (count % 100000 == 0 || count == totalProtections || count == 1) {
                    sender.sendMessage(Colors.Red + count + " / " + totalProtections);
                }

                if (world == null) {
                    continue;
                }

                // remove the protection
                toRemove.add(protection.getId());

                // remove the block ?
                if (shouldRemoveBlocks) {
                    removeBlocks.add(protection.getBlock());
                }

                completed++;
            }

            // Close the streaming statement
            result.close();
            resultStatement.close();

            // flush all of the queries
            fullRemoveProtections(sender, toRemove);

            if (shouldRemoveBlocks) {
                removeBlocks(sender, removeBlocks);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return completed;
    }

    /**
     * Remove a list of blocks from the world
     *
     * @param sender
     * @param blocks
     */
    private void removeBlocks(CommandSender sender, List<Block> blocks) {
        int count = 0;

        for (Block block : blocks) {
            if (block == null || !isProtectable(block)) {
                continue;
            }

            // possibility of a double chest
            if (block.getType() == Material.CHEST) {
                Block doubleChest = findAdjacentBlock(block, Material.CHEST);

                if (doubleChest != null) {
                    removeInventory(doubleChest);
                    doubleChest.setType(Material.AIR);
                }
            }

            // remove the inventory from the block if it has one
            removeInventory(block);

            // and now remove the block
            block.setType(Material.AIR);

            count++;
        }

        sender.sendMessage("Removed " + count + " blocks from the world");
    }

    /**
     * Remove the inventory from a block
     *
     * @param block
     */
    private void removeInventory(Block block) {
        if (block == null) {
            return;
        }

        if (!(block.getState() instanceof ContainerBlock)) {
            return;
        }

        ContainerBlock container = (ContainerBlock) block.getState();
        container.getInventory().clear();
    }

    /**
     * Push removal changes to the database
     *
     * @param sender
     * @param toRemove
     */
    private void fullRemoveProtections(CommandSender sender, List<Integer> toRemove) throws SQLException {
        final StringBuilder builder = new StringBuilder();
        final int total = toRemove.size();
        int count = 0;

        // iterate over the items to remove
        Iterator<Integer> iter = toRemove.iterator();

        // the database prefix
        String prefix = getPhysicalDatabase().getPrefix();

        // create the statement to use
        Statement statement = getPhysicalDatabase().getConnection().createStatement();

        while (iter.hasNext()) {
            int protectionId = iter.next();

            if (count % 100000 == 0) {
                builder.append("DELETE FROM " + prefix + "protections WHERE id IN (" + protectionId);
            } else {
                builder.append("," + protectionId);
            }

            if (count % 100000 == 99999 || count == (total - 1)) {
                builder.append(")");
                statement.executeUpdate(builder.toString());
                builder.setLength(0);

                sender.sendMessage(Colors.Green + "REMOVED " + (count + 1) + " / " + total);
            }

            count++;
        }

        statement.close();
    }

}
