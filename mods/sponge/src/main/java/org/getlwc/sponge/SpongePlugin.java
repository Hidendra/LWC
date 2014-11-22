package org.getlwc.sponge;

import org.getlwc.Engine;
import org.getlwc.ItemStack;
import org.getlwc.ServerInfo;
import org.getlwc.ServerLayer;
import org.getlwc.SimpleEngine;
import org.getlwc.entity.Player;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.ServerStartingEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.util.Owner;

import java.util.HashMap;

@Plugin(id = "lwc", name = "LWC", version = "0.0.1-SNAPSHOT")
public class SpongePlugin implements Owner {

    private SimpleEngine engine;
    private ServerLayer layer;

    @SuppressWarnings("unused")
    @Subscribe
    public void onStartup(ServerStartingEvent event) {
        layer = new SpongeServerLayer(this, event.getGame());
        ServerInfo serverInfo = new SpongeServerInfo(event.getGame());

        engine = (SimpleEngine) SimpleEngine.getOrCreateEngine(layer, serverInfo, new SpongeConsoleCommandSender());
        // TODO Sponge permission handler when it's ready
        engine.getEventBus().post(new org.getlwc.event.server.ServerStartingEvent());
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
    public Player wrapPlayer(org.spongepowered.api.entity.Player player) {
        return layer.getPlayer(player.getName());
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
