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
import net.minecraft.server.Container;
import net.minecraft.server.ContainerFurnace;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ICrafting;
import net.minecraft.server.Packet100OpenWindow;
import net.minecraft.server.TileEntityFurnace;
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
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("a", "admin")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if (!args[0].equals("view")) {
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
        EntityPlayer plr = (EntityPlayer) human;
        ICrafting crafting = (ICrafting) plr;
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        Container container = null;

        switch (block.getType()) {
            case CHEST:
                // net.minecraft.server.Block.CHEST.interact(handle, x, y, z, human);
                break;

            case FURNACE:
                // net.minecraft.server.Block.FURNACE.interact(handle, x, y, z, human);
                // human.a((net.minecraft.server.TileEntityFurnace) handle.getTileEntity(x, y, z));

                TileEntityFurnace furnace = (TileEntityFurnace) handle.getTileEntity(x, y, z);
                container = new ContainerFurnace(plr.inventory, furnace);

                plr.netServerHandler.sendPacket(new Packet100OpenWindow(42, 2, "LWC", 3));
                crafting.a(container, 0, furnace.cookTime);
                crafting.a(container, 1, furnace.burnTime);
                crafting.a(container, 2, furnace.ticksForCurrentFuel);
                break;

            case DISPENSER:
                net.minecraft.server.Block.DISPENSER.interact(handle, x, y, z, human);
                break;
        }

        lwc.sendLocale(sender, "protection.admin.view.viewing", "id", protectionId);
        return;
    }

}