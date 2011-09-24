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

package com.griefcraft.modules.fix;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.util.Colors;
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

        // Is it a chest, yo?
        if (block.getType() == Material.CHEST) {
            // Fix it!
            lwc.adjustChestDirection(block, event.getEvent().getBlockFace());
            player.sendMessage(Colors.Green + "Fixed the chest!");
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

        LWCPlayer player = event.getLWC().wrapPlayer(event.getSender());

        // create the action
        com.griefcraft.model.Action action = new com.griefcraft.model.Action();
        action.setName("fix");
        action.setPlayer(player);

        player.addAction(action);
        player.sendMessage(Colors.Green + "Click on an object to fix it.");
        event.setCancelled(true);
    }
    
}
