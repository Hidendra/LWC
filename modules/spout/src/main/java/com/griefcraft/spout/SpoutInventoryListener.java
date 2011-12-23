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

package com.griefcraft.spout;

import com.griefcraft.bukkit.LWCSpoutPlugin;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import org.bukkit.Location;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.getspout.spoutapi.event.inventory.InventoryClickEvent;
import org.getspout.spoutapi.event.inventory.InventoryListener;
import org.getspout.spoutapi.event.inventory.InventorySlotType;

public class SpoutInventoryListener extends InventoryListener {

    /**
     * The plugin instance
     */
    private LWCSpoutPlugin plugin;

    public SpoutInventoryListener(LWCSpoutPlugin plugin) {
        this.plugin = plugin;
    }

    public void onInventoryClick(InventoryClickEvent event) {
        LWC lwc = LWC.getInstance();
        // Player interacting with the inventory
        Player player = event.getPlayer();

        // The inventory they are using
        Inventory inventory = event.getInventory();

        // Location of the container
        Location location = event.getLocation();

        // Should be container
        InventorySlotType slotType = event.getSlotType();

        // If they are left clicking
        boolean leftClick = event.isLeftClick();

        // If it doesn't have a location we can't protect it :p
        if (inventory == null || location == null) {
            return;
        }

        // If it's the player's inventory, ignore it
        if (inventory instanceof CraftInventoryPlayer) {
            return;
        }

        // If it's not a container, we don't want it
        if (slotType != InventorySlotType.CONTAINER) {
            return;
        }

        // The item they are taking/swapping with
        ItemStack item = event.getItem();

        // Item their cursor has
        ItemStack cursor = event.getCursor();

        // Are they inserting a stack?
        if (item != null && cursor != null && item.getType() == cursor.getType()) {
            // If they are clicking an item of the stack type, they are inserting it into the inventory,
            // not switching it
            // As long as the item isn't a degradable item, we can explicitly allow it if they have the same durability
            if (item.getDurability() == cursor.getDurability()) {
                return;
            }
        }

        // They are trying to take an item :p
        if ((item != null && cursor == null) || (item != null && cursor != null)) {
            // Attempt to load the protection at that location
            Protection protection = lwc.findProtection(location.getBlock());

            // If no protection was found we can safely ignore it
            if (protection == null) {
                return;
            }

            // If it's not a donation chest, ignore if
            if (protection.getType() != Protection.Type.DONATION) {
                return;
            }

            // Can they admin it? (remove items/etc)
            boolean canAdmin = lwc.canAdminProtection(player, protection);

            // nope.avi
            if (!canAdmin) {
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
            }
        }
    }

}
