package org.getlwc.sponge.listeners;

import com.google.common.eventbus.Subscribe;
import org.getlwc.Block;
import org.getlwc.EventHelper;
import org.getlwc.entity.Player;
import org.getlwc.sponge.SpongePlugin;
import org.spongepowered.api.event.player.PlayerInteractEvent;
import org.spongepowered.api.event.player.PlayerJoinEvent;
import org.spongepowered.api.event.player.PlayerQuitEvent;

public class SpongeEventListener {

    private SpongePlugin plugin;

    public SpongeEventListener(SpongePlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPlayerJoin(final PlayerJoinEvent event) {
        EventHelper.onPlayerJoin(plugin.wrapPlayer(event.getPlayer()));
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPlayerQuit(PlayerQuitEvent event) {
        EventHelper.onPlayerQuit(plugin.wrapPlayer(event.getPlayer()));
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = plugin.wrapPlayer(event.getPlayer());
        Block block = plugin.wrapBlock(event.getBlock().orNull());

        if (EventHelper.onBlockInteract(player, block)) {
            event.setCancelled(true);
        }
    }

}
