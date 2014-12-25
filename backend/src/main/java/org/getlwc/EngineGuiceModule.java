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
package org.getlwc;

import com.google.inject.AbstractModule;
import org.getlwc.command.CommandHandler;
import org.getlwc.command.SimpleCommandHandler;
import org.getlwc.configuration.ConfigurationLoaderRegistry;
import org.getlwc.configuration.SimpleConfigurationLoaderRegistry;
import org.getlwc.event.EventBus;
import org.getlwc.event.SimpleEventBus;
import org.getlwc.util.registry.FallbackMinecraftRegistry;
import org.getlwc.util.registry.MinecraftRegistry;
import org.getlwc.util.resource.ResourceDownloader;
import org.getlwc.util.resource.SimpleResourceDownloader;

/**
 * The main Engine Guice module. This provides sane default binds for the engine,
 * except for {@link org.getlwc.ServerLayer} and {@link org.getlwc.command.ConsoleCommandSender}
 */
public class EngineGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Engine.class).to(SimpleEngine.class);
        bind(EventBus.class).to(SimpleEventBus.class);
        bind(MinecraftRegistry.class).to(FallbackMinecraftRegistry.class);
        bind(ProtectionManager.class).to(SimpleProtectionManager.class);
        bind(CommandHandler.class).to(SimpleCommandHandler.class);
        bind(ResourceDownloader.class).to(SimpleResourceDownloader.class);
        bind(ConfigurationLoaderRegistry.class).to(SimpleConfigurationLoaderRegistry.class);
    }

}
