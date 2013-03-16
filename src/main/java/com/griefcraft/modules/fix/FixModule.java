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

package com.griefcraft.modules.fix;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.util.matchers.DoubleChestMatcher;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class FixModule extends JavaModule {

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() == Result.CANCEL) {
            return;
        }

        if (!event.hasAction("fix")) {
            return;
        }

        LWC lwc = event.getLWC();
        LWCPlayer player = lwc.wrapPlayer(event.getPlayer());
        Protection protection = event.getProtection();
        Block block = protection.getBlock();

        if (!lwc.canAdminProtection(event.getPlayer(), protection)) {
            return;
        }

        // Should we fix orientation?
        if (DoubleChestMatcher.PROTECTABLES_CHESTS.contains(block.getType()) || block.getType() == Material.FURNACE || block.getType() == Material.DISPENSER) {
            lwc.adjustChestDirection(block, event.getEvent().getBlockFace());
            lwc.sendLocale(player, "lwc.fix.fixed", "block", block.getType().toString().toLowerCase());
            player.removeAction(player.getAction("fix"));
        }
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getSender() instanceof Player)) {
            return;
        }

        if (!event.hasFlag("fix")) {
            return;
        }

        LWC lwc = event.getLWC();
        LWCPlayer player = lwc.wrapPlayer(event.getSender());

        // create the action
        com.griefcraft.model.Action action = new com.griefcraft.model.Action();
        action.setName("fix");
        action.setPlayer(player);

        player.addAction(action);
        lwc.sendLocale(player, "lwc.fix.clickblock");
        event.setCancelled(true);
    }

}
