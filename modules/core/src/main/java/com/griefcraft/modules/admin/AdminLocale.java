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
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.StringUtils;
import org.bukkit.command.CommandSender;

public class AdminLocale extends JavaModule {

    @Override
    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
        if (!StringUtils.hasFlag(command, "a") && !StringUtils.hasFlag(command, "admin")) {
            return DEFAULT;
        }

        if (!args[0].equals("locale")) {
            return DEFAULT;
        }
        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc admin locale <key> [args]");
            return CANCEL;
        }

        String locale = args[1];
        String[] localeArgs = new String[0];

        if (args.length > 3) {
            localeArgs = StringUtils.join(args, 3).split(" ");
        }

        if (localeArgs.length > 0) {
            lwc.sendLocale(sender, locale, (Object[]) localeArgs);
        } else {
            lwc.sendLocale(sender, locale);
        }

        return CANCEL;
    }

}