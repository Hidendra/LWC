package org.getlwc.granite;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.getlwc.ServerLayer;
import org.getlwc.command.ConsoleCommandSender;
import org.granitemc.granite.api.plugin.PluginContainer;

public class GraniteEngineGuiceModule extends AbstractModule {

    private PluginContainer pluginContainer;

    public GraniteEngineGuiceModule(PluginContainer pluginContainer) {
        this.pluginContainer = pluginContainer;
    }

    @Override
    protected void configure() {
        bind(ServerLayer.class).to(GraniteServerLayer.class);
        bind(ConsoleCommandSender.class).to(GraniteConsoleCommandSender.class);
    }

    @Provides
    public PluginContainer providePluginContainer() {
        return pluginContainer;
    }

}
