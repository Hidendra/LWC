package org.getlwc.sponge;

import org.getlwc.Block;
import org.getlwc.Engine;
import org.getlwc.ItemStack;
import org.getlwc.ServerInfo;
import org.getlwc.ServerLayer;
import org.getlwc.SimpleEngine;
import org.getlwc.entity.Player;
import org.getlwc.sponge.listeners.SpongeEventListener;
import org.getlwc.sponge.permission.SpongePermissionHandler;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.state.ServerStartingEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.util.event.Subscribe;

import java.util.HashMap;

@Plugin(id = "lwc", name = "LWC", version = "0.0.1-SNAPSHOT")
public class SpongePlugin {

    private SimpleEngine engine;
    private ServerLayer layer;

    @SuppressWarnings("unused")
    @Subscribe
    public void onStartup(ServerStartingEvent event) {
        layer = new SpongeServerLayer(this, event.getGame());
        ServerInfo serverInfo = new SpongeServerInfo(event.getGame());

        engine = (SimpleEngine) SimpleEngine.getOrCreateEngine(layer, serverInfo, new SpongeConsoleCommandSender());
        engine.setPermissionHandler(new SpongePermissionHandler());
        engine.getEventBus().subscribe(new EngineEventListener(engine, this));
        engine.getEventBus().post(new org.getlwc.event.server.ServerStartingEvent());

        event.getGame().getEventManager().register(this, new SpongeEventListener(this));
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onShutdown(ServerStoppingEvent event) {
        engine.getEventBus().post(new org.getlwc.event.server.ServerStoppingEvent());
        engine = null;
    }

    /**
     * Wrap a player object to a native version we can work with
     *
     * @param player
     * @return
     */
    public Player wrapPlayer(org.spongepowered.api.entity.player.Player player) {
        return layer.getPlayer(player.getName());
    }

    /**
     * Wraps the given sponge block
     *
     * @param block
     * @return
     */
    public Block wrapBlock(org.spongepowered.api.block.BlockLoc block) {
        //
        return null;
    }

    /**
     * Returns the engine being used for this plugin
     *
     * @return
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * Get the game this plugin is using
     *
     * @return
     */
    public Game getGame() {
        return null; // TODO
    }

    /**
     * Cast an item stack to the native ItemStack
     *
     * @param item
     * @return
     */
    public ItemStack castItemStack(org.spongepowered.api.item.inventory.ItemStack item) {
        if (item == null) {
            return null;
        }

        // TODO no integer ID available; global change to move towards string ids and not just in MaterialRegistry should be done
        // TODO no enchantment support in the API yet
        return new ItemStack(-1, item.getQuantity(), item.getDamage(), item.getMaxStackQuantity(), new HashMap<Integer, Integer>());
    }

}
