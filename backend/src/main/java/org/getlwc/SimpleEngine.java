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

import org.getlwc.attribute.DescriptionAttributeFactory;
import org.getlwc.attribute.PasswordAttributeFactory;
import org.getlwc.command.CommandException;
import org.getlwc.command.CommandHandler;
import org.getlwc.command.ConsoleCommandSender;
import org.getlwc.command.SimpleCommandHandler;
import org.getlwc.commands.AddRemoveCommands;
import org.getlwc.commands.AttributeCommands;
import org.getlwc.commands.BaseCommands;
import org.getlwc.commands.BenchmarkCommands;
import org.getlwc.configuration.Configuration;
import org.getlwc.configuration.YamlConfiguration;
import org.getlwc.roles.PlayerRoleDefinition;
import org.getlwc.scripting.ModuleManager;
import org.getlwc.scripting.SimpleModuleManager;
import org.getlwc.sql.Database;
import org.getlwc.sql.DatabaseException;
import org.getlwc.sql.JDBCDatabase;
import org.getlwc.sql.MemoryDatabase;
import org.getlwc.util.config.FileConfiguration;

public class SimpleEngine implements Engine {

    /**
     * The protection manager
     */
    private final ProtectionManager protectionManager = new SimpleProtectionManager(this);

    /**
     * The role manager
     */
    private final RoleManager roleManager = new SimpleRoleManager();

    /**
     * The savable background queue
     */
    private final SaveQueue saveQueue = new SaveQueue();

    /**
     * The {@link EventHelper} instance that assists in consolidating event handling
     */
    private final EventHelper eventHelper = new SimpleEventHelper(this);

    /**
     * The {@link LibraryDownloader} responsible for downloading library files
     */
    private final LibraryDownloader downloader = new LibraryDownloader(this);

    /**
     * The global module manager
     */
    private final ModuleManager moduleManager = new SimpleModuleManager(this);

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
    private CommandHandler commandHandler;

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
     * The internal config (engine.yml inside of the jar file)
     */
    private Configuration internalConfig;

    /**
     * The server mod that is powering this engine
     */
    private ServerMod serverMod;

    private SimpleEngine(ServerLayer serverLayer, ServerInfo serverInfo, ConsoleCommandSender consoleSender) {
        this.serverLayer = serverLayer;
        this.serverInfo = serverInfo;
        this.consoleSender = consoleSender;

        downloader.init();
        System.setProperty("org.sqlite.lib.path", downloader.getNativeLibraryFolder());
        FileConfiguration.init(this);

        configuration = new YamlConfiguration("config.yml");
        internalConfig = new YamlConfiguration(getClass().getResourceAsStream("/engine.yml"));
        commandHandler = new SimpleCommandHandler(this);

        // load layer info (ServerMod)
        Configuration layerConfig = new YamlConfiguration(getClass().getResourceAsStream("/layer.yml"));
        serverMod = new ServerMod(layerConfig.getString("name"), layerConfig.getString("author"), layerConfig.getString("version"));

        consoleSender.sendMessage("Server: " + serverMod.getName() + " (" + serverInfo.getServerVersion() + ")");
        consoleSender.sendMessage("Layer: " + serverMod.getVersion() + " (authored by: " + serverMod.getAuthor() + ")");
        consoleSender.sendMessage("Backend: " + getBackendVersion());
        consoleSender.sendMessage("This version was built on: " + internalConfig.get("git.buildTime"));

        // connect to the db
        openDatabase();

        // Register any commands
        registerCommands();

        // register default roles
        registerDefaultRoles();

        // default attributes
        registerDefaultAttributes();

        // load modules
        moduleManager.loadAll();
    }

    /**
     * Create an LWC Engine
     *
     * @param serverLayer
     * @param serverInfo
     * @param consoleSender
     * @return
     */
    public static Engine createEngine(ServerLayer serverLayer, ServerInfo serverInfo, ConsoleCommandSender consoleSender) {
        if (serverLayer == null) {
            throw new IllegalArgumentException("Server layer object cannot be null");
        }
        if (serverInfo == null) {
            throw new IllegalArgumentException("Server info object cannot be null");
        }
        if (consoleSender == null) {
            throw new IllegalArgumentException("Console sender object cannot be null");
        }

        return new SimpleEngine(serverLayer, serverInfo, consoleSender);
    }

    /**
     * {@inheritDoc}
     */
    public ServerMod getServerMod() {
        return serverMod;
    }

    /**
     * {@inheritDoc}
     */
    public RoleManager getRoleManager() {
        return roleManager;
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
    public String getBackendVersion() {
        return internalConfig.getString("version") + " (" + internalConfig.getString("git.describe") + ")";
    }

    /**
     * {@inheritDoc}
     */
    public ServerInfo getServerInfo() {
        return serverInfo;
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
    public SaveQueue getSaveQueue() {
        return saveQueue;
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
     * {@inheritDoc}
     */
    public EventHelper getEventHelper() {
        return eventHelper;
    }

    /**
     * {@inheritDoc}
     */
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    /**
     * {@inheritDoc}
     */
    public void disable() {
        consoleSender.sendMessage("Shutting down!");
        saveQueue.flushAndClose();
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
                consoleSender.sendMessage("UNKNOWN ERROR: \"" + driverName + "\"");
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
            consoleSender.sendMessage("Connected to the database (" + databaseType + ")");
        } else {
            consoleSender.sendMessage("Failed to connect to the database");
        }
    }

    /**
     * Register the commands we want to use
     */
    private void registerCommands() {
        try {
            commandHandler.registerCommands(new BaseCommands(this));
            commandHandler.registerCommands(new AddRemoveCommands(this));
            commandHandler.registerCommands(new AttributeCommands(this));
            commandHandler.registerCommands(new BenchmarkCommands(this));
        } catch (CommandException e) {
            e.printStackTrace();
        }
    }

    /**
     * Register the default roles
     */
    private void registerDefaultRoles() {
        roleManager.registerDefinition(new PlayerRoleDefinition(this));
    }

    private void registerDefaultAttributes() {
        protectionManager.registerAttributeFactory(new DescriptionAttributeFactory(this));
        protectionManager.registerAttributeFactory(new PasswordAttributeFactory(this));
    }

}
