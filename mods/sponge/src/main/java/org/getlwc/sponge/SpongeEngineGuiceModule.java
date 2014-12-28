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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import org.getlwc.ServerLayer;
import org.getlwc.command.ConsoleCommandSender;
import org.getlwc.util.registry.MinecraftRegistry;
import org.slf4j.Logger;
import org.spongepowered.api.Game;

import javax.inject.Inject;
import javax.inject.Provider;

public class SpongeEngineGuiceModule extends AbstractModule {

    private SpongePlugin plugin;
    private Game game;

    public SpongeEngineGuiceModule(SpongePlugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    @Override
    protected void configure() {
        bind(ServerLayer.class).to(SpongeServerLayer.class);
        bind(ConsoleCommandSender.class).toProvider(SpongePluginLogProvider.class).in(Scopes.SINGLETON);
        bind(MinecraftRegistry.class).to(SpongeMinecraftRegistry.class);
    }

    @Provides
    public SpongePlugin provideSpongePlugin() {
        return plugin;
    }

    @Provides
    public Game provideGame() {
        return game;
    }

    private static class SpongePluginLogProvider implements Provider<ConsoleCommandSender> {

        private SpongePlugin plugin;

        @Inject
        public SpongePluginLogProvider(SpongePlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public ConsoleCommandSender get() {
            return new SpongeConsoleCommandSender(plugin.getLogger());
        }

    }

}
