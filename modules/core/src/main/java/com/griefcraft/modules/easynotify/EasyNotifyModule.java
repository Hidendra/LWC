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

package com.griefcraft.modules.easynotify;

import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionRegistrationPostEvent;
import com.griefcraft.util.Colors;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.Set;

/**
 * Sends notifications to players about various things when creating a protection
 */
public class EasyNotifyModule extends JavaModule {

    private final static Set<Material> REDSTONE_AFFECTED_BLOCKS = EnumSet.of(
            Material.POWERED_MINECART, Material.DETECTOR_RAIL, Material.PISTON_BASE, Material.PISTON_STICKY_BASE,
            Material.TNT, Material.REDSTONE_WIRE, Material.WOODEN_DOOR, Material.IRON_DOOR_BLOCK, Material.DIODE_BLOCK_OFF,
            Material.DIODE_BLOCK_ON, Material.TRAP_DOOR);

    @Override
    public void onPostRegistration(LWCProtectionRegistrationPostEvent event) {
        Player player = event.getPlayer();
        Protection protection = event.getProtection();
        Block block = event.getProtection().getBlock();

        // Redstone
        if (isBlockAffectedByRedstone(block)) {
            player.sendMessage(Colors.Red + "Note: " + Colors.White + "Redstone is currently allowed on this protection. To disable use of redstone on it, use " + Colors.Green + "/credstone on");
        }
    }

    /**
     * Check if a block can be affected by redstone
     * 
     * @param block
     * @return true if the block can be affected by redstone
     */
    private boolean isBlockAffectedByRedstone(Block block) {
        return REDSTONE_AFFECTED_BLOCKS.contains(block.getType());
    }

}
