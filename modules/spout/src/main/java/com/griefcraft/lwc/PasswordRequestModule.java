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
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCSendLocaleEvent;
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
        }

        @Override
        public void onButtonClicked(ButtonClickEvent event) {
            Button button = event.getButton();
            SpoutPlayer player = event.getPlayer();
            LWC lwc = LWC.getInstance();

            // if they don't have an unlock req, why is the screen open?
            if (!lwc.getMemoryDatabase().hasPendingUnlock(player.getName())) {
                player.getMainScreen().closePopup();
                return;
            }

            if (button == cancel) {
                player.getMainScreen().closePopup();
            } else if (button == unlock) {
                // check their password
                String password = lwc.encrypt(textField.getText().trim());

                int protectionId = lwc.getMemoryDatabase().getUnlockID(player.getName());

                if (protectionId == -1) {
                    lwc.sendLocale(player, "protection.internalerror", "id", "unlock");
                    return;
                }

                // load the protection they had clicked
                Protection protection = lwc.getPhysicalDatabase().loadProtection(protectionId);

                if (protection.getType() != ProtectionTypes.PASSWORD) {
                    lwc.sendLocale(player, "protection.unlock.notpassword");
                    return;
                }

                if (protection.getData().equals(password)) {
                    lwc.getMemoryDatabase().unregisterUnlock(player.getName());
                    lwc.getMemoryDatabase().registerPlayer(player.getName(), protectionId);
                    player.getMainScreen().closePopup();

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
