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

package com.griefcraft.modules.info;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Action;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Permission;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoModule extends JavaModule {

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("info")) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        Player player = event.getPlayer();
        event.setResult(Result.CANCEL);

        String type = lwc.getPlugin().getMessageParser().parseMessage(protection.typeToString().toLowerCase());

        lwc.sendLocale(player, "lwc.info", "owner", protection.getFormattedOwnerPlayerName(), "type", type);

        if (event.canAdmin()) {
            if (protection.getType() == Protection.Type.PRIVATE || protection.getType() == Protection.Type.DONATION) {
                lwc.sendLocale(player, "lwc.acl", "size", protection.getPermissions().size());
                int index = 0;
                for (Permission permission : protection.getPermissions()) {
                    if (index >= 9) {
                        break;
                    }

                    player.sendMessage(permission.toString());
                    index ++;
                }

                if (index == 0) {
                    lwc.sendLocale(player, "lwc.acl.empty");
                } else if (index >= 9) {
                    lwc.sendLocale(player, "lwc.acl.limitreached");
                }

                player.sendMessage("");
            }
        }

        if (lwc.isAdmin(player)) {
            lwc.sendLocale(player, "protection.interact.info.raw", "raw", protection.toString());
        }

        lwc.removeModes(player);
    }

    @Override
    public void onBlockInteract(LWCBlockInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("info")) {
            return;
        }

        LWC lwc = event.getLWC();
        Block block = event.getBlock();
        Player player = event.getPlayer();
        event.setResult(Result.CANCEL);

        lwc.sendLocale(player, "protection.interact.error.notregistered", "block", LWC.materialToString(block));
        lwc.removeModes(player);
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("i", "info")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if (!(sender instanceof Player)) {
            return;
        }

        event.setCancelled(true);

        if (!lwc.hasPlayerPermission(sender, "lwc.info")) {
            lwc.sendLocale(sender, "protection.accessdenied");
            return;
        }

        LWCPlayer player = lwc.wrapPlayer(sender);
        String type = "info";

        if (args.length > 0) {
            type = args[0].toLowerCase();
        }

        if (type.equals("info")) {
            Action action = new Action();
            action.setName("info");
            action.setPlayer(player);

            player.removeAllActions();
            player.addAction(action);

            lwc.sendLocale(player, "protection.info.finalize");
        } else if (type.equals("history")) {
            Action action = new Action();
            action.setName("history");
            action.setData("0");
            action.setPlayer(player);

            player.removeAllActions();
            player.addAction(action);

            lwc.sendLocale(player, "protection.info.finalize");
        }
    }

}
