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
import com.griefcraft.model.Action;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.griefcraft.util.StringUtil.join;

public class ModifyModule extends JavaModule {

    /**
     * Used to match an id parameter in /cmodify id:##
     */
    private final Pattern ID_MATCHER = Pattern.compile(".*id:(\\d+).*");

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
        Player bPlayer = event.getPlayer();
        event.setResult(Result.CANCEL);

        if (!lwc.isAdmin(bPlayer) && Boolean.parseBoolean(lwc.resolveProtectionConfiguration(protection.getBlock(), "readonly-modify"))) {
            lwc.sendLocale(player, "protection.accessdenied");
            return;
        }

        if (lwc.canAdminProtection(player.getBukkitPlayer(), protection)) {
            Action action = player.getAction("modify");

            String data = action.getData();
            String[] rights = new String[0];

            if (data.length() > 0) {
                rights = data.split(" ");
            }

            lwc.removeModes(player);
            lwc.processRightsModifications(player, protection, rights);
        } else {
            lwc.sendLocale(player, "protection.interact.error.notowner", "block", LWC.materialToString(protection.getBlockId()));
            lwc.removeModes(player);
        }

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
        String full = join(args, 0).trim();
        event.setCancelled(true);

        if (!lwc.hasPlayerPermission(sender, "lwc.modify")) {
            lwc.sendLocale(sender, "protection.accessdenied");
            return;
        }

        if (args.length < 1) {
            lwc.sendLocale(sender, "help.modify");
            return;
        }

        // Check for ID parameter
        Matcher matcher = ID_MATCHER.matcher(full);
        if (matcher.matches()) {
            int protectionId = Integer.parseInt(matcher.group(1));

            // load the protection
            Protection protection = lwc.getPhysicalDatabase().loadProtection(protectionId);

            // Does it even exist?
            if (protection == null) {
                lwc.sendLocale(sender, "lwc.protectionnotfound");
                return;
            }

            // Can they admin it? (if they're console, they can!)
            if (sender instanceof Player) {
                if (!lwc.canAdminProtection((Player) sender, protection)) {
                    lwc.sendLocale(sender, "protection.accessdenied");
                    return;
                }
            }

            // process it
            lwc.processRightsModifications(sender, protection, args);
            return;
        }

        if (!(sender instanceof Player)) {
            lwc.sendLocale(sender, "lwc.onlyrealplayers");
            return;
        }

        LWCPlayer player = lwc.wrapPlayer(sender);

        Action action = new Action();
        action.setName("modify");
        action.setPlayer(player);
        action.setData(full);

        player.removeAllActions();
        player.addAction(action);

        lwc.sendLocale(sender, "protection.modify.finalize");
    }

}
