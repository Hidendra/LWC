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

package com.griefcraft.bukkit;

import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.iConomy.iConomy;

public class iConomyServerListener extends ServerListener {

	private BukkitPlugin plugin;
	
	public iConomyServerListener(BukkitPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onPluginEnable(PluginEnableEvent event) {
		if(plugin.isInitialized()) {
			return;
		}
		
		Plugin iConomy = plugin.getServer().getPluginManager().getPlugin("iConomy");
		Plugin lwcPlugin = plugin.getServer().getPluginManager().getPlugin("LWC");
		
		if(iConomy != null && lwcPlugin != null) {
			if(iConomy.isEnabled() && lwcPlugin.isEnabled()) {
				iConomy money = (iConomy) iConomy;
				LWC lwc = ((LWCPlugin) lwcPlugin).getLWC();
				
				plugin.init(lwc, money);
			}
		}
	}
	
}
