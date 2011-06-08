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

package com.griefcraft.modules.info;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class InfoModule extends JavaModule {

    @Override
    public Result onProtectionInteract(LWC lwc, Player player, Protection protection, List<String> actions, boolean canAccess, boolean canAdmin) {
        if (!actions.contains("info")) {
            return DEFAULT;
        }

        lwc.sendLocale(player, "protection.interact.info.finalize", "type", lwc.getLocale(protection.typeToString().toLowerCase()), "owner", protection.getOwner(), "access", lwc.getLocale((canAccess ? "yes" : "no")));

        if (lwc.isAdmin(player)) {
            lwc.sendLocale(player, "protection.interact.info.raw", "raw", protection.toString());
        }

        lwc.removeModes(player);
        return CANCEL;
    }

    @Override
    public Result onBlockInteract(LWC lwc, Player player, Block block, List<String> actions) {
        if (!actions.contains("info")) {
            return DEFAULT;
        }

        lwc.sendLocale(player, "protection.interact.error.notregistered", "block", LWC.materialToString(block));
        lwc.removeModes(player);
        return CANCEL;
    }

    @Override
    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
        if (!StringUtils.hasFlag(command, "i") && !StringUtils.hasFlag(command, "info")) {
            return DEFAULT;
        }

        if (!lwc.hasPlayerPermission(sender, "lwc.info")) {
            lwc.sendLocale(sender, "protection.accessdenied");
            return CANCEL;
        }

        if (!(sender instanceof Player)) {
            return DEFAULT;
        }

        Player player = (Player) sender;
        String type = "info";

        if (args.length > 0) {
            type = args[0].toLowerCase();
        }

        if (type.equals("info")) {
            lwc.getMemoryDatabase().unregisterAllActions(player.getName());
            lwc.getMemoryDatabase().registerAction("info", player.getName());
            lwc.sendLocale(player, "protection.info.finalize");
        }

        return CANCEL;
    }

}
