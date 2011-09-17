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

package com.griefcraft.modules.unlock;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Action;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.Colors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.griefcraft.util.StringUtils.encrypt;
import static com.griefcraft.util.StringUtils.join;

public class UnlockModule extends JavaModule {

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("u", "unlock")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();
        event.setCancelled(true);

        if (!(sender instanceof Player)) {
            sender.sendMessage(Colors.Red + "Console is not supported.");
            return;
        }

        if (!lwc.hasPlayerPermission(sender, "lwc.unlock")) {
            lwc.sendLocale(sender, "protection.accessdenied");
            return;
        }

        if (args.length < 1) {
            lwc.sendSimpleUsage(sender, "/lwc -u <Password>");
            return;
        }

        LWCPlayer player = lwc.wrapPlayer(sender);
        String password = join(args, 0);
        password = encrypt(password);

        // see if they have the protection interaction action
        Action action = player.getAction("interacted");

        if (action == null) {
            player.sendMessage(Colors.Red + "Nothing selected. Open a locked protection first.");
        } else {
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
                player.addAccessibleProtection(protection);
                lwc.sendLocale(player, "protection.unlock.password.valid");
            } else {
                lwc.sendLocale(player, "protection.unlock.password.invalid");
            }
        }
    }

}
