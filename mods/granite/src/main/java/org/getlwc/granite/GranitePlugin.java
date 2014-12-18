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
package org.getlwc.granite;

import org.getlwc.Block;
import org.getlwc.Engine;
import org.getlwc.ServerInfo;
import org.getlwc.ServerLayer;
import org.getlwc.SimpleEngine;
import org.getlwc.Version;
import org.getlwc.command.CommandSender;
import org.getlwc.entity.Player;
import org.getlwc.event.server.ServerStartingEvent;
import org.getlwc.event.server.ServerStoppingEvent;
import org.getlwc.granite.experimental.GraniteCommandComposite;
import org.getlwc.granite.listeners.GraniteListener;
import org.getlwc.granite.permission.GranitePermissionHandler;
import org.getlwc.granite.world.GraniteBlock;
import org.getlwc.util.registry.FallbackMinecraftRegistry;
import org.granitemc.granite.api.plugin.OnDisable;
import org.granitemc.granite.api.plugin.OnEnable;
import org.granitemc.granite.api.plugin.Plugin;
import org.granitemc.granite.api.plugin.PluginContainer;
import org.granitemc.granite.reflect.GraniteServerComposite;

@Plugin(id = Version.PLUGIN_ID,  name = Version.PLUGIN_NAME, version = Version.PLUGIN_VERSION)
public class GranitePlugin {

    private SimpleEngine engine;
    private ServerLayer layer;

    @SuppressWarnings("unused")
    @OnEnable
    public void onEnable(PluginContainer container) {
        layer = new GraniteServerLayer(container);
        ServerInfo serverInfo = new GraniteServerInfo();

        engine = (SimpleEngine) SimpleEngine.getOrCreateEngine(layer, serverInfo, new FallbackMinecraftRegistry(), new GraniteConsoleCommandSender());
        engine.setPermissionHandler(new GranitePermissionHandler());
        engine.getEventBus().post(new ServerStartingEvent());

        container.registerEventHandler(new GraniteListener(this));

        // experimental: command injection (using @Command is more annoying)
        // plugin's are enabled in the GraniteServerComposite ctor so Granite.getServer()
        // will be null! (hence the thread that just waits, lol)
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (GraniteServerComposite.instance == null) {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                if (GraniteServerComposite.instance == null) {
                    engine.getConsoleSender().sendMessage("Failed to inject command handler");
                } else {
                    GraniteCommandComposite commandComposite = new GraniteCommandComposite(GranitePlugin.this);
                    commandComposite.injectComposite();
                    engine.getConsoleSender().sendMessage("Injected command handler successfully");
                }
            }
        }).start();
    }

    @SuppressWarnings("unused")
    @OnDisable
    public void onDisable(PluginContainer container) {
        engine.getEventBus().post(new ServerStoppingEvent());
        engine = null;
    }

    /**
     * Returns the engine this plugin is running on
     *
     * @return
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * Wraps a native command sender around the internal representation
     *
     * @param sender
     * @return
     */
    public CommandSender wrapCommandSender(org.granitemc.granite.api.command.CommandSender sender) {
        if (sender instanceof org.granitemc.granite.api.entity.player.Player) {
            return wrapPlayer((org.granitemc.granite.api.entity.player.Player) sender);
        } else {
            return engine.getConsoleSender();
        }
    }

    /**
     * Wraps a native player around the internal representation
     *
     * @param handle
     * @return
     */
    public Player wrapPlayer(org.granitemc.granite.api.entity.player.Player handle) {
        return layer.getPlayer(handle.getName());
    }

    /**
     * Wraps a native block around the internal representation
     * @param handle
     * @return
     */
    public Block wrapBlock(org.granitemc.granite.api.block.Block handle) {
        // TODO this is to work around worlds not being easily accessible via the api
        // so objects are just directly mapped instead of indirectly (the usual way)
        return new GraniteBlock(handle);
    }

}
