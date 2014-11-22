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
import org.bukkit.event.EventPriority;
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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.getlwc.Block;
import org.getlwc.EventHelper;
import org.getlwc.ExplosionType;
import org.getlwc.World;
import org.getlwc.bukkit.BukkitPlugin;
import org.getlwc.bukkit.entity.BukkitEntity;
import org.getlwc.bukkit.world.BukkitBlock;
import org.getlwc.command.CommandContext;
import org.getlwc.command.CommandException;
import org.getlwc.command.CommandSender;
import org.getlwc.entity.Entity;
import org.getlwc.entity.Player;

import java.util.List;

public class BukkitListener implements Listener {

    private BukkitPlugin plugin;

    public BukkitListener(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a player uses a command
     *
     * @param event
     */
    @SuppressWarnings("unused")
    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = plugin.wrapPlayer(event.getPlayer());
        String message = event.getMessage();

        if (handlerCommandEvent(CommandContext.Type.PLAYER, player, message)) {
            event.setCancelled(true);
        }
    }

    /**
     * Called when the console uses a command.
     * Using LOWEST because of kTriggers
     *
     * @param event
     */
    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerCommand(ServerCommandEvent event) {
        if (handlerCommandEvent(CommandContext.Type.SERVER, plugin.getEngine().getConsoleSender(), event.getCommand())) {
            // TODO how to cancel? just change the command to something else?
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(ignoreCancelled = true)
    public void playerJoin(final PlayerJoinEvent event) {
        // when the player first logs in locale is not populated yet from the client in the login process (Packet204LocaleAndViewDistance)
        // So instead we run the LWC join method 20 ticks later
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                EventHelper.onPlayerJoin(plugin.wrapPlayer(event.getPlayer()));
            }
        }, 20);
    }

    @SuppressWarnings("unused")
    @EventHandler(ignoreCancelled = true)
    public void playerQuit(PlayerQuitEvent event) {
        EventHelper.onPlayerQuit(plugin.wrapPlayer(event.getPlayer()));
    }

    @SuppressWarnings("unused")
    @EventHandler(ignoreCancelled = true)
    public void playerInteract(PlayerInteractEvent event) {
        Player player = plugin.wrapPlayer(event.getPlayer());
        World world = plugin.getWorld(event.getPlayer().getWorld().getName());
        Block block = new BukkitBlock(world, event.getClickedBlock());

        if (EventHelper.onBlockInteract(player, block)) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(ignoreCancelled = true)
    public void entityInteract(EntityInteractEvent event) {
        Entity entity = new BukkitEntity(plugin, event.getEntity());
        World world = plugin.getWorld(event.getEntity().getWorld().getName());
        Block block = new BukkitBlock(world, event.getBlock());

        if (EventHelper.onBlockInteract(entity, block)) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent event) {
        Player player = plugin.wrapPlayer(event.getPlayer());
        World world = plugin.getWorld(event.getPlayer().getWorld().getName());
        Block block = new BukkitBlock(world, event.getBlock());

        if (EventHelper.onBlockBreak(player, block)) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(ignoreCancelled = true)
    public void entityBreakDoor(EntityBreakDoorEvent event) {
        Entity entity = new BukkitEntity(plugin, event.getEntity());
        World world = plugin.getWorld(event.getEntity().getWorld().getName());
        Block block = new BukkitBlock(world, event.getBlock());

        if (EventHelper.onBlockBreak(entity, block)) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(ignoreCancelled = true)
    public void blockPlace(BlockPlaceEvent event) {
        Player player = plugin.wrapPlayer(event.getPlayer());
        World world = plugin.getWorld(event.getPlayer().getWorld().getName());
        Block block = new BukkitBlock(world, event.getBlock());

        if (EventHelper.onBlockPlace(player, block)) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(ignoreCancelled = true)
    public void signChange(SignChangeEvent event) {
        Player player = plugin.wrapPlayer(event.getPlayer());
        World world = plugin.getWorld(event.getPlayer().getWorld().getName());
        Block block = new BukkitBlock(world, event.getBlock());

        if (EventHelper.onSignChange(player, block)) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(ignoreCancelled = true)
    public void redstoneChange(BlockRedstoneEvent event) {
        World world = plugin.getWorld(event.getBlock().getWorld().getName());
        Block block = new BukkitBlock(world, event.getBlock());

        if (EventHelper.onRedstoneChange(block, event.getOldCurrent(), event.getNewCurrent())) {
            event.setNewCurrent(event.getOldCurrent());
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(ignoreCancelled = true)
    public void inventoryMoveItem(InventoryMoveItemEvent event) {
        if (handleMoveItemEvent(event.getSource()) || handleMoveItemEvent(event.getDestination())) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
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

        boolean doubleClick = false; // ?

        if (EventHelper.onInventoryClickItem(player, plugin.castLocation(location), plugin.castItemStack(event.getCurrentItem()), plugin.castItemStack(event.getCursor()), event.getSlot(), event.getRawSlot(), event.isRightClick(), event.isShiftClick(), doubleClick)) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @SuppressWarnings("unused")
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
        if (EventHelper.onExplosion(type, affected)) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(ignoreCancelled = true)
    public void pistonExtend(BlockPistonExtendEvent event) {
        World world = plugin.getWorld(event.getBlock().getWorld().getName());
        Block block = new BukkitBlock(world, event.getBlock());

        if (EventHelper.onPistonExtend(block, plugin.castLocation(event.getBlock().getRelative(event.getDirection()).getLocation()))) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(ignoreCancelled = true)
    public void pistonRetract(BlockPistonRetractEvent event) {
        World world = plugin.getWorld(event.getBlock().getWorld().getName());
        Block block = new BukkitBlock(world, event.getBlock());

        if (EventHelper.onPistonRetract(block, plugin.castLocation(event.getRetractLocation()))) {
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

        return EventHelper.onInventoryMoveItem(block.getLocation());
    }

    /**
     * Command processor
     *
     * @param sender
     * @param message the name of the command followed by any arguments.
     * @return true if the command event should be cancelled
     */
    private boolean handlerCommandEvent(CommandContext.Type type, CommandSender sender, String message) {
        // Normalize the command, removing any prepended /, etc
        message = normalizeCommand(message);
        int indexOfSpace = message.indexOf(' ');

        try {
            if (indexOfSpace != -1) {
                String command = message.substring(0, indexOfSpace);
                String arguments = message.substring(indexOfSpace + 1);

                return plugin.getEngine().getCommandHandler().handleCommand(new CommandContext(type, sender, command, arguments));
            } else { // No arguments
                return plugin.getEngine().getCommandHandler().handleCommand(new CommandContext(type, sender, message));
            }
        } catch (CommandException e) {
            plugin.getEngine().getConsoleSender().sendMessage("An error was encountered while processing a command: {0}", e.getMessage());
            e.printStackTrace();

            sender.sendMessage("&4[LWC] An internal error occurred while processing this command");
            return false;
        }
    }

    /**
     * Normalize a command, making player and console commands appear to be the same format
     *
     * @param message
     * @return
     */
    private String normalizeCommand(String message) {
        // Remove a prepended /
        if (message.startsWith("/")) {
            if (message.length() == 1) {
                return "";
            } else {
                message = message.substring(1);
            }
        }

        return message.trim();
    }

}
