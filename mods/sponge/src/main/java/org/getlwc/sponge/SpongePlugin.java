package org.getlwc.sponge;

import org.getlwc.ItemStack;
import org.getlwc.ServerInfo;
import org.getlwc.ServerLayer;
import org.getlwc.SimpleEngine;
import org.spongepowered.api.event.state.ServerStartingEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;
import org.spongepowered.api.plugin.Plugin;

import java.util.HashMap;

@Plugin(id = "lwc", name = "LWC", version = "0.0.1-SNAPSHOT")
public class SpongePlugin {

    private SimpleEngine engine;

    // no annotation in Sponge yet
    @SuppressWarnings("unused")
    // @SpongeEventHandler
    public void onStartup(ServerStartingEvent event) {
        ServerLayer serverLayer = new SpongeServerLayer(event.getGame());
        ServerInfo serverInfo = new SpongeServerInfo(event.getGame());

        engine = (SimpleEngine) SimpleEngine.getOrCreateEngine(serverLayer, serverInfo, new SpongeConsoleCommandSender());
        // TODO Sponge permission handler when it's ready
        engine.startup();
    }

    // no annotation in Sponge yet
    @SuppressWarnings("unused")
    // @SpongeEventHandler
    public void onShutdown(ServerStoppingEvent event) {
        engine.shutdown();
        engine = null;
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
