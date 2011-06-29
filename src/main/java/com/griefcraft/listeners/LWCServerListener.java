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

package com.griefcraft.listeners;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.griefcraft.lwc.LWCPlugin;

public class LWCServerListener extends ServerListener {

    private LWCPlugin plugin;

    public LWCServerListener(LWCPlugin plugin) {
        this.plugin = plugin;
    }

    // remove any modules used by the plugin
    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        Plugin disabled = event.getPlugin();

        plugin.getLWC().getModuleLoader().removeModules(disabled);
    }

}
