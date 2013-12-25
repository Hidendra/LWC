/*
 * Copyright 2011 Tyler Blair. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package com.griefcraft.modules.pluginsupport;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Permission;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCAccessEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.util.Colors;
import com.griefcraft.util.config.Configuration;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class WorldGuard extends JavaModule {

    /**
     * The WorldGuard module configuration
     */
    private Configuration configuration = Configuration.load("worldguard.yml");

    /**
     * The world guard plugin if it is enabled
     */
    private WorldGuardPlugin worldGuard = null;

    @Override
    public void load(LWC lwc) {
        Plugin plugin = lwc.getPlugin().getServer().getPluginManager().getPlugin("WorldGuard");

        if (plugin != null) {
            worldGuard = (WorldGuardPlugin) plugin;
        }
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("a", "admin")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if (!args[0].equals("purgeregion") && !args[0].equals("protectregion")) {
            return;
        }

        // The command name to send to them
        String commandName = args[0];

        event.setCancelled(true);

        // check for worldguard
        if (worldGuard == null) {
            sender.sendMessage(Colors.Red + "WorldGuard is not enabled.");
            return;
        }

        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc admin " + commandName + " <RegionName> [World]");
            return;
        }

        if (!(sender instanceof Player) && args.length < 3) {
            sender.sendMessage(Colors.Red + "You must specify the world name the region is in since you are not logged in as a player.");
            return;
        }

        // the region
        String regionName = args[1];

        // the world the region is in
        String worldName = args.length > 2 ? args[2] : "";

        // get the world to use
        World world;

        if (!worldName.isEmpty()) {
            world = lwc.getPlugin().getServer().getWorld(worldName);
        } else {
            world = ((Player) sender).getWorld();
        }

        // was the world found?
        if (world == null) {
            sender.sendMessage(Colors.Red + "Invalid world.");
            return;
        }

        GlobalRegionManager regions = worldGuard.getGlobalRegionManager();

        // get the region manager for the world
        RegionManager regionManager = regions.get(world);

        // try and get the region
        ProtectedRegion region = regionManager.getRegion(regionName);

        if (region == null) {
            sender.sendMessage(Colors.Red + "Region not found. If you region is in a different world than you, please use: /lwc admin " + commandName + " " + regionName + " WorldName");
            return;
        }

        BlockVector minimum = region.getMinimumPoint();
        BlockVector maximum = region.getMaximumPoint();

        // Min values
        int minBlockX = minimum.getBlockX();
        int minBlockY = minimum.getBlockY();
        int minBlockZ = minimum.getBlockZ();

        // Max values
        int maxBlockX = maximum.getBlockX();
        int maxBlockY = maximum.getBlockY();
        int maxBlockZ = maximum.getBlockZ();

        // Calculate the amount of the blocks in the region
        int numBlocks = (maxBlockX - minBlockX + 1) * (maxBlockY - minBlockY + 1) * (maxBlockZ - minBlockZ + 1);

        if (args[0].equals("purgeregion")) {
            // get all of the protections inside of the region
            List<Protection> protections = lwc.getPhysicalDatabase().loadProtections(world.getName(), minBlockX, maxBlockX, minBlockY, maxBlockY, minBlockZ, maxBlockZ);

            // remove all of them
            for (Protection protection : protections) {
                protection.remove();
            }

            sender.sendMessage(Colors.Green + "Removed " + protections.size() + " protections from the region " + regionName);
        } else if (args[0].equals("protectregion")) {
            // The owner to assign to the protections
            String ownerName = "LWCWorldGuard";

            // the number of blocks that were registered
            int registered = 0;

            for (int x = minBlockX; x <= maxBlockX; x++) {
                for (int y = minBlockY; y <= maxBlockY; y++) {
                    for (int z = minBlockZ; z <= maxBlockZ; z++) {
                        // Get the block at that location
                        Block block = world.getBlockAt(x, y, z);

                        // Ensure it's protectable
                        if (!lwc.isProtectable(block)) {
                            continue;
                        }

                        // Check if it's already protected
                        if (lwc.findProtection(block.getLocation()) != null) {
                            continue;
                        }

                        // Protect it!
                        lwc.getPhysicalDatabase().registerProtection(block.getTypeId(), Protection.Type.PRIVATE, world.getName(),
                                ownerName, "", x, y, z);
                        registered ++;
                    }
                }
            }

            sender.sendMessage("Registered " + registered + " blocks in the region " + regionName);
            sender.sendMessage("Currently, the owner of these protections is \"" + ownerName + "\". To change this to someone else, run:");
            sender.sendMessage("/lwc admin updateprotections set owner = 'NewOwner' where owner = '" + ownerName + "'");
        }
    }


    @Override
    public void onAccessRequest(LWCAccessEvent event) {
        if (event.getAccess() != Permission.Access.NONE) {
            // Player already has access.
            return;
        }

        // WorldGuard must be running and LWC must be configured to interface with it.
        if (worldGuard == null) {
            return;
        }
        if (!configuration.getBoolean("worldguard.enabled", false)) {
            return;
        }
        if (!configuration.getBoolean("worldguard.allowRegionPermissions", true)) {
            return;
        }

        Protection protection = event.getProtection();
        GlobalRegionManager globalRegionManager = worldGuard.getGlobalRegionManager();
        LocalPlayer wgPlayer = worldGuard.wrapPlayer(event.getPlayer());
        for (Permission permission : protection.getPermissions()) {
            if (permission.getType() != Permission.Type.REGION) {
                continue;
            }
            String regionName = permission.getName();
            if (regionName.equalsIgnoreCase("#this")) {
                // Handle the special value which tells us to not actually look up a region but
                // check just the player's WG build permissions on the block. It may be in multiple
                // regions or none; we don't care here. That's WorldGuard's domain.
                if (!globalRegionManager.canBuild(event.getPlayer(), protection.getBlock())) {
                    continue;
                }
            } else if (regionName.startsWith("#")) {
                // Silently disallow looking up regions by index, a newer WG feature.
                // Iterating through potentially thousands of regions each time we check a block's
                // ACL is not a good idea. It would be cleaner to use regionManager.getRegionExact()
                // below, but that would break compatibility with older versions of WG.
                continue;
            } else {
                // Region name specified, go look it up
                World world;
                int c = regionName.indexOf(':');
                if (c < 0) {
                    // No world specified in ACL. Use the block's world.
                    world = protection.getBlock().getWorld();
                } else {
                    // World specified. Partition the string and look up the world.
                    String worldName = regionName.substring(c + 1);
                    world = event.getLWC().getPlugin().getServer().getWorld(worldName);
                    regionName = regionName.substring(0, c);
                }
                if (world == null) {
                    continue;
                }
                RegionManager regionManager = globalRegionManager.get(world);
                if (regionManager == null) {
                    continue;
                }
                ProtectedRegion region = regionManager.getRegion(regionName);
                if (region == null) {
                    continue;
                }
                // Check the region (and its parents) to see if the player is a member (or an owner).
                if (!region.isMember(wgPlayer)) {
                    continue;
                }
            }
            // We match the region, so bump up our access level. Whether we get PLAYER access or
            // ADMIN access depends solely on the LWC permission entry. (I.e., WG owner does not
            // imply LWC admin.)
            if (permission.getAccess().ordinal() > event.getAccess().ordinal()) {
                event.setAccess(permission.getAccess());
                if (event.getAccess().ordinal() >= Permission.Access.ADMIN.ordinal()) {
                    return;
                }
                // else we just have PLAYER access. Keep looking; maybe we match another region
                // that grants us ADMIN.
            }
        }
    }

    @Override
    public void onRegisterProtection(LWCProtectionRegisterEvent event) {
        if (worldGuard == null) {
            return;
        }

        if (!configuration.getBoolean("worldguard.enabled", false)) {
            return;
        }

        LWC lwc = event.getLWC();
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Load the region manager for the world
        GlobalRegionManager globalRegionManager = worldGuard.getGlobalRegionManager();
        RegionManager regionManager = globalRegionManager.get(block.getWorld());

        // Are we enforcing building?
        if (configuration.getBoolean("worldguard.requireBuildRights", true)) {
            if (!globalRegionManager.canBuild(player, block)) {
                lwc.sendLocale(player, "lwc.worldguard.needbuildrights");
                event.setCancelled(true);
                return;
            }
        }

        // Create a vector for the region
        Vector vector = BukkitUtil.toVector(block);

        // Load the regions the block encompasses
        List<String> regions = regionManager.getApplicableRegionsIDs(vector);

        // Are they not in a region, and it's blocked there?
        if (regions.size() == 0) {
            if (!configuration.getBoolean("worldguard.allowProtectionsOutsideRegions", true)) {
                lwc.sendLocale(player, "lwc.worldguard.notallowed");
                event.setCancelled(true);
            }
        } else {
            // check each region
            for (String region : regions) {
                // Should we deny them?
                // we don't need to explicitly call isRegionAllowed because isRegionBlacklisted checks that as well
                if (isRegionBlacklisted(region)) {
                    lwc.sendLocale(player, "lwc.worldguard.blacklisted");
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    /**
     * Check if a region is blacklisted
     *
     * @param region
     * @return
     */
    private boolean isRegionBlacklisted(String region) {
        if (!isRegionAllowed(region)) {
            return true;
        }

        List<String> blacklistedRegions = configuration.getStringList("worldguard.blacklistedRegions", new ArrayList<String>());
        return blacklistedRegions.contains("*") || blacklistedRegions.contains(region);
    }

    /**
     * Check if a region is allowed to be built in
     *
     * @param region
     * @return
     */
    private boolean isRegionAllowed(String region) {
        List<String> allowedRegions = configuration.getStringList("worldguard.regions", new ArrayList<String>());
        return allowedRegions.contains("*") || allowedRegions.contains(region);
    }

    /**
     * Set a config value in the configuration
     *
     * @param path
     * @param value
     */
    public void set(String path, Object value) {
        configuration.setProperty(path, value);
    }

    /**
     * Save the configuration
     */
    public boolean save() {
        return configuration.save();
    }

}
