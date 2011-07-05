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

import org.bukkit.command.CommandSender;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;
import com.griefcraft.util.Updater;

public class AdminVersion extends JavaModule {

    @Override
    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
        if (!StringUtils.hasFlag(command, "a") && !StringUtils.hasFlag(command, "admin")) {
            return DEFAULT;
        }

        if (!args[0].equals("version")) {
            return DEFAULT;
        }

        Updater updater = lwc.getPlugin().getUpdater();
        // force a reload of the latest versions
        updater.loadVersions(false);
        String pluginColor = Colors.Green;
        double currPluginVersion = LWCInfo.VERSION;
        double latestPluginVersion = updater.getLatestPluginVersion();

        if (latestPluginVersion > currPluginVersion) {
            pluginColor = Colors.Red;
        }
        
        String full = LWCInfo.FULL_VERSION;

        lwc.sendLocale(sender, "protection.admin.version.finalize", "plugin_color", pluginColor, "plugin_version", full, "latest_plugin", latestPluginVersion);
        return CANCEL;
    }

}
