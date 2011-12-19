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

package com.griefcraft.modules.owners;

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

import java.util.List;

public class OwnersModule extends JavaModule {

    /**
     * How many results to show per page
     */
    public static final int RESULTS_PER_PAGE = 15;

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("owners")) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        LWCPlayer player = lwc.wrapPlayer(event.getPlayer());
        event.setResult(Result.CANCEL);

        Action action = player.getAction("owners");
        int accessPage = Integer.parseInt(action.getData());

        // Calculate range
        int start = (accessPage - 1) * RESULTS_PER_PAGE;
        int max = start + RESULTS_PER_PAGE;

        List<Permission> permissions = protection.getPermissions();
        int numRights = permissions.size();

        // May have only been 2 rows left, or something. Get the real max
        int realMax = start + permissions.size();

        lwc.sendLocale(player, "lwc.owners.results", "start", start, "max", realMax, "total", numRights);

        for (int index = 0; index < max; index++) {
            if ((start + index) >= numRights) {
                break;
            }

            Permission permission = permissions.get(start + index);
            player.sendMessage(permission.toString());
        }

        lwc.removeModes(player);
    }

    @Override
    public void onBlockInteract(LWCBlockInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("owners")) {
            return;
        }

        LWC lwc = event.getLWC();
        Player player = event.getPlayer();
        Block block = event.getBlock();

        lwc.sendLocale(player, "protection.interact.error.notregistered", "block", LWC.materialToString(block));
        lwc.removeModes(player);
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("o", "owner", "owners")) {
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

        LWCPlayer player = lwc.wrapPlayer(sender);
        int page = 1;

        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (Exception e) {
                lwc.sendSimpleUsage(sender, "/lwc -owners [page]");
                return;
            }
        }

        Action action = new Action();
        action.setName("owners");
        action.setPlayer(player);
        action.setData(page + "");

        player.removeAllActions();
        player.addAction(action);

        lwc.sendLocale(sender, "protection.owners.finalize");
    }

}
