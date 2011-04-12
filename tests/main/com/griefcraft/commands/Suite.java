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

package com.griefcraft.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.griefcraft.BukkitPlugin;
import com.griefcraft.tests.TestSuite;
import com.griefcraft.util.Colors;

public class Suite implements CommandExecutor {
	
	/**
	 * The BukkitPlugin instance
	 */
	private BukkitPlugin plugin;
	
	public Suite(BukkitPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
		if(args.length < 1) {
			sender.sendMessage("No arguments specified!");
			return true;
		}
		
		String action = args[0].toLowerCase();
		
		if(action.equals("run")) {
			if(args.length < 2) {
				sender.sendMessage("Usage: " + Colors.Red + "/suite start <name>");
				return true;
			}
			
			String suiteName = args[1];
			
			TestSuite testSuite = plugin.createTestSuite(suiteName, sender);
			
			if(testSuite != null) {
				testSuite.start();
			}
		}
		
		return true;
	}

}
