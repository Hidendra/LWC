/*
 * Copyright (c) 2011-2013 Tyler Blair
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

import org.getlwc.command.AddRemoveCommands;
import org.getlwc.command.BaseCommands;
import org.getlwc.command.BenchmarkCommands;
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
import org.getlwc.event.SimpleEventBus;
import org.getlwc.permission.DefaultPermissionHandler;
import org.getlwc.permission.PermissionHandler;

public class SimpleEngine implements Engine {

    /**
     * The instance of the engine
     */
    private static SimpleEngine instance = null;

    /**
     * The event bus for this engine
     */
    private EventBus eventBus = new SimpleEventBus();

    /**
     * The protection manager
     */
    private SimpleProtectionManager protectionManager;

    /**
     * The {@link ResourceDownloader} responsible for downloading library files
     */
    private final SimpleResourceDownloader downloader = new SimpleResourceDownloader(this);

    /**
     * The server layer
     */
    private ServerLayer serverLayer;

    /**
     * The underlying server's information
     */
    private ServerInfo serverInfo;

    /**
     * The command handler
     */
    private SimpleCommandHandler commandHandler;

    /**
     * The console sender
     */
    private ConsoleCommandSender consoleSender;

    /**
     * The database
     */
    private Database database;

    /**
     * The configuration file to use
     */
    private Configuration configuration;

    /**
     * The languages configuration
     */
    private Configuration languagesConfig;

    /**
     * The economy handler for the server
     */
    private EconomyHandler economyHandler = new DefaultEconomyHandler();

    /**
     * The permission handler for the server
     */
    private PermissionHandler permissionHandler = new DefaultPermissionHandler();

    private SimpleEngine(ServerLayer serverLayer, ServerInfo serverInfo, ConsoleCommandSender consoleSender) {
        this.serverLayer = serverLayer;
        this.serverInfo = serverInfo;
        this.consoleSender = consoleSender;

        serverLayer.getEngineHomeFolder().mkdirs();

        downloader.init();
        downloader.ensureResourceInstalled("gettext");
        downloader.ensureResourceInstalled("snakeyaml");

        System.setProperty("org.sqlite.lib.path", downloader.getNativeLibraryFolder());
        FileConfiguration.init(this);

        configuration = new YamlConfiguration("config.yml");
        languagesConfig = new YamlConfiguration(getClass().getResourceAsStream("/languages.yml"));
        I18n.init(this);

        consoleSender.sendTranslatedMessage("Server: {0} ({1})", serverInfo.getServerImplementationTitle(), serverInfo.getServerImplementationVersion());
        consoleSender.sendTranslatedMessage("Plugin: {0} ({1})", getImplementationTitle(), getImplementationVersion());
    }

    /**
     * Gets the Engine instance. Does not create the engine if it has not been created.
     *
     * @return
     */
    public static SimpleEngine getInstance() {
        return instance;
    }

    /**
     * Create an LWC Engine
     *
     * @param serverLayer
     * @param serverInfo
     * @param consoleSender
     * @return
     */
    public static Engine getOrCreateEngine(ServerLayer serverLayer, ServerInfo serverInfo, ConsoleCommandSender consoleSender) {
        if (instance != null) {
            return instance;
        }

        if (serverLayer == null) {
            throw new IllegalArgumentException("Server layer object cannot be null");
        }
        if (serverInfo == null) {
            throw new IllegalArgumentException("Server info object cannot be null");
        }
        if (consoleSender == null) {
            throw new IllegalArgumentException("Console sender object cannot be null");
        }

        instance = new SimpleEngine(serverLayer, serverInfo, consoleSender);

        return instance;
    }

    /**
     * {@inheritDoc}
     */
    public void startup() {
        commandHandler = new SimpleCommandHandler(this);
        protectionManager = new SimpleProtectionManager(this);

        // connect to the db
        openDatabase();

        // Register any commands
        registerHandlers();

        consoleSender.sendTranslatedMessage("Economy handler: {0}", economyHandler.getName());
        consoleSender.sendTranslatedMessage("Permission handler: {0}", permissionHandler.getName());
    }

    /**
     * {@inheritDoc}
     */
    public ResourceDownloader getResourceDownloader() {
        return downloader;
    }

    /**
     * {@inheritDoc}
     */
    public EconomyHandler getEconomyHandler() {
        return economyHandler;
    }

    /**
     * Set the economy handler that will be used for the server
     *
     * @param economyHandler
     */
    public void setEconomyHandler(EconomyHandler economyHandler) {
        this.economyHandler = economyHandler;
    }

    /**
     * {@inheritDoc}
     */
    public PermissionHandler getPermissionHandler() {
        return permissionHandler;
    }

    /**
     * Set the permission handler that will be used for the server
     *
     * @param permissionHandler
     */
    public void setPermissionHandler(PermissionHandler permissionHandler) {
        this.permissionHandler = permissionHandler;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * {@inheritDoc}
     */
    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }

    /**
     * {@inheritDoc}
     */
    public ServerLayer getServerLayer() {
        return serverLayer;
    }

    /**
     * {@inheritDoc}
     */
    public String getImplementationVersion() {
        return SimpleEngine.class.getPackage().getImplementationVersion();
    }

    /**
     * {@inheritDoc}
     */
    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    @Override
    public String getImplementationTitle() {
        return SimpleEngine.class.getPackage().getImplementationTitle();
    }

    /**
     * {@inheritDoc}
     */
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    /**
     * {@inheritDoc}
     */
    public ConsoleCommandSender getConsoleSender() {
        return consoleSender;
    }

    /**
     * {@inheritDoc}
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * {@inheritDoc}
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Get the config for languages
     *
     * @return
     */
    public Configuration getLanguagesConfiguration() {
        return languagesConfig;
    }

    /**
     * {@inheritDoc}
     */
    public void shutdown() {
        consoleSender.sendTranslatedMessage("Shutting down!");
        commandHandler.clearCommands();
        database.disconnect();
        database = null;
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
                consoleSender.sendTranslatedMessage("Driver \"{0}\" is not supported.", driverName);
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
            consoleSender.sendTranslatedMessage("Connected to the database with driver: {0}", databaseType);
        } else {
            consoleSender.sendTranslatedMessage("Failed to connect to the database!");
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

        eventBus.registerAll(object);
    }

}
