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

import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;

public interface ICommand {

	/**
	 * @return the name of the command (i.e /lwc -a)
	 */
	public String getName();

	/**
	 * Execute a command if it validates
	 * 
	 * @param lwc
	 * @param player
	 * @param command
	 * @param args
	 */
	public void execute(LWC lwc, Player player, String[] args);

	/**
	 * Validate a command to check if it should be executed
	 * 
	 * @param lwc
	 * @param command
	 * @param args
	 * @return
	 */
	public boolean validate(LWC lwc, Player player, String[] args);

}
