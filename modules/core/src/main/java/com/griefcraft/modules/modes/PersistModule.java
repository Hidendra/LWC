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
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PersistModule extends JavaModule {

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (!event.hasFlag("p", "mode")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        Player player = (Player) sender;
        String mode = args[0].toLowerCase();

        if(!mode.equals("persist")) {
            return;
        }

        List<String> modes = lwc.getMemoryDatabase().getModes(player.getName());

        if (!modes.contains(mode)) {
            lwc.getMemoryDatabase().registerMode(player.getName(), mode);
            lwc.sendLocale(player, "protection.modes.persist.finalize");
        } else {
            lwc.getMemoryDatabase().unregisterMode(player.getName(), mode);
            lwc.sendLocale(player, "protection.modes.persist.off");
        }

        event.setCancelled(true);
        return;
    }

}
