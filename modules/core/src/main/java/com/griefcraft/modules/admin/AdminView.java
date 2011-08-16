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

package com.griefcraft.modules.admin;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.StringUtils;
import net.minecraft.server.EntityHuman;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.Player;

public class AdminView extends JavaModule {

    @Override
    public void onCommand(LWCCommandEvent event) {
        if(event.isCancelled()) {
            return;
        }

        if(!event.hasFlag("a", "admin")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if(!args[0].equals("view")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        if (!(sender instanceof Player)) {
            lwc.sendLocale(sender, "protection.admin.noconsole");
            return;
        }

        Player player = (Player) sender;
        World world = player.getWorld();

        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc admin view <id>");
            return;
        }

        int protectionId = Integer.parseInt(args[1]);
        Protection protection = lwc.getPhysicalDatabase().loadProtection(protectionId);

        if (protection == null) {
            lwc.sendLocale(sender, "protection.admin.view.noexist");
            return;
        }

        Block block = world.getBlockAt(protection.getX(), protection.getY(), protection.getZ());

        if (!(block.getState() instanceof ContainerBlock)) {
            lwc.sendLocale(sender, "protection.admin.view.noinventory");
            return;
        }

        net.minecraft.server.World handle = ((CraftWorld) block.getWorld()).getHandle();
        EntityHuman human = ((CraftHumanEntity) player).getHandle();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        switch (block.getType()) {
            case CHEST:
                net.minecraft.server.Block.CHEST.a(handle, x, y, z, human);
                break;

            case FURNACE:
                net.minecraft.server.Block.FURNACE.a(handle, x, y, z, human);
                break;

            case DISPENSER:
                net.minecraft.server.Block.DISPENSER.a(handle, x, y, z, human);
                break;
        }

        lwc.sendLocale(sender, "protection.admin.view.viewing", "id", protectionId);
        return;
    }

}