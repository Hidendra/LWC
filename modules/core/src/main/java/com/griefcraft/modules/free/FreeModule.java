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
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreeModule extends JavaModule {

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("free")) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        Player player = event.getPlayer();
        event.setResult(Result.CANCEL);

        if (lwc.hasAdminPermission(player, "lwc.admin.remove") || protection.getOwner().equals(player.getName())) {
            LWCProtectionDestroyEvent evt = new LWCProtectionDestroyEvent(player, protection, true, true);
            lwc.getModuleLoader().dispatchEvent(evt);

            if(!evt.isCancelled()) {
                protection.remove();
                lwc.sendLocale(player, "protection.interact.remove.finalize", "block", LWC.materialToString(protection.getBlockId()));
                lwc.removeModes(player);
            }
        } else {
            lwc.sendLocale(player, "protection.interact.error.notowner", "block", LWC.materialToString(protection.getBlockId()));
            lwc.removeModes(player);
        }

        return;
    }

    @Override
    public void onBlockInteract(LWCBlockInteractEvent event) {
        if (!event.hasAction("free")) {
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

        if (!event.hasFlag("r", "remove")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if (!(sender instanceof Player)) {
            return;
        }

        event.setCancelled(true);

        if (!lwc.hasPlayerPermission(sender, "lwc.remove")) {
            lwc.sendLocale(sender, "protection.accessdenied");
            return;
        }

        if (args.length < 1) {
            lwc.sendSimpleUsage(sender, "/lwc -r <protection|modes>");
            return;
        }

        String type = args[0].toLowerCase();
        Player player = (Player) sender;

        if (type.equals("protection") || type.equals("chest") || type.equals("furnace") || type.equals("dispenser")) {
            if (lwc.getMemoryDatabase().hasPendingChest(player.getName())) {
                lwc.sendLocale(sender, "protection.general.pending");
                return;
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

        return;
    }

}
