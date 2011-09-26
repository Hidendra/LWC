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

package com.griefcraft.modules.towny;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.AccessRight;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCAccessEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.util.Colors;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyWorld;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TownyModule extends JavaModule {

    /**
     * If Towny borders are to be used
     */
    private boolean townyBorders;

    /**
     * The Towny plugin
     */
    private Towny towny;

    /**
     * Load the module
     */
    @Override
    public void load(LWC lwc) {
        this.townyBorders = lwc.getConfiguration().getBoolean("core.townyBorders", false);

        // check for Towny
        Plugin townyPlugin = lwc.getPlugin().getServer().getPluginManager().getPlugin("Towny");

        // abort !
        if (townyPlugin == null) {
            return;
        }

        this.towny = (Towny) townyPlugin;
    }

    /**
     * Cancel the event and inform the player that they cannot protect there
     *
     * @param event
     */
    private void trigger(LWCProtectionRegisterEvent event) {
        event.getPlayer().sendMessage(Colors.Red + "You can only protect blocks using LWC inside of a Town!");
        event.setCancelled(true);
    }

    @Override
    public void onAccessRequest(LWCAccessEvent event) {
        Player player = event.getPlayer();
        Protection protection = event.getProtection();

        if (event.getAccess() != AccessRight.RIGHT_NOACCESS) {
            return;
        }

        if (protection.getType() != ProtectionTypes.PRIVATE) {
            return;
        }

        if (towny == null) {
            return;
        }

        for (AccessRight right : protection.getAccessRights()) {
            if (right.getType() != AccessRight.TOWN) {
                continue;
            }

            String townName = right.getName();

            // Does the town exist?
            try {
                Town town = towny.getTownyUniverse().getTown(townName);

                if (town == null) {
                    return;
                }

                // check if the player is a resident of said town
                if (!town.hasResident(player.getName())) {
                    // Uh-oh!
                    event.setAccess(AccessRight.RIGHT_NOACCESS);
                } else {
                    // They're in the town :-)
                    event.setAccess(AccessRight.RIGHT_PLAYER);
                }

                // If they're the major, let them admin the protection
                if (town.getMayor().getName().equalsIgnoreCase(player.getName())) {
                    event.setAccess(AccessRight.RIGHT_ADMIN);
                }
            } catch (Exception e) {

            }
        }
    }

    /**
     * Just a note: catching NotRegisteredException (which where an Exception is caught is where its thrown)
     *              will throw a ClassNotFoundException when Towny is not installed.
     */
    @Override
    public void onRegisterProtection(LWCProtectionRegisterEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!townyBorders || towny == null) {
            return;
        }

        // the block being protected
        Block block = event.getBlock();

        // Get the towny world
        TownyWorld world = null;

        try {
            world = towny.getTownyUniverse().getWorld(block.getWorld().getName());
        } catch (Exception e) {
            // No world, don't let them protect it!
            trigger(event);
            return;
        }

        if (!world.isUsingTowny()) {
            return;
        }

        // attempt to get the town block
        TownBlock townBlock = null;

        try {
            townBlock = world.getTownBlock(Coord.parseCoord(block));
        } catch (Exception e) {
            // No world, don't let them protect it!
            trigger(event);
            return;
        }
    }

}
