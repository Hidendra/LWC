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

package com.griefcraft.modules.modify;

import static com.griefcraft.util.StringUtils.join;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.AccessRight;
import com.griefcraft.model.Action;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;

public class ModifyModule extends JavaModule {

    @Override
    public Result onProtectionInteract(LWC lwc, Player player, Protection protection, List<String> actions, boolean canAccess, boolean canAdmin) {
        if (!actions.contains("modify")) {
            return DEFAULT;
        }

        if (lwc.canAdminProtection(player, protection)) {
            Action action = lwc.getMemoryDatabase().getAction("modify", player.getName());

            final String defaultEntities = action.getData();
            String[] entities = new String[0];

            if (defaultEntities.length() > 0) {
                entities = defaultEntities.split(" ");
            }

            lwc.removeModes(player);

            for (String rightsName : entities) {
                boolean remove = false;
                boolean isAdmin = false;
                int chestType = AccessRight.PLAYER;

                if (rightsName.startsWith("-")) {
                    remove = true;
                    rightsName = rightsName.substring(1);
                }

                if (rightsName.startsWith("@")) {
                    isAdmin = true;
                    rightsName = rightsName.substring(1);
                }

                if (rightsName.toLowerCase().startsWith("g:")) {
                    chestType = AccessRight.GROUP;
                    rightsName = rightsName.substring(2);
                }

                if (rightsName.toLowerCase().startsWith("l:")) {
                    chestType = AccessRight.LIST;
                    rightsName = rightsName.substring(2);
                }

                if (rightsName.toLowerCase().startsWith("list:")) {
                    chestType = AccessRight.LIST;
                    rightsName = rightsName.substring(5);
                }

                int chestID = protection.getId();
                String localeChild = AccessRight.typeToString(chestType).toLowerCase();

                if (!remove) {
                    lwc.getPhysicalDatabase().unregisterProtectionRights(chestID, rightsName);
                    lwc.getPhysicalDatabase().registerProtectionRights(chestID, rightsName, isAdmin ? 1 : 0, chestType);
                    lwc.sendLocale(player, "protection.interact.rights.register." + localeChild, "name", rightsName, "isadmin", isAdmin ? "[" + Colors.Red + "ADMIN" + Colors.Gold + "]" : "");
                } else {
                    lwc.getPhysicalDatabase().unregisterProtectionRights(chestID, rightsName);
                    lwc.sendLocale(player, "protection.interact.rights.remove." + localeChild, "name", rightsName, "isadmin", isAdmin ? "[" + Colors.Red + "ADMIN" + Colors.Gold + "]" : "");
                }

                protection.update();
            }
        } else {
            lwc.sendLocale(player, "protection.interact.error.notowner", "block", LWC.materialToString(protection.getBlockId()));
            lwc.removeModes(player);
        }

        return CANCEL;
    }

    @Override
    public Result onBlockInteract(LWC lwc, Player player, Block block, List<String> actions) {
        if (!actions.contains("modify")) {
            return DEFAULT;
        }

        lwc.sendLocale(player, "protection.interact.error.notregistered", "block", LWC.materialToString(block));
        lwc.removeModes(player);
        return CANCEL;
    }

    @Override
    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
        if (!StringUtils.hasFlag(command, "m") && !StringUtils.hasFlag(command, "modify")) {
            return DEFAULT;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Colors.Red + "Console not supported.");
            return CANCEL;
        }

        if (!lwc.hasPlayerPermission(sender, "lwc.modify")) {
            lwc.sendLocale(sender, "protection.accessdenied");
            return CANCEL;
        }

        if (args.length < 1) {
            lwc.sendLocale(sender, "help.modify");
            return CANCEL;
        }

        String full = join(args, 0);
        Player player = (Player) sender;

        lwc.getMemoryDatabase().unregisterAllActions(player.getName());
        lwc.getMemoryDatabase().registerAction("modify", player.getName(), full);
        lwc.sendLocale(sender, "protection.modify.finalize");
        return CANCEL;
    }

}
