/**
 * This file is part of LWC (https://github.com/Hidendra/LWC)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
