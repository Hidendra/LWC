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

package com.griefcraft.lwc;

import com.griefcraft.bukkit.LWCSpoutPlugin;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.Colors;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.gui.PopupScreen;
import org.getspout.spoutapi.player.SpoutPlayer;

public class ManagementModule extends JavaModule {

    /**
     * The plugin object
     */
    private LWCSpoutPlugin plugin;

    public ManagementModule(LWCSpoutPlugin plugin) {
        this.plugin = plugin;
    }

    class ManagementPopup extends LWCPopupScreen {

        public ManagementPopup() {
            
            
            ManagementModule.this.plugin.bindLogo(this);
        }

        @Override
        public void onButtonClicked(ButtonClickEvent event) {
            
        }

    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getSender() instanceof Player)) {
            return;
        }

        String command = event.getCommand();

        if (!command.equalsIgnoreCase("spout")) {
            return;
        }

        // we're taking over this ship from here
        SpoutPlayer player = SpoutManager.getPlayer((Player) event.getSender());
        event.setCancelled(true);

        if (!player.isSpoutCraftEnabled()) {
            player.sendMessage(Colors.Red + "Spout client required.");
        }

        // they have spout, so give them what they want
        PopupScreen managementPopup = new ManagementPopup();
        player.getMainScreen().attachPopupScreen(managementPopup);
    }

}
