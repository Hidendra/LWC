/*
 * Copyright 2011 Tyler Blair. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package com.griefcraft.lwc;

import com.griefcraft.bukkit.LWCSpoutPlugin;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.Colors;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.Label;
import org.getspout.spoutapi.gui.PopupScreen;
import org.getspout.spoutapi.gui.WidgetAnchor;
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

        public ManagementPopup(SpoutPlayer player) {
            Label label = new GenericLabel("LWC");
            label.setAnchor(WidgetAnchor.TOP_CENTER);
            label.setAlign(WidgetAnchor.TOP_CENTER);
            label.shiftYPos(10);

            attachWidget(ManagementModule.this.plugin, label);
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
            player.sendMessage(Colors.Red + "Spout client is required.");
            return;
        }

        // they have spout, so give them what they want
        PopupScreen managementPopup = new ManagementPopup(player);
        player.getMainScreen().attachPopupScreen(managementPopup);
    }

}
