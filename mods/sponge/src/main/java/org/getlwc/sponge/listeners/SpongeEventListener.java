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
