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

package com.griefcraft.modules.modify;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.AccessRight;
import com.griefcraft.model.Action;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.util.Colors;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.griefcraft.util.StringUtils.join;

public class ModifyModule extends JavaModule {

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("modify")) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        LWCPlayer player = lwc.wrapPlayer(event.getPlayer());
        event.setResult(Result.CANCEL);

        if (lwc.canAdminProtection(player.getBukkitPlayer(), protection)) {
            Action action = player.getAction("modify");

            final String defaultEntities = action.getData();
            String[] entities = new String[0];

            if (defaultEntities.length() > 0) {
                entities = defaultEntities.split(" ");
            }

            lwc.removeModes(player);

            for (String rightsName : entities) {
                boolean remove = false;
                boolean isAdmin = false;
                int type = AccessRight.PLAYER;

                if (rightsName.startsWith("-")) {
                    remove = true;
                    rightsName = rightsName.substring(1);
                }

                if (rightsName.startsWith("@")) {
                    isAdmin = true;
                    rightsName = rightsName.substring(1);
                }

                if (rightsName.toLowerCase().startsWith("g:")) {
                    type = AccessRight.GROUP;
                    rightsName = rightsName.substring(2);
                }

                if (rightsName.toLowerCase().startsWith("l:")) {
                    type = AccessRight.LIST;
                    rightsName = rightsName.substring(2);
                }

                if (rightsName.toLowerCase().startsWith("list:")) {
                    type = AccessRight.LIST;
                    rightsName = rightsName.substring(5);
                }

                if (rightsName.toLowerCase().startsWith("t:")) {
                    type = AccessRight.TOWN;
                    rightsName = rightsName.substring(2);
                }

                if (rightsName.toLowerCase().startsWith("town:")) {
                    type = AccessRight.TOWN;
                    rightsName = rightsName.substring(5);
                }

                int protectionId = protection.getId();
                String localeChild = AccessRight.typeToString(type).toLowerCase();

                if (!remove) {
                    AccessRight accessRight = new AccessRight();
                    accessRight.setProtectionId(protectionId);
                    accessRight.setRights(isAdmin ? 1 : 0);
                    accessRight.setName(rightsName);
                    accessRight.setType(type);

                    // add it to the protection and queue it to be saved
                    protection.addAccessRight(accessRight);
                    protection.save();
                    
                    lwc.sendLocale(player, "protection.interact.rights.register." + localeChild, "name", rightsName, "isadmin", isAdmin ? "[" + Colors.Red + "ADMIN" + Colors.Gold + "]" : "");
                } else {
                    protection.removeAccessRightsMatching(rightsName, type);
                    protection.save();

                    lwc.sendLocale(player, "protection.interact.rights.remove." + localeChild, "name", rightsName, "isadmin", isAdmin ? "[" + Colors.Red + "ADMIN" + Colors.Gold + "]" : "");
                }
            }
        } else {
            lwc.sendLocale(player, "protection.interact.error.notowner", "block", LWC.materialToString(protection.getBlockId()));
            lwc.removeModes(player);
        }

        return;
    }

    @Override
    public void onBlockInteract(LWCBlockInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("modify")) {
            return;
        }

        LWC lwc = event.getLWC();
        Block block = event.getBlock();
        Player player = event.getPlayer();
        event.setResult(Result.CANCEL);

        lwc.sendLocale(player, "protection.interact.error.notregistered", "block", LWC.materialToString(block));
        lwc.removeModes(player);
        return;
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("m", "modify")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();
        event.setCancelled(true);

        if (!(sender instanceof Player)) {
            sender.sendMessage(Colors.Red + "Console not supported.");
            return;
        }

        if (!lwc.hasPlayerPermission(sender, "lwc.modify")) {
            lwc.sendLocale(sender, "protection.accessdenied");
            return;
        }

        if (args.length < 1) {
            lwc.sendLocale(sender, "help.modify");
            return;
        }

        String full = join(args, 0);
        LWCPlayer player = lwc.wrapPlayer(sender);

        Action action = new Action();
        action.setName("modify");
        action.setPlayer(player);
        action.setData(full);

        player.removeAllActions();
        player.addAction(action);

        lwc.sendLocale(sender, "protection.modify.finalize");
        return;
    }

}
