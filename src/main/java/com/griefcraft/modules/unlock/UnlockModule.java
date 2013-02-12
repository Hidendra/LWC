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

package com.griefcraft.modules.unlock;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Action;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.griefcraft.util.StringUtil.encrypt;
import static com.griefcraft.util.StringUtil.join;

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
            lwc.sendLocale(sender, "lwc.onlyrealplayers");
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
            lwc.sendLocale(sender, "lwc.unlock.noselection");
        } else {
            Protection protection = action.getProtection();

            if (protection == null) {
                lwc.sendLocale(player, "protection.internalerror", "id", "unlock");
                return;
            }

            if (protection.getType() != Protection.Type.PASSWORD) {
                lwc.sendLocale(player, "protection.unlock.notpassword");
                return;
            }

            if (protection.getPassword().equals(password)) {
                player.addAccessibleProtection(protection);
                player.removeAction(action);
                lwc.sendLocale(player, "protection.unlock.password.valid");
            } else {
                lwc.sendLocale(player, "protection.unlock.password.invalid");
            }
        }
    }

}
