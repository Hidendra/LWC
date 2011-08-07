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
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class JavaModule implements Module {

    /**
     * Allow the event to occur (e.g allow the redstone, allow a protection destruction, and so on)
     */
    public final static Result ALLOW = Result.ALLOW;

    /**
     * Cancel the event from happening (e.g disallow protection interaction, disallow protection registration)
     */
    public final static Result CANCEL = Result.CANCEL;

    /**
     * The default result returned by events
     */
    public final static Result DEFAULT = Result.DEFAULT;

    public void load(LWC lwc) {
    }

    public Result canAccessProtection(LWC lwc, Player player, Protection protection) {
        return DEFAULT;
    }

    public Result canAdminProtection(LWC lwc, Player player, Protection protection) {
        return DEFAULT;
    }

    public Result onDropItem(LWC lwc, Player player, Item item, ItemStack itemStack) {
        return DEFAULT;
    }

    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
        return DEFAULT;
    }

    public Result onRedstone(LWC lwc, Protection protection, Block block, int current) {
        return DEFAULT;
    }

    public Result onDestroyProtection(LWC lwc, Player player, Protection protection, Block block, boolean canAccess, boolean canAdmin) {
        return DEFAULT;
    }

    public Result onProtectionInteract(LWC lwc, Player player, Protection protection, List<String> actions, boolean canAccess, boolean canAdmin) {
        return DEFAULT;
    }

    public Result onBlockInteract(LWC lwc, Player player, Block block, List<String> actions) {
        return DEFAULT;
    }

    public Result onRegisterProtection(LWC lwc, Player player, Block block) {
        return DEFAULT;
    }

    public void onPostRegistration(LWC lwc, Protection protection) {
    	
    }

    public void onPostRemoval(LWC lwc, Protection protection) {
        
    }

    public Result onSendLocale(LWC lwc, Player player, String locale) {
        return DEFAULT;
    }

}
