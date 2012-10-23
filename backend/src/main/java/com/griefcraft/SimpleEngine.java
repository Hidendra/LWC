/*
 * Copyright (c) 2011, 2012, Tyler Blair
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

package com.griefcraft;

import com.griefcraft.command.CommandException;
import com.griefcraft.command.CommandHandler;
import com.griefcraft.command.ConsoleCommandSender;
import com.griefcraft.command.SimpleCommandHandler;
import com.griefcraft.commands.BaseCommands;
import com.griefcraft.commands.BenchmarkCommands;
import com.griefcraft.configuration.Configuration;
import com.griefcraft.internal.SimpleProtectionManager;
import com.griefcraft.roles.PlayerRoleDefinition;
import com.griefcraft.sql.Database;
import com.griefcraft.sql.DatabaseException;
import com.griefcraft.sql.JDBCDatabase;

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

    private SimpleEngine(ServerLayer serverLayer, ServerInfo serverInfo, ConsoleCommandSender consoleSender, Configuration configuration) {
        this.serverLayer = serverLayer;
        this.serverInfo = serverInfo;
        this.consoleSender = consoleSender;
        this.configuration = configuration;
        this.commandHandler = new SimpleCommandHandler();

        consoleSender.sendMessage("Server: " + serverInfo.getServerMod() + " [" + serverInfo.getServerVersion() + "]");
        consoleSender.sendMessage("Layer: " + serverInfo.getLayerVersion());
        consoleSender.sendMessage("Backend: " + getBackendVersion());

        // connect to the db
        openDatabase();

        // Register any commands
        registerCommands();

        // register default roles
        registerDefaultRoles();
    }

    /**
     * Create an LWC engine using SimpleLWC
     *
     * @param configuration
     * @return
     */
    public static Engine createEngine(ServerLayer serverLayer, ServerInfo serverInfo, ConsoleCommandSender consoleSender, Configuration configuration) {
        if (serverLayer == null) {
            throw new IllegalArgumentException("Server layer object cannot be null");
        }
        if (serverInfo == null) {
            throw new IllegalArgumentException("Server info object cannot be null");
        }
        if (consoleSender == null) {
            throw new IllegalArgumentException("Console sender object cannot be null");
        }
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration object cannot be null");
        }

        return new SimpleEngine(serverLayer, serverInfo, consoleSender, configuration);
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }

    public ServerLayer getServerLayer() {
        return serverLayer;
    }

    public String getBackendVersion() {
        return "v5-volatile-test (Java)";
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public ConsoleCommandSender getConsoleSender() {
        return consoleSender;
    }

    public Database getDatabase() {
        return database;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Open and connect to the database
     */
    private void openDatabase() {
        JDBCDatabase.JDBCConnectionDetails details = new JDBCDatabase.JDBCConnectionDetails(
                JDBCDatabase.Driver.MYSQL,
                "127.0.0.1",
                "minecraft",
                "",
                "root",
                ""
        );

        // Open the database
        database = new JDBCDatabase(this, details);

        boolean result;

        // attempt to connect to the database
        try {
            result = database.connect();
        } catch (DatabaseException e) {
            result = false;
            e.printStackTrace();
        }

        if (result) {
            consoleSender.sendMessage("Connected to the database (" + details.getDriver() + ")");
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

}
