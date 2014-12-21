package org.getlwc.bukkit;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.bukkit.Bukkit;
import org.getlwc.ServerLayer;
import org.getlwc.command.ConsoleCommandSender;

public class BukkitEngineGuiceModule extends AbstractModule {

    private BukkitPlugin plugin;

    public BukkitEngineGuiceModule(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(ServerLayer.class).to(BukkitServerLayer.class);
        bind(ConsoleCommandSender.class).to(BukkitConsoleCommandSender.class);
    }

    @Provides
    public BukkitPlugin providePlugin() {
        return plugin;
    }

    @Provides
    public org.bukkit.command.ConsoleCommandSender provideConsoleCommandSender() {
        return Bukkit.getServer().getConsoleSender();
    }

}
