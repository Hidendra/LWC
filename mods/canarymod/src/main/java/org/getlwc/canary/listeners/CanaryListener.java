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
import org.getlwc.canary.LWC;
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
    private LWC plugin;

    public CanaryListener(LWC plugin) {
        this.plugin = plugin;
    }

    @HookHandler(ignoreCanceled = true)
    public void playerJoin(ConnectionHook event) {
        EventHelper.onPlayerJoin(plugin.wrapPlayer(event.getPlayer()));
    }

    @HookHandler(ignoreCanceled = true)
    public void playerQuit(DisconnectionHook event) {
        EventHelper.onPlayerQuit(plugin.wrapPlayer(event.getPlayer()));
    }

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

    @HookHandler(ignoreCanceled = true)
    public void redstoneChange(RedstoneChangeHook hook) {
        World world = plugin.getWorld(hook.getSourceBlock().getWorld().getName());
        Block block = new CanaryBlock(world, hook.getSourceBlock());

        boolean result = EventHelper.onRedstoneChange(block, hook.getOldLevel(), hook.getNewLevel());

        if (result) {
            hook.setCanceled();
        }
    }

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
        List<Block> affected = new ArrayList<Block>();

        for (net.canarymod.api.world.blocks.Block block : hook.getAffectedBlocks()) {
            affected.add(world.getBlockAt(block.getX(), block.getY(), block.getZ()));
        }

        boolean result = EventHelper.onExplosion(type, affected);

        if (result) {
            hook.setCanceled();
        }
    }

    @HookHandler(ignoreCanceled = true)
    public void onPistonExtend(PistonExtendHook hook) {
        World world = plugin.getWorld(hook.getPiston().getWorld().getName());
        Block block = new CanaryBlock(world, hook.getPiston());

        if (EventHelper.onPistonExtend(block, plugin.castLocation(hook.getMoving().getLocation()))) {
            hook.setCanceled();
        }
    }

    @HookHandler(ignoreCanceled = true)
    public void onPistonRetract(PistonRetractHook hook) {
        World world = plugin.getWorld(hook.getPiston().getWorld().getName());
        Block block = new CanaryBlock(world, hook.getPiston());

        if (EventHelper.onPistonExtend(block, plugin.castLocation(hook.getMoving().getLocation()))) {
            hook.setCanceled();
        }
    }

}
