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
package org.getlwc.bukkit;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import org.bukkit.Bukkit;
import org.getlwc.ServerLayer;
import org.getlwc.command.ConsoleCommandSender;
import org.getlwc.configuration.Configuration;
import org.getlwc.configuration.ConfigurationLoaderRegistry;
import org.getlwc.configuration.files.EngineConfiguration;

import javax.inject.Provider;
import java.io.File;

public class BukkitEngineGuiceModule extends AbstractModule {

    private BukkitPlugin plugin;

    public BukkitEngineGuiceModule(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(ServerLayer.class).to(BukkitServerLayer.class);
        bind(ConsoleCommandSender.class).to(BukkitConsoleCommandSender.class);
        bind(EngineConfiguration.class).toProvider(BukkitEngineConfigurationProvider.class);
    }

    @Provides
    public BukkitPlugin providePlugin() {
        return plugin;
    }

    @Provides
    public org.bukkit.command.ConsoleCommandSender provideConsoleCommandSender() {
        return Bukkit.getServer().getConsoleSender();
    }

    /**
     * Default config provider that defaults to yaml
     */
    private static class BukkitEngineConfigurationProvider implements Provider<EngineConfiguration> {

        @Inject
        private ConfigurationLoaderRegistry registry;

        // TODO replace with @ConfigDir?
        @Inject
        @Deprecated
        private ServerLayer serverLayer;

        @Override
        public EngineConfiguration get() {
            File configDir = serverLayer.getDataFolder();
            Configuration config = registry.load(new File(configDir, "config.yml"));
            return new EngineConfiguration(config);
        }

    }

}
