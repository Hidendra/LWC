package org.getlwc.sponge;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.getlwc.ServerLayer;
import org.getlwc.command.ConsoleCommandSender;
import org.getlwc.util.registry.MinecraftRegistry;
import org.spongepowered.api.Game;

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
        bind(ConsoleCommandSender.class).to(SpongeConsoleCommandSender.class);
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

}
