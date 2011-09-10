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

        Player player = (Player) sender;
        String password = join(args, 0);
        password = encrypt(password);

        if (!lwc.getMemoryDatabase().hasPendingUnlock(player.getName())) {
            player.sendMessage(Colors.Red + "Nothing selected. Open a locked protection first.");
            return;
        } else {
            int protectionId = lwc.getMemoryDatabase().getUnlockID(player.getName());

            if (protectionId == -1) {
                lwc.sendLocale(player, "protection.internalerror", "id", "unlock");
                return;
            }

            Protection entity = lwc.getPhysicalDatabase().loadProtection(protectionId);

            if (entity.getType() != ProtectionTypes.PASSWORD) {
                lwc.sendLocale(player, "protection.unlock.notpassword");
                return;
            }

            if (entity.getData().equals(password)) {
                lwc.getMemoryDatabase().unregisterUnlock(player.getName());
                lwc.getMemoryDatabase().registerPlayer(player.getName(), protectionId);
                lwc.sendLocale(player, "protection.unlock.password.valid");
            } else {
                lwc.sendLocale(player, "protection.unlock.password.invalid");
            }
        }

        return;
    }

}
