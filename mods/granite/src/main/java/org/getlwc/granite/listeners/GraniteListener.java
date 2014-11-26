package org.getlwc.granite.listeners;

import org.getlwc.Block;
import org.getlwc.EventHelper;
import org.getlwc.entity.Player;
import org.getlwc.granite.GranitePlugin;
import org.granitemc.granite.api.event.EventHandler;
import org.granitemc.granite.api.event.block.EventBlockBreak;
import org.granitemc.granite.api.event.block.EventBlockPlace;
import org.granitemc.granite.api.event.player.EventPlayerInteract;
import org.granitemc.granite.api.event.player.EventPlayerJoin;
import org.granitemc.granite.api.event.player.EventPlayerQuit;

public class GraniteListener {

    private GranitePlugin plugin;

    public GraniteListener(GranitePlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerJoin(EventPlayerJoin event) {
        if (event.isCancelled()) {
            return;
        }

        EventHelper.onPlayerJoin(plugin.wrapPlayer(event.getPlayer()));
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerQuit(EventPlayerQuit event) {
        if (event.isCancelled()) {
            return;
        }

        EventHelper.onPlayerQuit(plugin.wrapPlayer(event.getPlayer()));
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerInteract(EventPlayerInteract event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = plugin.wrapPlayer(event.getPlayer());
        Block block = plugin.wrapBlock(event.getBlock());

        boolean result = EventHelper.onBlockInteract(player, block);

        if (result) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onBlockPlace(EventBlockPlace event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = plugin.wrapPlayer(event.getPlayer());
        Block block = plugin.wrapBlock(event.getBlock());

        boolean result = EventHelper.onBlockPlace(player, block);

        if (result) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onBlockBreak(EventBlockBreak event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = plugin.wrapPlayer(event.getPlayer());
        Block block = plugin.wrapBlock(event.getBlock());

        boolean result = EventHelper.onBlockPlace(player, block);

        if (result) {
            event.setCancelled(true);
        }
    }

}
