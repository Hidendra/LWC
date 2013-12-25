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

package com.griefcraft.io;

import com.griefcraft.lwc.LWC;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class RestorableBlock implements Restorable {

    /**
     * The block id
     */
    private int id;

    /**
     * The world's name
     */
    private String world;

    /**
     * The x coordinate
     */
    private int x;

    /**
     * The y coordinate
     */
    private int y;

    /**
     * The z coordinate
     */
    private int z;

    /**
     * The block data
     */
    private int data;

    /**
     * The items in this block's inventory if it has one
     */
    private final Map<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();

    public int getType() {
        return 1; // TODO ENUM, HOPEFULLY I'LL REMEMBER IF I PUT THIS TODO EVERYWHERE
    }

    public void restore() {
        LWC lwc = LWC.getInstance();

        lwc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(lwc.getPlugin(), new Runnable() {
            public void run() {
                Server server = Bukkit.getServer();

                // Get the world
                World bworld = server.getWorld(world);

                // Not found :-(
                if (world == null) {
                    return;
                }

                // Get the block we want
                Block block = bworld.getBlockAt(x, y, z);

                // Begin screwing with shit :p
                block.setTypeId(id);
                block.setData((byte) data);

                if (items.size() > 0) {
                    if (!(block.getState() instanceof InventoryHolder)) {
                        System.out.println(String.format("The block at [%d, %d, %d] has backed up items but no longer supports them. Why? %s", x, y, z, block.toString()));
                    }

                    // Get the block's inventory
                    Inventory inventory = ((InventoryHolder) block.getState()).getInventory();

                    // Set all of the items to it
                    for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                        int slot = entry.getKey();
                        ItemStack stack = entry.getValue();

                        if (stack == null) {
                            continue;
                        }

                        // Add it to the inventory
                        inventory.setItem(slot, stack);
                    }
                }
            }
        });
    }

    /**
     * Wrap a block in a restorableblock object
     *
     * @param block
     * @return
     */
    public static RestorableBlock wrapBlock(Block block) {
        if (block == null) {
            return null;
        }

        RestorableBlock rblock = new RestorableBlock();
        rblock.id = block.getTypeId();
        rblock.world = block.getWorld().getName();
        rblock.x = block.getX();
        rblock.y = block.getY();
        rblock.z = block.getZ();
        rblock.data = block.getData();

        BlockState state = block.getState();

        // Does it have an inventory? ^^
        if (state instanceof InventoryHolder) {
            Inventory inventory = ((InventoryHolder) state).getInventory();
            ItemStack[] stacks = inventory.getContents();

            for (int slot = 0; slot < stacks.length; slot++) {
                ItemStack stack = stacks[slot];

                if (stack == null) {
                    continue; // don't waste space!
                }

                rblock.setSlot(slot, stack);
            }
        }

        return rblock;
    }

    /**
     * Set a slot in the inventory
     *
     * @param slot
     * @param stack
     */
    public void setSlot(int slot, ItemStack stack) {
        items.put(slot, stack);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public Map<Integer, ItemStack> getItems() {
        return items;
    }
}
