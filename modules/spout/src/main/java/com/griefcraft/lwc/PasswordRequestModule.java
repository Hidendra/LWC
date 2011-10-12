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
import com.griefcraft.model.Action;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCSendLocaleEvent;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericTextField;
import org.getspout.spoutapi.gui.PopupScreen;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;

public class PasswordRequestModule extends JavaModule {

    /**
     * Our plugin instance
     */
    private LWCSpoutPlugin plugin;

    public PasswordRequestModule(LWCSpoutPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Sent to players when they open a password protected protection
     */
    class PasswordRequestPopup extends LWCPopupScreen {

        private GenericLabel label;
        private GenericLabel label2;
        private GenericTextField textField;
        private GenericButton unlock;
        private GenericButton cancel;

        public PasswordRequestPopup() {
            // Create our widgets
            label = new GenericLabel("You attempted to interact with a Password-protected object");
            label.setTextColor(new Color(1.0f, 0, 0));
            label.setAlign(WidgetAnchor.TOP_CENTER);
            label.setAnchor(WidgetAnchor.TOP_CENTER);
            label.shiftYPos(10);

            label2 = new GenericLabel("Please enter the password to the protection below");
            label2.setAlign(WidgetAnchor.TOP_CENTER);
            label2.setAnchor(WidgetAnchor.TOP_CENTER);
            label2.shiftYPos(25);

            textField = new GenericTextField();
            textField.setMaximumCharacters(50);
            textField.setAnchor(WidgetAnchor.CENTER_CENTER);
            textField.setHeight(20).setWidth(200);
            textField.shiftXPos(-100).shiftYPos(-60);
            // textField.setX(20).setY(50);

            unlock = new GenericButton("Unlock");
            unlock.setAnchor(WidgetAnchor.CENTER_CENTER);
            unlock.setHeight(20).setWidth(135);
            unlock.shiftXPos(-100).shiftYPos(-30);
            // unlock.setX(20).setY(75);

            cancel = new GenericButton("Cancel");
            cancel.setAnchor(WidgetAnchor.CENTER_CENTER);
            cancel.setHeight(20).setWidth(60);
            cancel.shiftXPos(40).shiftYPos(-30);
            // cancel.setX(160).setY(75);

            // attach them
            attachWidget(PasswordRequestModule.this.plugin, label);
            attachWidget(PasswordRequestModule.this.plugin, label2);
            attachWidget(PasswordRequestModule.this.plugin, textField);
            attachWidget(PasswordRequestModule.this.plugin, unlock);
            attachWidget(PasswordRequestModule.this.plugin, cancel);
            PasswordRequestModule.this.plugin.bindLogo(this);
        }

        @Override
        public void onButtonClicked(ButtonClickEvent event) {
            Button button = event.getButton();
            SpoutPlayer player = event.getPlayer();
            LWC lwc = LWC.getInstance();
            LWCPlayer lwcPlayer = lwc.wrapPlayer(player);

            Action action = lwcPlayer.getAction("interacted");

            // if they don't have an unlock req, why is the screen open?
            if (action == null) {
                player.getMainScreen().closePopup();
                return;
            }

            if (button == cancel) {
                player.getMainScreen().closePopup();
            } else if (button == unlock) {
                // check their password
                String password = lwc.encrypt(textField.getText().trim());

                // load the protection they had clicked
                Protection protection = action.getProtection();

                if (protection == null) {
                    lwc.sendLocale(player, "protection.internalerror", "id", "unlock");
                    return;
                }

                if (protection.getType() != ProtectionTypes.PASSWORD) {
                    lwc.sendLocale(player, "protection.unlock.notpassword");
                    return;
                }

                if (protection.getPassword().equals(password)) {
                    lwcPlayer.addAccessibleProtection(protection);
                    player.getMainScreen().closePopup();

                    // open the chest that they clicked :P
                    // Unsafe code so we catch it just incase
                    try {
                        Block block = protection.getBlock();

                        net.minecraft.server.World handle = ((CraftWorld) block.getWorld()).getHandle();
                        net.minecraft.server.EntityPlayer plr = (net.minecraft.server.EntityPlayer) ((CraftHumanEntity) player).getHandle();

                        switch (block.getType()) {
                            case CHEST:
                                net.minecraft.server.Block.CHEST.interact(handle, block.getX(), block.getY(), block.getZ(), plr);
                                break;

                            case FURNACE:
                                net.minecraft.server.Block.FURNACE.interact(handle, block.getX(), block.getY(), block.getZ(), plr);
                                break;

                            case DISPENSER:
                                net.minecraft.server.Block.DISPENSER.interact(handle, block.getX(), block.getY(), block.getZ(), plr);
                                break;
                        }
                    } catch (Throwable t) {
                        PasswordRequestModule.this.plugin.log("Warning: Open inventory via PasswordRequestModule is broken!");
                    }

                    lwc.sendLocale(player, "protection.unlock.password.valid");
                } else {
                    lwc.sendLocale(player, "protection.unlock.password.invalid");
                }
            }
        }

    }

    @Override
    public void onSendLocale(LWCSendLocaleEvent event) {
        if (event.isCancelled()) {
            return;
        }

        SpoutPlayer player = SpoutManager.getPlayer(event.getPlayer());
        String locale = event.getLocale();

        // we only want spout clients!
        if (!player.isSpoutCraftEnabled()) {
            return;
        }

        // wait for them to try to open a passworded chest
        if (!locale.equals("protection.general.locked.password")) {
            return;
        }

        // cancel it and send them a popup :)
        event.setCancelled(true);

        // create the popup
        PopupScreen popup = createPopup();

        // Send it to the player!
        player.getMainScreen().attachPopupScreen(popup);
    }

    /**
     * Create a popup screen that shows the password request interface
     *
     * @return
     */
    private PopupScreen createPopup() {
        return new PasswordRequestPopup();
    }

}
