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

package org.getlwc.bukkit.entity;

import org.getlwc.Engine;
import org.getlwc.Location;
import org.getlwc.bukkit.BukkitPlugin;
import org.getlwc.entity.Player;
import org.getlwc.util.Color;

public class BukkitPlayer extends Player {

    /**
     * The plugin object
     */
    private final BukkitPlugin plugin;

    /**
     * The player handle
     */
    private final org.bukkit.entity.Player handle;

    public BukkitPlayer(Engine engine, BukkitPlugin plugin, org.bukkit.entity.Player handle) {
        if (handle == null) {
            throw new IllegalArgumentException("Player handle cannot be null");
        }

        this.plugin = plugin;
        this.handle = handle;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return handle.getName();
    }

    /**
     * {@inheritDoc}
     */
    public Location getLocation() {
        org.bukkit.Location lhandle = handle.getLocation();
        return new Location(plugin.getWorld(lhandle.getWorld().getName()), lhandle.getX(), lhandle.getY(), lhandle.getZ());
    }

    /**
     * {@inheritDoc}
     */
    public void sendMessage(String message) {
        for (String line : message.split("\n")) {
            handle.sendMessage(Color.replaceColors(line));
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPermission(String node) {
        return handle.hasPermission(node);
    }
}
