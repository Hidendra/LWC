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

package com.griefcraft;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.command.ColouredConsoleSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.util.Colors;

public class BukkitPlugin extends JavaPlugin {

	/**
	 * The LWCPlugin instance
	 */
	private LWCPlugin lwcPlugin;
	
	/**
	 * The LWC instance
	 */
	private LWC lwc;
	
	/**
	 * The sender to send messages to
	 */
	private CommandSender sender;
	
	@Override
	public void onEnable() {
		sender = new ColouredConsoleSender((CraftServer) getServer());
		Plugin plugin = getServer().getPluginManager().getPlugin("LWC");

		if(plugin != null) {
			if(!plugin.isEnabled()) {
				getServer().getPluginManager().enablePlugin(plugin);
			}

			lwcPlugin = (LWCPlugin) plugin;
			lwc = lwcPlugin.getLWC();
			
			log("Loaded LWC version: " + Colors.Green + lwc.getVersion());
		} else {
			log("LWC is not loaded!");
		}
	}

	@Override
	public void onDisable() {
		log("Disabling..");
		lwcPlugin = null;
		lwc = null;
	}

	/**
	 * Log a string to stdout
	 * 
	 * @param str
	 */
	private void log(String str) {
		sender.sendMessage(Colors.Yellow + "[LWC Tests]\t" + Colors.White + str);
	}
	
}
