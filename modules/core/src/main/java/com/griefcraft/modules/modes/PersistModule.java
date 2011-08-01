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

public class PersistModule extends JavaModule {

    @Override
    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
        if (!StringUtils.hasFlag(command, "p") && !StringUtils.hasFlag(command, "mode")) {
            return DEFAULT;
        }

        if (args.length == 0) {
            lwc.sendSimpleUsage(sender, "/lwc mode <mode>");
            return CANCEL;
        }

        String mode = args[0];
        Player player = (Player) sender;

        if (!mode.equalsIgnoreCase("persist")) {
            return DEFAULT;
        }

        if (!lwc.isModeWhitelisted(player, mode)) {
            if (!lwc.isAdmin(sender) && !lwc.isModeEnabled(mode)) {
                lwc.sendLocale(player, "protection.modes.disabled");
                return CANCEL;
            }
        }

        List<String> modes = lwc.getMemoryDatabase().getModes(player.getName());

        if (!modes.contains(mode)) {
            lwc.getMemoryDatabase().registerMode(player.getName(), mode);
            lwc.sendLocale(player, "protection.modes.persist.finalize");
        } else {
            lwc.getMemoryDatabase().unregisterMode(player.getName(), mode);
            lwc.sendLocale(player, "protection.modes.persist.off");
        }


        return CANCEL;
    }

}
