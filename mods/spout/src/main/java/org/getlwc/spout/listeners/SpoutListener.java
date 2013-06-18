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

package org.getlwc.spout.listeners;

import org.getlwc.Block;
import org.getlwc.ExplosionType;
import org.getlwc.World;
import org.getlwc.entity.Entity;
import org.getlwc.entity.Player;
import org.getlwc.spout.SpoutPlugin;
import org.getlwc.spout.entity.SpoutEntity;
import org.getlwc.spout.world.SpoutBlock;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.block.BlockChangeEvent;
import org.spout.api.event.cause.PlayerCause;
import org.spout.api.event.entity.EntityInteractBlockEvent;
import org.spout.api.event.player.PlayerInteractBlockEvent;
import org.spout.api.event.player.PlayerJoinEvent;
import org.spout.api.event.player.PlayerLeaveEvent;
import org.spout.api.event.server.protection.EntityCanBreakEvent;
import org.spout.api.geo.discrete.Point;
import org.spout.vanilla.event.block.RedstoneChangeEvent;
import org.spout.vanilla.event.block.SignUpdateEvent;
import org.spout.vanilla.event.cause.PlayerBreakCause;
import org.spout.vanilla.event.cause.PlayerPlacementCause;
import org.spout.vanilla.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;

public class SpoutListener implements Listener {

    /**
     * The plugin object
     */
    private SpoutPlugin plugin;

    public SpoutListener(SpoutPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        plugin.getInternalEngine().getEventHelper().onPlayerQuit(plugin.wrapPlayer(event.getPlayer()));
    }

    @EventHandler
    public void playerQuit(PlayerLeaveEvent event) {
        plugin.getInternalEngine().getEventHelper().onPlayerQuit(plugin.wrapPlayer(event.getPlayer()));
    }

    @EventHandler
    public void playerInteract(PlayerInteractBlockEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = plugin.wrapPlayer((org.spout.api.entity.Player) event.getEntity());
        World world = plugin.getWorld(event.getEntity().getWorld().getName());
        Block block = new SpoutBlock(world, event.getInteracted());

        boolean result = plugin.getInternalEngine().getEventHelper().onBlockInteract(player, block);

        if (result) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void entityInteract(EntityInteractBlockEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Entity entity  = new SpoutEntity(plugin, event.getEntity());
        World world = plugin.getWorld(event.getEntity().getWorld().getName());
        Block block = new SpoutBlock(world, event.getInteracted());

        if (plugin.getInternalEngine().getEventHelper().onBlockInteract(entity, block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void entityBreakDoor(EntityCanBreakEvent event) {
        Entity entity  = new SpoutEntity(plugin, event.getEntity());
        World world = plugin.getWorld(event.getEntity().getWorld().getName());
        Block block = new SpoutBlock(world, event.getBlock());

        if (plugin.getInternalEngine().getEventHelper().onBlockBreak(entity, block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockChange(BlockChangeEvent event) {
        if (event.isCancelled() || event.getCause() == null) {
            return;
        }

        if (event.getCause() instanceof PlayerBreakCause) {
            onBlockBreak(event, (PlayerBreakCause) event.getCause());
        } else if (event.getCause() instanceof PlayerPlacementCause) {
            onBlockPlace(event, (PlayerPlacementCause) event.getCause());
        }
        // also PlayerPlacementCause, etc
    }

    @EventHandler
    public void signUpdate(SignUpdateEvent event) {
        if (event.isCancelled() || event.getSource() == null) {
            return;
        }

        Entity entity;

        if (event.getSource() instanceof PlayerCause) {
            entity = plugin.wrapPlayer(((PlayerCause) event.getSource()).getSource());
        } else {
            throw new UnsupportedOperationException("Unsupported event source for SignUpdateEvent: " + event.getSource().getClass().getSimpleName());
        }


        World world = plugin.getWorld(entity.getLocation().getWorld().getName());
        Block block = new SpoutBlock(world, event.getSign().getBlock());

        boolean result = plugin.getInternalEngine().getEventHelper().onSignChange(entity, block);

        if (result) {
            event.setCancelled(true);
        }
    }

    // Vanilla
    @EventHandler
    public void redstoneChange(RedstoneChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        World world = plugin.getWorld(event.getBlock().getWorld().getName());
        Block block = new SpoutBlock(world, event.getBlock());

        boolean result = plugin.getInternalEngine().getEventHelper().onRedstoneChange(block, event.getPreviousPower(), event.getNewPower());

        if (result) {
            event.setCancelled(true);
        }
    }

    // Vanilla event
    @EventHandler
    public void entityExplode(EntityExplodeEvent event) {
        ExplosionType type = null;

        System.out.println(event.getEntity() + " " + event.getEntity().getClass().getSimpleName());

        if (type == null) {
            throw new UnsupportedOperationException("Unsupported explosion entity: " + event.getEntity());
        }

        World world = plugin.getWorld(event.getEpicenter().getWorld().getName());
        List<Block> affected = new ArrayList<Block>();

        for (org.spout.api.geo.cuboid.Block block : event.getBlocks()) {
            affected.add(world.getBlockAt(block.getX(), block.getY(), block.getZ()));
        }

        /**
         * TODO - does not support removing specific blocks
         */
        boolean result = plugin.getInternalEngine().getEventHelper().onExplosion(type, affected);

        if (result) {
            event.setCancelled(true);
        }
    }

    private void onBlockBreak(BlockChangeEvent event, PlayerBreakCause cause) {
        Player player = plugin.wrapPlayer(cause.getSource());
        World world = plugin.getWorld(player.getLocation().getWorld().getName());
        Point point = event.getBlock().getPosition();
        Block block = new SpoutBlock(world, cause.getSource().getWorld().getBlock(point.getBlockX(), point.getBlockY(), point.getBlockZ()));

        boolean result = plugin.getInternalEngine().getEventHelper().onBlockBreak(player, block);

        if (result) {
            event.setCancelled(true);
        }
    }

    private void onBlockPlace(BlockChangeEvent event, PlayerPlacementCause cause) {
        Player player = plugin.wrapPlayer(cause.getSource());
        World world = plugin.getWorld(player.getLocation().getWorld().getName());
        Point point = event.getBlock().getPosition();
        Block block = new SpoutBlock(world, cause.getSource().getWorld().getBlock(point.getBlockX(), point.getBlockY(), point.getBlockZ()));

        boolean result = plugin.getInternalEngine().getEventHelper().onBlockPlace(player, block);

        if (result) {
            event.setCancelled(true);
        }
    }

}
