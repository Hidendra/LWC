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

        boolean result = EventHelper.onBlockBreak(player, block);

        if (result) {
            event.setCancelled(true);
        }
    }

}
