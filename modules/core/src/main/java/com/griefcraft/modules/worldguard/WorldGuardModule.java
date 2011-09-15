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

package com.griefcraft.modules.worldguard;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.util.Colors;
import com.griefcraft.util.config.Configuration;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.block.Block;
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
