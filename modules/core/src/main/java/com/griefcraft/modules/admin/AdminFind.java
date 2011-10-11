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
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import org.bukkit.command.CommandSender;

import java.util.List;

public class AdminFind extends JavaModule {

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

        if (!args[0].equals("find")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc admin find <player> [page]");
            return;
        }

        final int perPage = 7; // listings per page

        String player = args[1];
        int page = 1;

        if (args.length > 2) {
            try {
                page = Integer.parseInt(args[2]);
            } catch (Exception e) {
                lwc.sendLocale(sender, "protection.find.invalidpage");
                return;
            }
        }

        int start = (page - 1) * perPage;

        List<Protection> protections = lwc.getPhysicalDatabase().loadProtectionsByPlayer(player, start, perPage);
        int results = lwc.getPhysicalDatabase().getProtectionCount(player);
        int max = protections.size(); // may not be the full perPage
        int ceil = start + max;

        lwc.sendLocale(sender, "protection.find.currentpage", "page", page);

        if (results != max) {
            lwc.sendLocale(sender, "protection.find.nextpage", "player", player, "page", page + 1);
        }

        lwc.sendLocale(sender, "protection.find.showing", "start", start, "ceil", ceil, "results", results);

        for (Protection protection : protections) {
            sender.sendMessage(protection.toString());
        }
    }

}