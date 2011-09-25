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

import com.griefcraft.bukkit.MockPlayer;
import com.griefcraft.bukkit.MockServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.Test;

import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.junit.Assert.assertEquals;

/**
 * WORLD NAMES UTILIZED
 * - main
 * - nether
 */
public class LWCTest {

    /**
     * mock Bukkit server
     */
    private static MockServer server = null;
    private static Logger logger = Logger.getLogger("TestSuite");

    public LWCTest() {
        if (LWCTest.server == null) {
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter() {
                @Override
                public synchronized String format(LogRecord arg0) {
                    return arg0.getMessage();
                }
            });
            Logger global = Logger.getLogger("");
            global.addHandler(consoleHandler);

            LWCTest.server = new MockServer();
        }

        server.softReset();
    }

    @Test
    public void CheckLWC() {
        // assertEquals(getPlugin("LWC").isEnabled(), true);
    }

    @Test
    public void CheckPermissions() {
        // assertEquals(getPlugin("Permissions").isEnabled(), true);
    }

    @Test
    public void CreateHidendra() {
        Player player = new MockPlayer("Hidendra");
        server.addPlayer(player);

        assertEquals(server.getOnlinePlayers().length, 1);
    }

    @Test
    public void Case1() {

    }

    /**
     * @param pluginName The plugin to retrieve
     * @return the plugin object
     */
    private Plugin getPlugin(String pluginName) {
        Plugin plugin = server.getPluginManager().getPlugin(pluginName);

        if (plugin == null) {
            return null;
        }

        return plugin;
    }

}
