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

package com.griefcraft.scripting;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;

/**
 * This interface defines methods that modules may implement
 * 
 * Please note that for python modules that subclass this, you don't have to
 * implement every method!! Just the methods you want.
 */
public interface Module {

	// Results returned by methods
	public enum Result {
		ALLOW, CANCEL, DEFAULT;
	}

	/**
	 * @return the canonical name from the module (eg. the package, for example com.griefcraft.modules.YOURMODULE)
	 */
	public String getName();
	
	/**
	 * @return
	 */
	public int getVersion();
	
	/**
	 * @return the author of the module
	 */
	public String getAuthor();
	
	/**
	 * @return the module's descriptiopn
	 */
	public String getDescription();
	
	/**
	 * Called when the module is loaded
	 */
	public void load();
	
	/**
	 * Called when the module is unloaded
	 * Includes being called when being reloaded, or a server stop
	 */
	// public void unload();
	
	/**
	 * Player or console command
	 * 
	 * @param lwc
	 * @param sender
	 * @param command does not include "lwc", eg. /lwc info = "info"
	 * @param args
	 * @return true to mark the command as "used"
	 */
	public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args);
	
	/**
	 * Called when redstone is passed to a valid protection
	 * 
	 * @param lwc
	 * @param protection
	 * @param block
	 * @param current the old current
	 * @return true to revert the current
	 */
	public Result onRedstone(LWC lwc, Protection protection, Block block, int current);
	
	/**
	 * Called when a protection is destroyed
	 * 
	 * @param lwc
	 * @param protection
	 * @param block
	 * @return true to prevent destruction
	 */
	public Result onDestroyProtection(LWC lwc, Player player, Protection protection, Block block, boolean canAccess, boolean canAdmin);
	
	/**
	 * Called when a player left interacts with a valid protection
	 * 
	 * @param lwc
	 * @param player
	 * @param protection
	 * @param canAccess
	 * @return true to cancel the interaction with the protection (eg. opening a chest)
	 */
	public Result onProtectionInteract(LWC lwc, Player player, Protection protection, List<String> actions, boolean canAccess, boolean canAdmin);
	
	/**
	 * Called when a player interacts with a block
	 * 
	 * @param lwc
	 * @param player
	 * @param protection
	 * @param canAccess
	 * @return true to cancel the interaction with the block (eg. opening a chest)
	 */
	public Result onBlockInteract(LWC lwc, Player player, Block block, List<String> actions);
	
	/**
	 * Called just before a protection registration is finalized, after all post-checks are passed.
	 * 
	 * @param lwc
	 * @param player
	 * @param block
	 * @return true to cancel the registration
	 * TODO
	 */
	public Result onRegisterProtection(LWC lwc, Player player, Block block);
	
}
