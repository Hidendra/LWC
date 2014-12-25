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

import org.getlwc.configuration.ConfigurationLoader;
import org.getlwc.configuration.ConfigurationLoaderRegistry;
import org.getlwc.configuration.json.JSONConfigurationLoader;
import org.getlwc.content.command.AddRemoveCommands;
import org.getlwc.content.command.BaseCommands;
import org.getlwc.content.command.BenchmarkCommands;
import org.getlwc.command.CommandException;
import org.getlwc.command.CommandHandler;
import org.getlwc.command.ConsoleCommandSender;
import org.getlwc.command.SimpleCommandHandler;
import org.getlwc.configuration.Configuration;
import org.getlwc.configuration.FileConfiguration;
import org.getlwc.configuration.YamlConfiguration;
import org.getlwc.content.DescriptionModule;
import org.getlwc.db.Database;
import org.getlwc.db.DatabaseException;
import org.getlwc.db.jdbc.JDBCDatabase;
import org.getlwc.db.memory.MemoryDatabase;
import org.getlwc.economy.DefaultEconomyHandler;
import org.getlwc.economy.EconomyHandler;
import org.getlwc.event.EventBus;
import org.getlwc.event.Listener;
import org.getlwc.event.server.ServerStartingEvent;
import org.getlwc.event.server.ServerStoppingEvent;
import org.getlwc.permission.DefaultPermissionHandler;
import org.getlwc.permission.PermissionHandler;
import org.getlwc.util.registry.MinecraftRegistry;
import org.getlwc.util.resource.ResourceDownloader;
import org.getlwc.util.resource.SimpleResourceDownloader;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SimpleEngine implements Engine {

    /**
     * The instance of the engine
     */
    @Deprecated
    private static SimpleEngine instance = null;

    /**
     * The event bus for this engine
     */
    private EventBus eventBus;

    /**
     * The protection manager
     */
    @Inject
    private SimpleProtectionManager protectionManager;

    /**
     * The {@link org.getlwc.util.resource.ResourceDownloader} responsible for downloading library files
     */
    @Inject
    private ResourceDownloader downloader;

    /**
     * The server layer
     */
    @Inject
    private ServerLayer serverLayer;

    /**
     * The command handler
     */
    @Inject
    private SimpleCommandHandler commandHandler;

    /**
     * The console sender
     */
    @Inject
    private ConsoleCommandSender consoleSender;

    /**
     * The database
     */
    private Database database;

    /**
     * The configuration file to use
     */
    private YamlConfiguration configuration;

    /**
     * The languages configuration
     */
    private YamlConfiguration languagesConfig;

    /**
     * The minecraft registry
     */
    @Inject
    private MinecraftRegistry minecraftRegistry;

    /**
     * The economy handler for the server
     */
    private EconomyHandler economyHandler = new DefaultEconomyHandler();

    /**
     * The permission handler for the server
     */
    private PermissionHandler permissionHandler = new DefaultPermissionHandler();

    @Inject
    public SimpleEngine(EventBus eventBus, ConfigurationLoaderRegistry configurationRegistry) {
        this.eventBus = eventBus;
        instance = this; // TODO deprecated

        eventBus.subscribe(this);

        // config filetypes supported by all mods
        ConfigurationLoader jsonLoader = new JSONConfigurationLoader();
        configurationRegistry.bind("json", jsonLoader);

        configurationRegistry.bind(ConfigurationLoaderRegistry.DEFAULT_KEY, jsonLoader);
    }

    @SuppressWarnings("unused")
    @Listener
    public void onStartup(ServerStartingEvent event) {
        serverLayer.getDataFolder().mkdirs();

        downloader.ensureResourceInstalled("gettext");
        downloader.ensureResourceInstalled("snakeyaml"); // TODO remove when FileConfiguration is removed.

        FileConfiguration.init(this);

        configuration = new YamlConfiguration("config.yml");
        languagesConfig = new YamlConfiguration(getClass().getResourceAsStream("/languages.yml"));
        I18n.init(this);

        consoleSender.sendMessage("Server: {0} ({1})", serverLayer.getImplementationTitle(), serverLayer.getImplementationVersion());
        consoleSender.sendMessage("Plugin: {0} ({1})", getImplementationTitle(), getImplementationVersion());

        commandHandler = new SimpleCommandHandler(this);
        protectionManager = new SimpleProtectionManager(this);

        // connect to the db
        openDatabase();

        // Register any commands
        registerHandlers();

        consoleSender.sendMessage("Economy handler: {0}", economyHandler.getName());
        consoleSender.sendMessage("Permission handler: {0}", permissionHandler.getName());
    }

    @SuppressWarnings("unused")
    @Listener
    public void onShutdown(ServerStoppingEvent event) {
        consoleSender.sendMessage("Shutting down!");
        commandHandler.clearCommands();
        database.disconnect();
        database = null;
    }

    /**
     * Gets the Engine instance. Does not create the engine if it has not been created.
     *
     * @return
     */
    @Deprecated
    public static SimpleEngine getInstance() {
        return instance;
    }

    @Override
    public ResourceDownloader getResourceDownloader() {
        return downloader;
    }

    @Override
    public EconomyHandler getEconomyHandler() {
        return economyHandler;
    }

    @Override
    public void setEconomyHandler(EconomyHandler economyHandler) {
        this.economyHandler = economyHandler;
    }

    @Override
    public PermissionHandler getPermissionHandler() {
        return permissionHandler;
    }

    @Override
    public void setPermissionHandler(PermissionHandler permissionHandler) {
        this.permissionHandler = permissionHandler;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public MinecraftRegistry getMinecraftRegistry() {
        return minecraftRegistry;
    }

    @Override
    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }

    @Override
    public ServerLayer getServerLayer() {
        return serverLayer;
    }

    @Override
    public String getImplementationVersion() {
        return SimpleEngine.class.getPackage().getImplementationVersion();
    }

    @Override
    public String getImplementationTitle() {
        return SimpleEngine.class.getPackage().getImplementationTitle();
    }

    @Override
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    @Override
    public ConsoleCommandSender getConsoleSender() {
        return consoleSender;
    }

    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public YamlConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Get the config for languages
     *
     * @return
     */
    @Deprecated
    public YamlConfiguration getLanguagesConfiguration() {
        return languagesConfig;
    }

    /**
     * Open and connect to the database
     */
    private void openDatabase() {
        String driverName = configuration.getString("database.driver");
        String databaseType;

        if (driverName.equalsIgnoreCase("memory")) {
            database = new MemoryDatabase(this);
            databaseType = "memory";
        } else {
            JDBCDatabase.Driver driver = JDBCDatabase.Driver.resolveDriver(driverName);

            if (driver == null) {
                consoleSender.sendMessage("Driver \"{0}\" is not supported.", driverName);
                return;
            }

            JDBCDatabase.JDBCConnectionDetails details = new JDBCDatabase.JDBCConnectionDetails(
                    JDBCDatabase.Driver.resolveDriver(configuration.getString("database.driver")),
                    configuration.getString("database.hostname"),
                    configuration.getString("database.database"),
                    configuration.getString("database.databasePath"),
                    configuration.getString("database.prefix"),
                    configuration.getString("database.username"),
                    configuration.getString("database.password")
            );

            // Open the database
            database = new JDBCDatabase(this, details);
            databaseType = details.getDriver().toString();
        }

        boolean result;

        // attempt to connect to the database
        try {
            result = database.connect();
        } catch (DatabaseException e) {
            result = false;
            e.printStackTrace();
        }

        if (result) {
            consoleSender.sendMessage("Connected to the database with driver: {0}", databaseType);
        } else {
            consoleSender.sendMessage("Failed to connect to the database!");
        }
    }

    /**
     * Register the commands we want to use
     */
    private void registerHandlers() {
        registerObjectEvents(new BaseCommands(this));
        registerObjectEvents(new AddRemoveCommands(this));
        registerObjectEvents(new BenchmarkCommands(this));

        registerObjectEvents(new DescriptionModule());
    }

    /**
     * Helper method to register events & commands.
     *
     * @param object
     */
    private void registerObjectEvents(Object object) {
        try {
            commandHandler.registerCommands(object);
        } catch (CommandException e) {
            e.printStackTrace();
        }

        eventBus.subscribe(object);
    }

}
