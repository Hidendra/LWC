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

package com.griefcraft.modules.worldguard;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.util.Colors;
import com.griefcraft.util.config.Configuration;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class WorldGuardModule extends JavaModule {

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

        if (!args[0].equals("purgeregion")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        // check for worldguard
        if (worldGuard == null) {
            sender.sendMessage(Colors.Red + "WorldGuard is not enabled.");
            return;
        }

        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc admin purgeregion <RegionName> [World]");
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
        World world = null;

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
            sender.sendMessage(Colors.Red + "Region not found. If you region is in a different world than you, please use: /lwc admin purgeregion " + regionName + " WorldName");
            return;
        }

        BlockVector minimum = region.getMinimumPoint();
        BlockVector maximum = region.getMaximumPoint();

        // get all of the protections inside of the region
        List<Protection> protections = lwc.getPhysicalDatabase().loadProtections(world.getName(), minimum.getBlockX(), maximum.getBlockX(), 0, 128, minimum.getBlockZ(), maximum.getBlockZ());

        // remove all of them
        for (Protection protection : protections) {
            protection.remove();
        }

        sender.sendMessage(Colors.Green + "Removed " + protections.size() + " protections from the region " + regionName);
    }

    @Override
    public void onRegisterProtection(LWCProtectionRegisterEvent event) {
        if (worldGuard == null) {
            return;
        }

        if (!configuration.getBoolean("worldguard.enabled", false)) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();

        try {
            // Get the region manager
            GlobalRegionManager regions = worldGuard.getGlobalRegionManager();
            RegionManager regionManager = regions.get(player.getWorld());

            // Reflect into BukkitUtil.toVector ...
            Class<?> bukkitUtil = worldGuard.getClass().getClassLoader().loadClass("com.sk89q.worldguard.bukkit.BukkitUtil");
            Method toVector = bukkitUtil.getMethod("toVector", Block.class);
            Vector blockVector = (Vector) toVector.invoke(null, block);

            // Now let's get the list of regions at the block we're clicking
            List<String> regionSet = regionManager.getApplicableRegionsIDs(blockVector);
            List<String> allowedRegions = configuration.getStringList("worldguard.regions", new ArrayList<String>());

            boolean deny = true;

            // check for *
            if (allowedRegions.contains("*")) {
                if (regionSet.size() > 0) {
                    // Yeah!
                    return;
                }
            }

            // if they aren't in any of the regions we need to deny them
            for (String region : regionSet) {
                if (allowedRegions.contains(region)) {
                    deny = false;
                    break;
                }
            }

            if (deny) {
                player.sendMessage(Colors.Red + "You cannot protect that " + LWC.materialToString(block) + " outside of WorldGuard regions");
                event.setCancelled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
