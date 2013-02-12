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

package com.griefcraft.modules.flag;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Action;
import com.griefcraft.model.Flag;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtil;
import org.bukkit.command.CommandSender;

public class BaseFlagModule extends JavaModule {

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (!event.hasAction("flag")) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        LWCPlayer player = lwc.wrapPlayer(event.getPlayer());

        Action action = player.getAction("flag");
        String data = action.getData();
        event.setResult(Result.CANCEL);

        if (!event.canAdmin()) {
            lwc.sendLocale(player, "protection.accessdenied");
            return;
        }

        boolean shouldAdd = data.substring(0, 1).equals("+");
        String flagName = data.substring(1);

        Flag.Type type = null;

        for (Flag.Type tmp : Flag.Type.values()) {
            if (tmp.toString().equalsIgnoreCase(flagName)) {
                type = tmp;
                break;
            }
        }

        if (type == null) {
            lwc.sendLocale(player, "protection.internalerror", "id", "flg");
            return;
        }

        //////// FIXME - needs to allow data somehow
        Flag flag = protection.getFlag(type);

        if (flag == null) {
            flag = new Flag(type);
        }

        if (shouldAdd) {
            protection.addFlag(flag);
            lwc.sendLocale(player, "protection.interact.flag.add", "flag", StringUtil.capitalizeFirstLetter(flagName));
        } else {
            protection.removeFlag(flag);
            lwc.sendLocale(player, "protection.interact.flag.remove", "flag", StringUtil.capitalizeFirstLetter(flagName));
        }

        protection.save();
        lwc.removeModes(player);
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (!event.hasFlag("f", "flag")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();
        event.setCancelled(true);

        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc flag <flag> <on/off>");

            // TODO
            String flags = "";
            for (Flag.Type type : Flag.Type.values()) {
                flags += Colors.Yellow + type.toString().toLowerCase() + Colors.White + ", ";
            }
            flags = flags.substring(0, flags.length() - 2);

            lwc.sendLocale(sender, "lwc.flags.available", "flags", flags);
            return;
        }

        LWCPlayer player = lwc.wrapPlayer(sender);
        String flagName = args[0];
        String type = args[1].toLowerCase();
        String internalType; // + or -

        // Allow lwc.flag.?? (e.g lwc.flag.redstone) or optionally the umbrella node lwc.allflags
        if (!lwc.hasPermission(sender, "lwc.flag." + flagName, "lwc.protect", "lwc.allflags")) {
            lwc.sendLocale(sender, "protection.accessdenied");
            return;
        }

        // verify the flag name
        Flag.Type match = null;
        for (Flag.Type flag : Flag.Type.values()) {
            if (flag.toString().equalsIgnoreCase(flagName) || flag.toString().toLowerCase().startsWith(flagName.toLowerCase())) {
                match = flag;
                flagName = flag.toString(); // get the case-correct name while we're there
                break;
            }
        }

        if (match == null) {
            lwc.sendLocale(sender, "protection.flag.invalidflag", "flag", flagName);
            return;
        }

        // ensure it is not a restricted flag
        if (match.isRestricted() && !lwc.isAdmin(player)) {
            lwc.sendLocale(player, "protection.accessdenied");
            return;
        }

        if (type.equals("on") || type.equals("true") || type.equals("yes")) {
            internalType = "+";
        } else if (type.equals("off") || type.equals("false") || type.equals("no")) {
            internalType = "-";
        } else {
            lwc.sendLocale(sender, "protection.flag.invalidtype", "type", type);
            return;
        }

        Action action = new Action();
        action.setName("flag");
        action.setPlayer(player);
        action.setData(internalType + flagName);

        player.removeAllActions();
        player.addAction(action);

        lwc.sendLocale(sender, "protection.flag.finalize");
    }

}
