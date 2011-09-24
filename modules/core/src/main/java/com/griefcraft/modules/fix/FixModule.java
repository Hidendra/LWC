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
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.util.Colors;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FixModule extends JavaModule {

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.canAccess()) {
            return;
        }

        if (!event.hasAction("fix")) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        Block block = protection.getBlock();
        Player player = event.getPlayer();

        event.setResult(Result.CANCEL);


        // Is it a chest?
        if (block.getType() == Material.CHEST) {
            // Fix it!
            lwc.adjustChestDirection(block, event.getEvent().getBlockFace());
            player.sendMessage(Colors.Green + "Fixed the block face");
        }

        lwc.getMemoryDatabase().unregisterAction("fix", player.getName());
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("fix")) {
            return;
        }


        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if (!(sender instanceof Player)) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) sender;
        
        lwc.getMemoryDatabase().registerAction("fix", player.getName());
        player.sendMessage(Colors.Green + "Click on a block to fix it");
    }

}
