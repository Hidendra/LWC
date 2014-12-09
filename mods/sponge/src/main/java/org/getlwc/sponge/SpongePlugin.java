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
package org.getlwc.sponge;

import org.getlwc.Block;
import org.getlwc.Engine;
import org.getlwc.ItemStack;
import org.getlwc.ServerInfo;
import org.getlwc.ServerLayer;
import org.getlwc.SimpleEngine;
import org.getlwc.entity.Player;
import org.getlwc.lang.Locale;
import org.getlwc.sponge.listeners.SpongeEventListener;
import org.getlwc.sponge.permission.SpongePermissionHandler;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.state.ServerStartingEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.util.event.Subscribe;

import java.util.HashMap;

@Plugin(id = "lwc", name = "LWC", version = "5.0.0-SNAPSHOT")
public class SpongePlugin {

    private SimpleEngine engine;
    private ServerLayer layer;
    private Game game;

    @SuppressWarnings("unused")
    @Subscribe
    public void onStartup(ServerStartingEvent event) {
        game = event.getGame();
        layer = new SpongeServerLayer(this, game);
        ServerInfo serverInfo = new SpongeServerInfo(game);

        engine = (SimpleEngine) SimpleEngine.getOrCreateEngine(layer, serverInfo, new SpongeConsoleCommandSender());
        engine.setPermissionHandler(new SpongePermissionHandler());
        engine.getEventBus().subscribe(new EngineEventListener(engine, this));
        engine.getEventBus().post(new org.getlwc.event.server.ServerStartingEvent());

        game.getEventManager().register(this, new SpongeEventListener(this));
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
        Player res = layer.getPlayer(player.getName());

        if (!res.getLocale().getName().equalsIgnoreCase(player.getLocale().toString())) {
            res.setLocale(new Locale(player.getLocale().toString()));
            engine.getConsoleSender().sendMessage("Player " + res.getName() + " loaded using locale: " + res.getLocale().toString());
        }

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
        return game;
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
