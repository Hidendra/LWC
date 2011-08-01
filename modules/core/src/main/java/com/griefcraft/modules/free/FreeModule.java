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

package com.griefcraft.modules.free;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class FreeModule extends JavaModule {

    @Override
    public Result onProtectionInteract(LWC lwc, Player player, Protection protection, List<String> actions, boolean canAccess, boolean canAdmin) {
        if (!actions.contains("free")) {
            return DEFAULT;
        }

        if (lwc.hasAdminPermission(player, "lwc.admin.remove") || protection.getOwner().equals(player.getName())) {
            protection.remove();
            lwc.sendLocale(player, "protection.interact.remove.finalize", "block", LWC.materialToString(protection.getBlockId()));
            lwc.removeModes(player);
        } else {
            lwc.sendLocale(player, "protection.interact.error.notowner", "block", LWC.materialToString(protection.getBlockId()));
            lwc.removeModes(player);
        }

        return CANCEL;
    }

    @Override
    public Result onBlockInteract(LWC lwc, Player player, Block block, List<String> actions) {
        if (!actions.contains("free")) {
            return DEFAULT;
        }

        lwc.sendLocale(player, "protection.interact.error.notregistered", "block", LWC.materialToString(block));
        lwc.removeModes(player);
        return CANCEL;
    }

    @Override
    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
        if (!StringUtils.hasFlag(command, "r") && !StringUtils.hasFlag(command, "remove")) {
            return DEFAULT;
        }

        if (!lwc.hasPlayerPermission(sender, "lwc.remove")) {
            lwc.sendLocale(sender, "protection.accessdenied");
            return CANCEL;
        }

        if (args.length < 1) {
            lwc.sendSimpleUsage(sender, "/lwc -r <protection|modes>");
            return CANCEL;
        }

        if (!(sender instanceof Player)) {
            return DEFAULT;
        }

        String type = args[0].toLowerCase();
        Player player = (Player) sender;

        if (type.equals("protection") || type.equals("chest") || type.equals("furnace") || type.equals("dispenser")) {
            if (lwc.getMemoryDatabase().hasPendingChest(player.getName())) {
                lwc.sendLocale(sender, "protection.general.pending");
                return CANCEL;
            }

            lwc.getMemoryDatabase().unregisterAllActions(player.getName());
            lwc.getMemoryDatabase().registerAction("free", player.getName());
            lwc.sendLocale(sender, "protection.remove.protection.finalize");
        } else if (type.equals("modes")) {
            lwc.getMemoryDatabase().unregisterAllModes(player.getName());
            lwc.sendLocale(sender, "protection.remove.modes.finalize");
        } else {
            lwc.sendSimpleUsage(sender, "/lwc -r <protection|modes>");
        }

        return CANCEL;
    }

}
