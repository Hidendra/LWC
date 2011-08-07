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

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * This interface defines methods that modules may implement
 */
public interface Module {

    // Results returned by methods
    public enum Result {
        ALLOW, CANCEL, DEFAULT
    }

    /**
     * Called when the module is loaded
     */
    public void load(LWC lwc);

    /**
     * See if a player can access a protection
     *
     * @param lwc
     * @param player
     * @param protection
     * @return
     */
    public Result canAccessProtection(LWC lwc, Player player, Protection protection);

    /**
     * See if a player can administrate a protection (i.e modify it)
     *
     * @param lwc
     * @param player
     * @param protection
     * @return
     */
    public Result canAdminProtection(LWC lwc, Player player, Protection protection);

    /**
     * Called when a player drops an item
     *
     * @param lwc
     * @param player
     * @param item
     * @param itemStack
     * @return
     */
    public Result onDropItem(LWC lwc, Player player, Item item, ItemStack itemStack);

    /**
     * Player or console command
     *
     * @param lwc
     * @param sender
     * @param command does not include "lwc", eg. /lwc info = "info"
     * @param args
     * @return
     */
    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args);

    /**
     * Called when redstone is passed to a valid protection
     *
     * @param lwc
     * @param protection
     * @param block
     * @param current    the old current
     * @return
     */
    public Result onRedstone(LWC lwc, Protection protection, Block block, int current);

    /**
     * Called when a protection is destroyed
     *
     * @param lwc
     * @param protection
     * @param block
     * @return
     */
    public Result onDestroyProtection(LWC lwc, Player player, Protection protection, Block block, boolean canAccess, boolean canAdmin);

    /**
     * Called when a player left interacts with a valid protection
     *
     * @param lwc
     * @param player
     * @param protection
     * @param canAccess
     * @return
     */
    public Result onProtectionInteract(LWC lwc, Player player, Protection protection, List<String> actions, boolean canAccess, boolean canAdmin);

    /**
     * Called when a player interacts with a block
     *
     * @param lwc
     * @param player
     * @param block
     * @param actions
     * @return
     */
    public Result onBlockInteract(LWC lwc, Player player, Block block, List<String> actions);

    /**
     * Called just before a protection registration is finalized, after all post-checks are passed.
     *
     * @param lwc
     * @param player
     * @param block
     * @return
     */
    public Result onRegisterProtection(LWC lwc, Player player, Block block);

    /**
     * Called after a protection is registered
     *
     * @param lwc
     * @param protection
     */
    public void onPostRegistration(LWC lwc, Protection protection);

    /**
     * Called after a protection is removed (at this point, the protection is IMMUTABLE.)
     * 
     * @param lwc
     * @param protection
     */
    public void onPostRemoval(LWC lwc, Protection protection);

    /**
     * Called when a localized message is sent to a player (e.g lwc.accessdenied)
     * 
     * @param lwc
     * @param player
     * @param locale
     * @since LWC 3.40
     */
    public Result onSendLocale(LWC lwc, Player player, String locale);

}
