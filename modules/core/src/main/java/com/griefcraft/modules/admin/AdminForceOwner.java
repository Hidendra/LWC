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

package com.griefcraft.modules.admin;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Action;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminForceOwner extends JavaModule {

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("forceowner")) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        LWCPlayer player = lwc.wrapPlayer(event.getPlayer());

        Action action = player.getAction("forceowner");
        String newOwner = action.getData();

        protection.setOwner(newOwner);
        protection.save();

        lwc.sendLocale(player, "protection.interact.forceowner.finalize", "player", newOwner);
        lwc.removeModes(player);
        event.setResult(Result.CANCEL);

        return;
    }

    @Override
    public void onBlockInteract(LWCBlockInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("forceowner")) {
            return;
        }

        LWC lwc = event.getLWC();
        Player player = event.getPlayer();

        lwc.sendLocale(player, "protection.interact.error.notregistered", "block", LWC.materialToString(event.getBlock()));
        lwc.removeModes(player);
        event.setResult(Result.CANCEL);
        return;
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("a", "admin")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if (!args[0].equals("forceowner")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc admin forceowner <player>");
            return;
        }

        if (!(sender instanceof Player)) {
            lwc.sendLocale(sender, "protection.admin.noconsole");
            return;
        }

        LWCPlayer player = lwc.wrapPlayer(sender);
        String newOwner = args[1];

        Action action = new Action();
        action.setName("forceowner");
        action.setPlayer(player);
        action.setData(newOwner);
        player.addAction(action);

        lwc.sendLocale(sender, "protection.admin.forceowner.finalize", "player", newOwner);

        return;
    }

}