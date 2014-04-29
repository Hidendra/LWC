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

package com.griefcraft.modules.create;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Action;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.scripting.event.LWCProtectionRegistrationPostEvent;
import com.griefcraft.sql.PhysDB;
import com.griefcraft.util.StringUtil;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateModule extends JavaModule {

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("create")) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        Player player = event.getPlayer();

        if (protection.isOwner(player)) {
            lwc.sendLocale(player, "protection.interact.error.alreadyregistered", "block", LWC.materialToString(protection.getBlockId()));
        } else {
            lwc.sendLocale(player, "protection.interact.error.notowner", "block", LWC.materialToString(protection.getBlockId()));
        }

        lwc.removeModes(player);
        event.setResult(Result.CANCEL);
    }

    @Override
    public void onBlockInteract(LWCBlockInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("create")) {
            return;
        }

        LWC lwc = event.getLWC();
        Block block = event.getBlock();
        LWCPlayer player = lwc.wrapPlayer(event.getPlayer());

        if (!lwc.isProtectable(block)) {
            return;
        }

        PhysDB physDb = lwc.getPhysicalDatabase();

        Action action = player.getAction("create");
        String actionData = action.getData();
        String[] split = actionData.split(" ");
        String protectionType = split[0].toLowerCase();
        String protectionData = StringUtil.join(split, 1);

        // check permissions again (DID THE LITTLE SHIT MOVE WORLDS??!?!?!?!?!?)
        if (!lwc.hasPermission(event.getPlayer(), "lwc.create." + protectionType, "lwc.create", "lwc.protect")) {
            lwc.sendLocale(player, "protection.accessdenied");
            lwc.removeModes(player);
            event.setResult(Result.CANCEL);
            return;
        }

        // misc data we'll use later
        String playerName = player.getName();
        String worldName = block.getWorld().getName();
        int blockX = block.getX();
        int blockY = block.getY();
        int blockZ = block.getZ();

        lwc.removeModes(player);
        LWCProtectionRegisterEvent evt = new LWCProtectionRegisterEvent(player.getBukkitPlayer(), block);
        lwc.getModuleLoader().dispatchEvent(evt);

        // another plugin cancelled the registration
        if (evt.isCancelled()) {
            return;
        }

        // The created protection
        Protection protection = null;

        if (protectionType.equals("public")) {
            protection = physDb.registerProtection(block.getTypeId(), Protection.Type.PUBLIC, worldName, player.getName(), "", blockX, blockY, blockZ);
            lwc.sendLocale(player, "protection.interact.create.finalize");
        } else if (protectionType.equals("password")) {
            String password = lwc.encrypt(protectionData);

            protection = physDb.registerProtection(block.getTypeId(), Protection.Type.PASSWORD, worldName, player.getName(), password, blockX, blockY, blockZ);
            player.addAccessibleProtection(protection);

            lwc.sendLocale(player, "protection.interact.create.finalize");
            lwc.sendLocale(player, "protection.interact.create.password");
        } else if (protectionType.equals("private") || protectionType.equals("donation")) {
            String[] rights = protectionData.split(" ");

            protection = physDb.registerProtection(block.getTypeId(), Protection.Type.matchType(protectionType), worldName, player.getName(), "", blockX, blockY, blockZ);

            lwc.sendLocale(player, "protection.interact.create.finalize");
            lwc.processRightsModifications(player, protection, rights);
        }

        // tell the modules that a protection was registered
        if (protection != null) {
            // Fix the blocks that match it
            protection.removeCache();
            LWC.getInstance().getProtectionCache().addProtection(protection);

            lwc.getModuleLoader().dispatchEvent(new LWCProtectionRegistrationPostEvent(protection));
        }

        event.setResult(Result.CANCEL);
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("c", "create")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if (!(sender instanceof Player)) {
            return;
        }

        if (args.length == 0) {
            lwc.sendLocale(sender, "help.creation");
            return;
        }

        LWCPlayer player = lwc.wrapPlayer(sender);

        String full = StringUtil.join(args, 0).trim();
        String type = args[0].toLowerCase();
        String data = StringUtil.join(args, 1);
        event.setCancelled(true);

        /**
         * Allow individual enforcements with e.g lwc.create.private, or just the umbrella lwc.create for all
         */
        if (!lwc.hasPermission(sender, "lwc.create." + type, "lwc.create", "lwc.protect")) {
            lwc.sendLocale(sender, "protection.accessdenied");
            return;
        }

        try {
            switch (Protection.Type.matchType(type)) {
                case PASSWORD:
                    if (args.length < 2) {
                        lwc.sendSimpleUsage(player, "/lwc -c password <Password>");
                        return;
                    }

                    String hiddenPass = StringUtil.transform(data, '*');
                    lwc.sendLocale(player, "protection.create.password", "password", hiddenPass);
                    break;
            }
        } catch (IllegalArgumentException e) {
            // Invalid protection type!
            lwc.sendLocale(player, "help.creation");
            return;
        }

        Action action = new Action();
        action.setName("create");
        action.setPlayer(player);
        action.setData(full);

        player.removeAllActions();
        player.addAction(action);

        lwc.sendLocale(player, "protection.create.finalize", "type", lwc.getPlugin().getMessageParser().parseMessage(type));
    }

}
