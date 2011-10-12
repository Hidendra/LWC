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

package com.griefcraft.modules.debug;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.model.Flag;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.Colors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugModule extends JavaModule {

    /**
     * The value for yes in the locale
     */
    private String yes = null;

    /**
     * The value for no in the locale
     */
    private String no = null;

    @Override
    public void load(LWC lwc) {
        yes = lwc.getLocale("yes");
        no = lwc.getLocale("no");
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("debug")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();
        event.setCancelled(true);

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only usable by real players :-)");
            return;
        }

        Player player = (Player) sender;

        player.sendMessage(" ");
        player.sendMessage(Colors.Gray + "LWC: " + LWCInfo.FULL_VERSION);
        player.sendMessage(" ");
        player.sendMessage(Colors.Green + "Standard LWC permissions");
        doPermission(player, "lwc.protect");

        doPlayerPermission(player, "lwc.create.public");
        doPlayerPermission(player, "lwc.create.password");
        doPlayerPermission(player, "lwc.create.private");
        doPlayerPermission(player, "lwc.info");
        doPlayerPermission(player, "lwc.remove");
        doPlayerPermission(player, "lwc.modify");
        doPlayerPermission(player, "lwc.unlock");

        for (Flag.Type type : Flag.Type.values()) {
            doPlayerPermission(player, "lwc.flag." + type.toString().toLowerCase());
        }

        player.sendMessage(" ");
        player.sendMessage(Colors.Yellow + "Mod permissions");
        doPermission(player, "lwc.mod");

        player.sendMessage(" ");
        player.sendMessage(Colors.Red + "Admin permissions");
        doPermission(player, "lwc.admin");


    }

    /**
     * @param player
     * @param node
     */
    private void doPermission(Player player, String node) {
        player.sendMessage(node + ": " + strval(LWC.getInstance().hasPermission(player, node)));
    }

    /**
     * @param player
     * @param node
     */
    private void doPlayerPermission(Player player, String node) {
        player.sendMessage(node + ": " + strval(LWC.getInstance().hasPlayerPermission(player, node)));
    }

    /**
     * @param player
     * @param node
     */
    private void doAdminPermission(Player player, String node) {
        player.sendMessage(node + ": " + strval(LWC.getInstance().hasAdminPermission(player, node)));
    }

    /**
     * Convert a boolean to its yes/no equivilent
     *
     * @param bool
     * @return
     */
    private String strval(boolean bool) {
        return bool ? yes : no;
    }

}
