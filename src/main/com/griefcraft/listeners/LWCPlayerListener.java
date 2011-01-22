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

package com.griefcraft.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;

import com.griefcraft.commands.ICommand;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.nijikokun.bukkit.Permissions.Permissions;

public class LWCPlayerListener extends PlayerListener {

	/**
	 * The plugin instance
	 */
	private LWCPlugin plugin;
	
	public LWCPlayerListener(LWCPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onPlayerCommand(PlayerChatEvent event) {
		if(event.isCancelled()) {
			return;
		}
		
		LWC lwc = plugin.getLWC();
		Player player = event.getPlayer();

		if(lwc.getPermissions() != null && !Permissions.Security.permission(player, "lwc.protect")) {
			return;
		}
		
		String[] split = event.getMessage().split(" ");
		String command = split[0].substring(1);
		String subCommand = "";
		String[] args = split.length > 1 ? new String[split.length - 1] : new String[0];

		/* Calculate the arguments used internally */
		if (split.length > 1) {
			for (int i = 1; i < split.length; i++) {
				split[i] = split[i].trim();

				if (split[i].isEmpty()) {
					continue;
				}

				args[i - 1] = split[i];
				subCommand += split[i] + " ";
			}
		}
		
		if(command.equals("cpublic")) {
			onPlayerCommand(new PlayerChatEvent(Type.PLAYER_COMMAND, player, "/lwc -c public"));
			return;
		}
		else if(command.equals("cpassword")) {
			onPlayerCommand(new PlayerChatEvent(Type.PLAYER_COMMAND, player, "/lwc -c password " + subCommand));
			return;
		}
		else if(command.equals("cprivate")) {
			onPlayerCommand(new PlayerChatEvent(Type.PLAYER_COMMAND, player, "/lwc -c private"));
			return;
		}
		else if(command.equals("cinfo")) {
			onPlayerCommand(new PlayerChatEvent(Type.PLAYER_COMMAND, player, "/lwc -i"));
			return;
		}
		else if(command.equals("cunlock")) {
			onPlayerCommand(new PlayerChatEvent(Type.PLAYER_COMMAND, player, "/lwc -u " + subCommand));
			return;
		}
		else if(command.equals("cremove")) {
			onPlayerCommand(new PlayerChatEvent(Type.PLAYER_COMMAND, player, "/lwc -r protection"));
			return;
		}
		
		// TODO: check if they can use the command ??
		/* if (!player.canUseCommand(split[0])) {
			return;
		} */

		if (!"lwc".equalsIgnoreCase(command)) {
			return;
		}

		if (args.length == 0) {
			lwc.sendFullHelp(player);
			return;
		}

		for (ICommand cmd : lwc.getCommands()) {
			if (!cmd.validate(lwc, player, args)) {
				continue;
			}

			cmd.execute(lwc, player, args);
			event.setCancelled(true);
		}
	}
	
	/**
	 * Player dcd, clear them from memory if they're in it
	 */
	@Override
	public void onPlayerQuit(PlayerEvent event) {
		LWC lwc = plugin.getLWC();
		String player = event.getPlayer().getName();
		
		lwc.getMemoryDatabase().unregisterPlayer(player);
		lwc.getMemoryDatabase().unregisterUnlock(player);
		lwc.getMemoryDatabase().unregisterChest(player);
		lwc.getMemoryDatabase().unregisterAllActions(player);
	}
	
}
