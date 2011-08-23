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

package com.griefcraft.modules.flag;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Action;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.util.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BaseFlagModule extends JavaModule {

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (!event.hasAction("flag")) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        Player player = event.getPlayer();

        Action action = lwc.getMemoryDatabase().getAction("flag", player.getName());
        String data = action.getData();
        event.setResult(Result.CANCEL);

        if (!event.canAdmin()) {
            lwc.sendLocale(player, "protection.accessdenied");
            return;
        }

        boolean shouldAdd = data.substring(0, 1).equals("+");
        String flagName = data.substring(1);

        Protection.Flag flag = null;

        for (Protection.Flag tmp : Protection.Flag.values()) {
            if (tmp.toString().equalsIgnoreCase(flagName)) {
                flag = tmp;
                break;
            }
        }

        if (flag == null) {
            lwc.sendLocale(player, "protection.internalerror", "id", "flg");
            return;
        }

        if (shouldAdd) {
            protection.addFlag(flag);
            lwc.sendLocale(player, "protection.interact.flag.add", "flag", StringUtils.capitalizeFirstLetter(flagName));
        } else {
            protection.removeFlag(flag);
            lwc.sendLocale(player, "protection.interact.flag.remove", "flag", StringUtils.capitalizeFirstLetter(flagName));
        }

        protection.saveNow();
        lwc.removeModes(player);
        return;
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (!event.hasFlag("f", "flag")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();
        event.setCancelled(true);

        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc flag <flag> <on/off>");

            boolean denyRedstone = lwc.getConfiguration().getBoolean("protections.denyRedstone", false);
            String redstone = denyRedstone ? lwc.getLocale("help.flags.redstone.allow") : lwc.getLocale("help.flags.redstone.deny");
            lwc.sendLocale(sender, "help.flags", "redstone", redstone);

            return;
        }

        Player player = (Player) sender;
        String flagName = args[0];
        String type = args[1].toLowerCase();
        String internalType; // + or -

        /**
         * Allow lwc.flag.?? (e.g lwc.flag.redstone) or optionally the umbrella node lwc.allflags
         */
        if (!lwc.hasPermission(sender, "lwc.flag." + flagName, "lwc.protect", "lwc.allflags")) {
            lwc.sendLocale(sender, "protection.accessdenied");
            return;
        }

        // verify the flag name
        boolean match = false;
        for (Protection.Flag flag : Protection.Flag.values()) {
            if (flag.toString().equalsIgnoreCase(flagName)) {
                match = true;
                flagName = flag.toString(); // get the case-correct name while we're there
                break;
            }
        }

        if (!match) {
            lwc.sendLocale(sender, "protection.flag.invalidflag", "flag", flagName);
            return;
        }

        if (type.equals("on") || type.equals("true") || type.equals("yes")) {
            internalType = "+";
        } else if (type.equals("off") || type.equals("false") || type.equals("no")) {
            internalType = "-";
        } else {
            lwc.sendLocale(sender, "protection.flag.invalidtype", "type", type);
            return;
        }

        lwc.getMemoryDatabase().unregisterAllActions(player.getName());
        lwc.getMemoryDatabase().registerAction("flag", player.getName(), internalType + flagName);
        lwc.sendLocale(sender, "protection.flag.finalize");

        return;
    }

}
