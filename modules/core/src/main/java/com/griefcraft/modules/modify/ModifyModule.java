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

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.AccessRight;
import com.griefcraft.model.Action;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.util.Colors;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.griefcraft.util.StringUtils.join;

public class ModifyModule extends JavaModule {

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("modify")) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        Player player = event.getPlayer();
        event.setResult(Result.CANCEL);

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
                int type = AccessRight.PLAYER;

                if (rightsName.startsWith("-")) {
                    remove = true;
                    rightsName = rightsName.substring(1);
                }

                if (rightsName.startsWith("@")) {
                    isAdmin = true;
                    rightsName = rightsName.substring(1);
                }

                if (rightsName.toLowerCase().startsWith("g:")) {
                    type = AccessRight.GROUP;
                    rightsName = rightsName.substring(2);
                }

                if (rightsName.toLowerCase().startsWith("l:")) {
                    type = AccessRight.LIST;
                    rightsName = rightsName.substring(2);
                }

                if (rightsName.toLowerCase().startsWith("list:")) {
                    type = AccessRight.LIST;
                    rightsName = rightsName.substring(5);
                }

                int protectionId = protection.getId();
                String localeChild = AccessRight.typeToString(type).toLowerCase();

                if (!remove) {
                    AccessRight accessRight = new AccessRight();
                    accessRight.setProtectionId(protectionId);
                    accessRight.setRights(isAdmin ? 1 : 0);
                    accessRight.setName(rightsName);
                    accessRight.setType(type);

                    // add it to the protection and queue it to be saved
                    protection.addAccessRight(accessRight);
                    protection.save();
                    
                    lwc.sendLocale(player, "protection.interact.rights.register." + localeChild, "name", rightsName, "isadmin", isAdmin ? "[" + Colors.Red + "ADMIN" + Colors.Gold + "]" : "");
                } else {
                    protection.removeAccessRightsMatching(rightsName, type);
                    protection.save();

                    lwc.sendLocale(player, "protection.interact.rights.remove." + localeChild, "name", rightsName, "isadmin", isAdmin ? "[" + Colors.Red + "ADMIN" + Colors.Gold + "]" : "");
                }
            }
        } else {
            lwc.sendLocale(player, "protection.interact.error.notowner", "block", LWC.materialToString(protection.getBlockId()));
            lwc.removeModes(player);
        }

        return;
    }

    @Override
    public void onBlockInteract(LWCBlockInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("modify")) {
            return;
        }

        LWC lwc = event.getLWC();
        Block block = event.getBlock();
        Player player = event.getPlayer();
        event.setResult(Result.CANCEL);

        lwc.sendLocale(player, "protection.interact.error.notregistered", "block", LWC.materialToString(block));
        lwc.removeModes(player);
        return;
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("m", "modify")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();
        event.setCancelled(true);

        if (!(sender instanceof Player)) {
            sender.sendMessage(Colors.Red + "Console not supported.");
            return;
        }

        if (!lwc.hasPlayerPermission(sender, "lwc.modify")) {
            lwc.sendLocale(sender, "protection.accessdenied");
            return;
        }

        if (args.length < 1) {
            lwc.sendLocale(sender, "help.modify");
            return;
        }

        String full = join(args, 0);
        Player player = (Player) sender;

        lwc.getMemoryDatabase().unregisterAllActions(player.getName());
        lwc.getMemoryDatabase().registerAction("modify", player.getName(), full);
        lwc.sendLocale(sender, "protection.modify.finalize");
        return;
    }

}
