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

package com.griefcraft.modules.modes;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class NoSpamModule extends JavaModule {

    @Override
    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
        if (!StringUtils.hasFlag(command, "p") && !StringUtils.hasFlag(command, "mode")) {
            return DEFAULT;
        }

        Player player = (Player) sender;
        String mode = args[0].toLowerCase();

        if(!mode.equals("nospam")) {
            return DEFAULT;
        }

        List<String> modes = lwc.getMemoryDatabase().getModes(player.getName());

        if (!modes.contains(mode)) {
            lwc.getMemoryDatabase().registerMode(player.getName(), mode);
            lwc.sendLocale(player, "protection.modes.nospam.finalize");
        } else {
            lwc.getMemoryDatabase().unregisterMode(player.getName(), mode);
            lwc.sendLocale(player, "protection.modes.nospam.off");
        }


        return CANCEL;
    }

    @Override
    public Result onSendLocale(LWC lwc, Player player, String locale) {
        List<String> modes = lwc.getMemoryDatabase().getModes(player.getName());

        // they don't intrigue us
        if(!modes.contains("nospam")) {
            return DEFAULT;
        }

        // hide all of the creation messages
        if(locale.endsWith("create.finalize")) {
            return CANCEL;
        }

        return DEFAULT;
    }

}
