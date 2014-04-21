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

package com.griefcraft.modules.admin;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Action;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.util.UUIDRegistry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AdminForceOwner extends JavaModule {

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("forceowner")) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        LWCPlayer player = lwc.wrapPlayer(event.getPlayer());

        Action action = player.getAction("forceowner");
        String newOwner = action.getData();

        protection.setOwner(newOwner);
        protection.save();

        lwc.sendLocale(player, "protection.interact.forceowner.finalize", "player", protection.getFormattedOwnerPlayerName());
        lwc.removeModes(player);
        event.setResult(Result.CANCEL);

    }

    @Override
    public void onBlockInteract(LWCBlockInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("forceowner")) {
            return;
        }

        LWC lwc = event.getLWC();
        Player player = event.getPlayer();

        lwc.sendLocale(player, "protection.interact.error.notregistered", "block", LWC.materialToString(event.getBlock()));
        lwc.removeModes(player);
        event.setResult(Result.CANCEL);
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("a", "admin")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if (!args[0].equals("forceowner")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc admin forceowner <Player> [ProtectionID]");
            return;
        }

        String newOwner = args[1];

        // did they provide an ID?
        if (args.length > 2) {
            try {
                int protectionId = Integer.parseInt(args[2]);

                Protection protection = lwc.getPhysicalDatabase().loadProtection(protectionId);

                // No protection found
                if (protection == null) {
                    lwc.sendLocale(sender, "lwc.protectionnotfound");
                    return;
                }

                UUID uuid = UUIDRegistry.getUUID(newOwner);

                if (uuid != null) {
                    protection.setOwner(uuid.toString());
                } else {
                    protection.setOwner(newOwner);
                }

                protection.save();

                lwc.sendLocale(sender, "protection.interact.forceowner.finalize", "player", UUIDRegistry.formatPlayerName(newOwner));
                return;
            } catch (NumberFormatException e) {
                lwc.sendLocale(sender, "lwc.invalidprotectionid");
                return;
            }
        }

        if (!(sender instanceof Player)) {
            lwc.sendLocale(sender, "protection.admin.noconsole");
            return;
        }


        LWCPlayer player = lwc.wrapPlayer(sender);
        Action action = new Action();
        action.setName("forceowner");
        action.setPlayer(player);

        UUID uuid = UUIDRegistry.getUUID(newOwner);

        if (uuid != null) {
            action.setData(uuid.toString());
        } else {
            action.setData(newOwner);
        }

        player.addAction(action);

        lwc.sendLocale(sender, "protection.admin.forceowner.finalize", "player", newOwner);
    }

}