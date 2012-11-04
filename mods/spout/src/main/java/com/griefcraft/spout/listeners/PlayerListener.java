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

package com.griefcraft.spout.listeners;

import com.griefcraft.entity.Player;
import com.griefcraft.spout.SpoutPlugin;
import com.griefcraft.spout.world.SpoutBlock;
import com.griefcraft.world.Block;
import com.griefcraft.world.World;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.player.PlayerInteractEvent;
import org.spout.api.geo.discrete.Point;

public class PlayerListener implements Listener {

    /**
     * The plugin object
     */
    private SpoutPlugin plugin;

    public PlayerListener(SpoutPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler()
    public void playerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = plugin.wrapPlayer(event.getPlayer());
        World world = plugin.getWorld(event.getPlayer().getWorld().getName());
        Point point = event.getInteractedPoint();
        Block block = new SpoutBlock(world, event.getPlayer().getWorld().getBlock(point.getBlockX(), point.getBlockY(), point.getBlockZ()));

        // send the event for the player around the plugin (and maybe other plugins, too.)
        boolean result = player.getEventDelegate().onPlayerInteract(block);

        // cancel it if need be
        if (result) {
            event.setCancelled(true);
        }
    }

}
