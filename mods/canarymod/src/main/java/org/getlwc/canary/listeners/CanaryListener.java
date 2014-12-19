/**
 * Copyright (c) 2011-2014 Tyler Blair
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
package org.getlwc.canary.listeners;

import net.canarymod.api.entity.TNTPrimed;
import net.canarymod.api.entity.living.monster.Creeper;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.entity.EndermanPickupBlockHook;
import net.canarymod.hook.player.BlockDestroyHook;
import net.canarymod.hook.player.BlockLeftClickHook;
import net.canarymod.hook.player.BlockPlaceHook;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.hook.player.ConnectionHook;
import net.canarymod.hook.player.DisconnectionHook;
import net.canarymod.hook.player.EntityRightClickHook;
import net.canarymod.hook.player.SignChangeHook;
import net.canarymod.hook.world.ExplosionHook;
import net.canarymod.hook.world.PistonExtendHook;
import net.canarymod.hook.world.PistonRetractHook;
import net.canarymod.hook.world.RedstoneChangeHook;
import net.canarymod.plugin.PluginListener;
import org.getlwc.Block;
import org.getlwc.EventHelper;
import org.getlwc.ExplosionType;
import org.getlwc.World;
import org.getlwc.canary.CanaryPlugin;
import org.getlwc.canary.entity.CanaryEntity;
import org.getlwc.canary.world.CanaryBlock;
import org.getlwc.entity.Entity;
import org.getlwc.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CanaryListener implements PluginListener {

    /**
     * Plugin object
     */
    private CanaryPlugin plugin;

    public CanaryListener(CanaryPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("unused")
    @HookHandler(ignoreCanceled = true)
    public void playerJoin(ConnectionHook event) {
        EventHelper.onPlayerJoin(plugin.wrapPlayer(event.getPlayer()));
    }

    @SuppressWarnings("unused")
    @HookHandler(ignoreCanceled = true)
    public void playerQuit(DisconnectionHook event) {
        EventHelper.onPlayerQuit(plugin.wrapPlayer(event.getPlayer()));
    }

    @SuppressWarnings("unused")
    @HookHandler(ignoreCanceled = true)
    public void entityRightClick(EntityRightClickHook hook) {
        Player player = plugin.wrapPlayer(hook.getPlayer());
        Entity target = plugin.wrapEntity(hook.getEntity());

        if (EventHelper.onEntityInteract(player, target)) {
            hook.setCanceled();
        }
    }

    @SuppressWarnings("unused")
    @HookHandler(ignoreCanceled = true)
    public void blockLeftClick(BlockLeftClickHook hook) {
        Player player = plugin.wrapPlayer(hook.getPlayer());
        World world = plugin.getWorld(hook.getPlayer().getWorld().getName());
        Block block = new CanaryBlock(world, hook.getBlock());

        boolean result = EventHelper.onBlockInteract(player, block);

        if (result) {
            hook.setCanceled();
        }
    }

    @SuppressWarnings("unused")
    @HookHandler(ignoreCanceled = true)
    public void blockRightClick(BlockRightClickHook hook) {
        Player player = plugin.wrapPlayer(hook.getPlayer());
        World world = plugin.getWorld(hook.getPlayer().getWorld().getName());
        Block block = new CanaryBlock(world, hook.getBlockClicked());

        boolean result = EventHelper.onBlockInteract(player, block);

        if (result) {
            hook.setCanceled();
        }
    }

    @SuppressWarnings("unused")
    @HookHandler(ignoreCanceled = true)
    public void entityInteract(EndermanPickupBlockHook hook) {
        Entity entity = new CanaryEntity(plugin, hook.getEnderman());
        World world = entity.getLocation().getWorld();
        Block block = new CanaryBlock(world, hook.getBlock());

        boolean result = EventHelper.onBlockInteract(entity, block);

        if (result) {
            hook.setCanceled();
        }
    }

    @SuppressWarnings("unused")
    @HookHandler(ignoreCanceled = true)
    public void blockDestroy(BlockDestroyHook hook) {
        Player player = plugin.wrapPlayer(hook.getPlayer());
        World world = plugin.getWorld(hook.getPlayer().getWorld().getName());
        Block block = new CanaryBlock(world, hook.getBlock());

        boolean result = EventHelper.onBlockBreak(player, block);

        if (result) {
            hook.setCanceled();
        }
    }

    @SuppressWarnings("unused")
    @HookHandler(ignoreCanceled = true)
    public void blockPlace(BlockPlaceHook hook) {
        Player player = plugin.wrapPlayer(hook.getPlayer());
        World world = plugin.getWorld(hook.getPlayer().getWorld().getName());
        Block block = new CanaryBlock(world, hook.getBlockPlaced());

        boolean result = EventHelper.onBlockPlace(player, block);

        if (result) {
            hook.setCanceled();
        }
    }

    @SuppressWarnings("unused")
    @HookHandler(ignoreCanceled = true)
    public void signChange(SignChangeHook hook) {
        Player player = plugin.wrapPlayer(hook.getPlayer());
        World world = plugin.getWorld(hook.getPlayer().getWorld().getName());
        Block block = new CanaryBlock(world, hook.getSign().getBlock());

        boolean result = EventHelper.onSignChange(player, block);

        if (result) {
            hook.setCanceled();
        }
    }

    @SuppressWarnings("unused")
    @HookHandler(ignoreCanceled = true)
    public void redstoneChange(RedstoneChangeHook hook) {
        World world = plugin.getWorld(hook.getSourceBlock().getWorld().getName());
        Block block = new CanaryBlock(world, hook.getSourceBlock());

        boolean result = EventHelper.onRedstoneChange(block, hook.getOldLevel(), hook.getNewLevel());

        if (result) {
            hook.setCanceled();
        }
    }

    @SuppressWarnings("unused")
    @HookHandler(ignoreCanceled = true)
    public void entityExplode(ExplosionHook hook) {
        ExplosionType type = null;

        if (hook.getEntity() instanceof TNTPrimed) {
            type = ExplosionType.TNT;
        } else if (hook.getEntity() instanceof Creeper) {
            type = ExplosionType.CREEPER;
        } else {
            throw new UnsupportedOperationException("Unsupported entity: " + hook.getEntity().getClass().getSimpleName() + " " + hook.getEntity().getName());
        }

        World world = plugin.getWorld(hook.getEntity().getWorld().getName());
        List<Block> affected = new ArrayList<>();

        for (net.canarymod.api.world.blocks.Block block : hook.getAffectedBlocks()) {
            affected.add(world.getBlockAt(block.getX(), block.getY(), block.getZ()));
        }

        boolean result = EventHelper.onExplosion(type, affected);

        if (result) {
            hook.setCanceled();
        }
    }

    @SuppressWarnings("unused")
    @HookHandler(ignoreCanceled = true)
    public void onPistonExtend(PistonExtendHook hook) {
        World world = plugin.getWorld(hook.getPiston().getWorld().getName());
        Block block = new CanaryBlock(world, hook.getPiston());

        if (EventHelper.onPistonExtend(block, plugin.castLocation(hook.getMoving().getLocation()))) {
            hook.setCanceled();
        }
    }

    @SuppressWarnings("unused")
    @HookHandler(ignoreCanceled = true)
    public void onPistonRetract(PistonRetractHook hook) {
        World world = plugin.getWorld(hook.getPiston().getWorld().getName());
        Block block = new CanaryBlock(world, hook.getPiston());

        if (EventHelper.onPistonExtend(block, plugin.castLocation(hook.getMoving().getLocation()))) {
            hook.setCanceled();
        }
    }

}
