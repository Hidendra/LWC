/*
 * Copyright (c) 2011, 2012, Tyler Blair
 * All rights reserved.
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

package org.getlwc.bukkit.listeners;

import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.getlwc.Block;
import org.getlwc.ExplosionType;
import org.getlwc.World;
import org.getlwc.bukkit.BukkitPlugin;
import org.getlwc.bukkit.entity.BukkitEntity;
import org.getlwc.bukkit.world.BukkitBlock;
import org.getlwc.entity.Entity;
import org.getlwc.entity.Player;

import java.util.List;

public class BukkitListener implements Listener {

    /**
     * The plugin object
     */
    private BukkitPlugin plugin;

    public BukkitListener(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void playerJoin(PlayerJoinEvent event) {
        plugin.getEngine().getEventHelper().onPlayerQuit(plugin.wrapPlayer(event.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true)
    public void playerQuit(PlayerQuitEvent event) {
        plugin.getEngine().getEventHelper().onPlayerQuit(plugin.wrapPlayer(event.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true)
    public void playerInteract(PlayerInteractEvent event) {
        Player player = plugin.wrapPlayer(event.getPlayer());
        World world = plugin.getWorld(event.getPlayer().getWorld().getName());
        Block block = new BukkitBlock(world, event.getClickedBlock());

        if (plugin.getEngine().getEventHelper().onBlockInteract(player, block)) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void entityInteract(EntityInteractEvent event) {
        Entity entity = new BukkitEntity(plugin, event.getEntity());
        World world = plugin.getWorld(event.getEntity().getWorld().getName());
        Block block = new BukkitBlock(world, event.getBlock());

        if (plugin.getEngine().getEventHelper().onBlockInteract(entity, block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent event) {
        Player player = plugin.wrapPlayer(event.getPlayer());
        World world = plugin.getWorld(event.getPlayer().getWorld().getName());
        Block block = new BukkitBlock(world, event.getBlock());

        if (plugin.getEngine().getEventHelper().onBlockBreak(player, block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void entityBreakDoor(EntityBreakDoorEvent event) {
        Entity entity = new BukkitEntity(plugin, event.getEntity());
        World world = plugin.getWorld(event.getEntity().getWorld().getName());
        Block block = new BukkitBlock(world, event.getBlock());

        if (plugin.getEngine().getEventHelper().onBlockBreak(entity, block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void blockPlace(BlockPlaceEvent event) {
        Player player = plugin.wrapPlayer(event.getPlayer());
        World world = plugin.getWorld(event.getPlayer().getWorld().getName());
        Block block = new BukkitBlock(world, event.getBlock());

        if (plugin.getEngine().getEventHelper().onBlockPlace(player, block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void signChange(SignChangeEvent event) {
        Player player = plugin.wrapPlayer(event.getPlayer());
        World world = plugin.getWorld(event.getPlayer().getWorld().getName());
        Block block = new BukkitBlock(world, event.getBlock());

        if (plugin.getEngine().getEventHelper().onSignChange(player, block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void structureGrow(StructureGrowEvent event) {
        World world = plugin.getWorld(event.getLocation().getWorld().getName());
        List<Block> blocks = plugin.castBlockStateList(world, event.getBlocks());

        if (plugin.getEngine().getEventHelper().onStructureGrow(plugin.castLocation(event.getLocation()), blocks)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void redstoneChange(BlockRedstoneEvent event) {
        World world = plugin.getWorld(event.getBlock().getWorld().getName());
        Block block = new BukkitBlock(world, event.getBlock());

        if (plugin.getEngine().getEventHelper().onRedstoneChange(block, event.getOldCurrent(), event.getNewCurrent())) {
            event.setNewCurrent(event.getOldCurrent());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void inventoryMoveItem(InventoryMoveItemEvent event) {
        if (handleMoveItemEvent(event.getSource()) || handleMoveItemEvent(event.getDestination())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void inventoryClickItem(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player)) {
            return;
        }

        // Player interacting with the inventory
        Player player = plugin.wrapPlayer((org.bukkit.entity.Player) event.getWhoClicked());

        // The inventory they are using
        Inventory inventory = event.getInventory();

        if (inventory == null || event.getSlot() < 0) {
            return;
        }

        // Location of the container
        org.bukkit.Location location;
        InventoryHolder holder = null;

        try {
            holder = event.getInventory().getHolder();
        } catch (AbstractMethodError e) {
            return;
        }

        try {
            if (holder instanceof BlockState) {
                location = ((BlockState) holder).getLocation();
            } else if (holder instanceof DoubleChest) {
                location = ((DoubleChest) holder).getLocation();
            } else {
                return;
            }
        } catch (Exception e) {
            return;
        }

        if (event.getSlotType() != InventoryType.SlotType.CONTAINER) {
            return;
        }

        boolean doubleClick = false;

        // backwards compatibility
        try {
            // doubleClick = event.isDoubleClick();
        } catch (Throwable e) {
        } // OK, just old build

        //
        if (plugin.getEngine().getEventHelper().onInventoryClickItem(player, plugin.castLocation(location), plugin.castItemStack(event.getCurrentItem()), plugin.castItemStack(event.getCursor()), event.getSlot(), event.getRawSlot(), event.isRightClick(), event.isShiftClick(), doubleClick)) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void entityExplode(EntityExplodeEvent event) {
        ExplosionType type = null;

        if (event.getEntityType() == EntityType.PRIMED_TNT) {
            type = ExplosionType.TNT;
        } else if (event.getEntityType() == EntityType.CREEPER) {
            type = ExplosionType.CREEPER;
        }

        if (type == null) {
            throw new UnsupportedOperationException("Unsupported explosion entity: " + event.getEntityType());
        }

        World world = plugin.getWorld(event.getLocation().getWorld().getName());
        List<Block> affected = plugin.castBlockList(world, event.blockList());

        /**
         * TODO - does not support removing specific blocks
         */
        if (plugin.getEngine().getEventHelper().onExplosion(type, affected)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void pistonExtend(BlockPistonExtendEvent event) {
        World world = plugin.getWorld(event.getBlock().getWorld().getName());
        Block block = new BukkitBlock(world, event.getBlock());

        if (plugin.getEngine().getEventHelper().onPistonExtend(block, plugin.castLocation(event.getBlock().getRelative(event.getDirection()).getLocation()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void pistonRetract(BlockPistonRetractEvent event) {
        World world = plugin.getWorld(event.getBlock().getWorld().getName());
        Block block = new BukkitBlock(world, event.getBlock());

        if (plugin.getEngine().getEventHelper().onPistonRetract(block, plugin.castLocation(event.getRetractLocation()))) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles the {@link InventoryMoveItemEvent} event, for the source/dest inventories
     *
     * @param inventory
     * @return
     */
    private boolean handleMoveItemEvent(Inventory inventory) {
        if (inventory == null) {
            return false;
        }

        // Location of the container
        org.bukkit.Location location;
        InventoryHolder holder;

        try {
            holder = inventory.getHolder();
        } catch (AbstractMethodError e) {
            return false;
        }

        try {
            if (holder instanceof BlockState) {
                location = ((BlockState) holder).getLocation();
            } else if (holder instanceof DoubleChest) {
                location = ((DoubleChest) holder).getLocation();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        World world = plugin.getWorld(location.getWorld().getName());
        Block block = new BukkitBlock(world, location.getBlock());

        return plugin.getEngine().getEventHelper().onInventoryMoveItem(block.getLocation());
    }

}
