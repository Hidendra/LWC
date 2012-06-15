/*
 * Copyright 2011 Tyler Blair. All rights reserved.
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
import com.griefcraft.configuration.Configuration;

import java.util.logging.Logger;

public class SimpleLWC implements LWC {
    private Logger logger = Logger.getLogger("LWC");

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
     * The configuration file to use
     */
    private Configuration configuration;

    private SimpleLWC(ServerInfo serverInfo, ConsoleCommandSender consoleSender, Configuration configuration) {
        this.serverInfo = serverInfo;
        this.consoleSender = consoleSender;
        this.configuration = configuration;
        this.commandHandler = new SimpleCommandHandler();

        // Register any commands
        registerCommands();
    }

    /**
     * Create an LWC object using SimpleLWC
     *
     * @param configuration
     * @return
     */
    public static LWC createLWC(ServerInfo serverInfo, ConsoleCommandSender consoleSender, Configuration configuration) {
        if (consoleSender == null) {
            throw new IllegalArgumentException("Console sender object cannot be null");
        }
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration object cannot be null");
        }

        return new SimpleLWC(serverInfo, consoleSender, configuration);
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

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Register the commands we want to use
     */
    private void registerCommands() {
        try {
            commandHandler.registerCommands(new BaseCommands());
        } catch (CommandException e) {
            e.printStackTrace();
        }
    }

}
