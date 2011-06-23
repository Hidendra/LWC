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

package com.griefcraft.modules.menu;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MenuModule extends JavaModule {

    @Override
    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
        if (!StringUtils.hasFlag(command, "menu")) {
            return DEFAULT;
        }

        if (args.length < 1) {
            lwc.sendSimpleUsage(sender, "/lwc menu <basic|advanced>");
            return CANCEL;
        }

        String newStyle = args[0].toLowerCase();

        if (!newStyle.equals("basic") && !newStyle.equals("advanced")) {
            sender.sendMessage(Colors.Red + "Invalid style.");
            return CANCEL;
        }

        Player player = (Player) sender;

        lwc.getPhysicalDatabase().setMenuStyle(player.getName(), newStyle);
        lwc.sendLocale(player, "protection.menu.finalize", "style", newStyle);
        return CANCEL;
    }

}
