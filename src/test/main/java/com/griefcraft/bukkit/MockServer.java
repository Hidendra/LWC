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

package com.griefcraft.bukkit;

import com.avaje.ebean.config.ServerConfig;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.scheduler.CraftScheduler;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mock server for Bukkit
 */
public class MockServer implements Server {

    // Server constants
    public static final String NAME = "MockServer";
    public static final String SERVER_NAME = "Hidendra's Bastion";
    public static final String SERVER_ID = "";
    public static final String SERVER_IP = "";
    public static final String SERVER_VERSION = "";
    public static final String UPDATE_FOLDER = "";
    public static final int MAX_PLAYERS = 64;
    public static final int SERVER_PORT = 5555; // doesn't run a server, just aesthetic
    public static final int SPAWN_RADIUS = 15;
    public static final boolean ONLINE_MODE = false;

    private Logger logger = Logger.getLogger("MockServer");

    /**
     * The players online
     */
    private List<Player> players;

    /**
     * The currently loaded worlds
     */
    private Map<String, World> worlds;

    /**
     * The plugin manager
     */
    private PluginManager pluginManager;

    /**
     * The scheduler
     */
    private BukkitScheduler scheduler;

    /**
     * The plugin command map
     */
    private CommandMap commandMap;

    public MockServer() {
        // bind the mock server to Bukkit
        Bukkit.setServer(this);

        logger.info("Loading MockServer");

        players = new ArrayList<Player>();
        worlds = new LinkedHashMap<String, World>();
        commandMap = new SimpleCommandMap(this);
        pluginManager = new SimplePluginManager(this, (SimpleCommandMap) commandMap);
        scheduler = new CraftScheduler(null);
        loadPlugins();
        enablePlugins(PluginLoadOrder.STARTUP);

        // create 1 world
        World world = new MockWorld();
        worlds.put("main", world);

        enablePlugins(PluginLoadOrder.POSTWORLD);
    }

    /**
     * Clear out worlds, players, and so on
     */
    public void softReset() {
        players = new ArrayList<Player>();
        worlds = new LinkedHashMap<String, World>();
    }

    /**
     * Debug a method call
     *
     * @param method
     * @param bind
     */
    private void methodCalled(String method, Object... bind) {
        logger.info("CALL -> " + String.format(method, bind));
    }

    /**
     * Add a player
     *
     * @param player
     */
    public void addPlayer(Player player) {
        players.add(player);
    }

    public World getWorld(String arg0) {
        methodCalled("getWorld(%s)", arg0);

        return worlds.get(arg0);
    }

    public World getWorld(UUID uuid) {

        return null;
    }

    public List<World> getWorlds() {
        methodCalled("getWorlds()");

        return new ArrayList<World>(worlds.values());
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public BukkitScheduler getScheduler() {
        return scheduler;
    }

    public ServicesManager getServicesManager() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean addRecipe(Recipe arg0) {
        methodCalled("addRecipe(%s)", arg0.toString());
        return false;
    }

    public void configureDbConfig(ServerConfig arg0) {
        methodCalled("configureDbConfig(%s)", arg0.toString());
    }

    public Player[] getOnlinePlayers() {
        methodCalled("getOnlinePlayers()");
        return players.toArray(new Player[players.size()]);
    }

    public Player getPlayer(String arg0) {
        methodCalled("getPlayer(%s)", arg0);

        for (Player player : players) {
            if (player.getName().equals(arg0)) {
                return player;
            }
        }

        return null;
    }

    // borrowed from Bukkit
    public List<Player> matchPlayer(String arg0) {
        methodCalled("matchPlayer(%s)", arg0);

        List<Player> matchedPlayers = new ArrayList<Player>();

        for (Player iterPlayer : this.getOnlinePlayers()) {
            String iterPlayerName = iterPlayer.getName();

            if (arg0.equalsIgnoreCase(iterPlayerName)) {
                // Exact match
                matchedPlayers.clear();
                matchedPlayers.add(iterPlayer);
                break;
            }
            if (iterPlayerName.toLowerCase().indexOf(arg0.toLowerCase()) != -1) {
                // Partial match
                matchedPlayers.add(iterPlayer);
            }
        }

        return matchedPlayers;
    }

    public Logger getLogger() {
        return logger;
    }

    public String getIp() {
        return SERVER_IP;
    }

    public String getUpdateFolder() {
        return UPDATE_FOLDER;
    }

    public String getServerName() {
        return SERVER_NAME;
    }

    public int getMaxPlayers() {
        return MAX_PLAYERS;
    }

    public int getPort() {
        return SERVER_PORT;
    }

    public String getServerId() {
        return SERVER_ID;
    }

    public String getVersion() {
        return SERVER_VERSION;
    }

    public String getName() {
        return NAME;
    }

    public int broadcastMessage(String arg0) {
        methodCalled("broadcastMessage(%s)", arg0);
        return 0;
    }

    public World createWorld(String arg0, Environment arg1) {
        methodCalled("createWorld(%s, %s)", arg0, arg1.toString());
        return null;
    }

    public World createWorld(String arg0, Environment arg1, long arg2) {
        methodCalled("createWorld(%s, %s, %d)", arg0, arg1.toString(), arg2);
        return null;
    }

    public boolean dispatchCommand(CommandSender arg0, String arg1) {
        methodCalled("dispatchCommand(%s, %s)", arg0.toString(), arg1);
        return false;
    }

    public PluginCommand getPluginCommand(String arg0) {
        methodCalled("getPluginCommand(%s)", arg0);
        return null;
    }

    public boolean unloadWorld(String arg0, boolean arg1) {
        methodCalled("unloadWorld(%s, %s)", arg0, Boolean.toString(arg1));
        return false;
    }

    public boolean unloadWorld(World arg0, boolean arg1) {
        methodCalled("unloadWorld(%s, %s)", arg0.toString(), Boolean.toString(arg1));
        return false;
    }

    public World createWorld(String arg0, Environment arg1, ChunkGenerator arg2) {
        methodCalled("createWorld(%s, %s, %s)", arg0, arg1.toString(), arg2.toString());
        return null;
    }

    public World createWorld(String arg0, Environment arg1, long arg2, ChunkGenerator arg3) {
        methodCalled("createWorld(%s, %s, %d, %s)", arg0, arg1.toString(), arg2, arg3.toString());
        return null;
    }

    public void reload() {
        methodCalled("reload()");
    }

    public void savePlayers() {
        methodCalled("savePlayers()");
    }

    @Override
    public Map<String, String[]> getCommandAliases() {
        methodCalled("getCommandAliases()");
        return null;
    }

    @Override
    public boolean getOnlineMode() {
        methodCalled("getOnlineMode()");
        return ONLINE_MODE;
    }

    @Override
    public int getSpawnRadius() {
        methodCalled("getSpawnRadius()");
        return SPAWN_RADIUS;
    }

    @Override
    public void setSpawnRadius(int arg0) {
        methodCalled("setSpawnRadius(%d)", arg0);
    }

    /**
     * Load all plugins
     */
    public void loadPlugins() {
        methodCalled("loadPlugins()");

        pluginManager.registerInterface(JavaPluginLoader.class);

        File pluginFolder = new File("plugins");

        if (pluginFolder.exists()) {
            try {
                Plugin[] plugins = pluginManager.loadPlugins(pluginFolder);
                for (Plugin plugin : plugins) {
                    try {
                        plugin.onLoad();
                    } catch (Throwable ex) {
                        Logger.getLogger(CraftServer.class.getName()).log(Level.SEVERE, ex.getMessage() + " initializing " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
                    }
                }
            } catch (Throwable ex) {
                Logger.getLogger(CraftServer.class.getName()).log(Level.SEVERE, ex.getMessage() + " (Is it up to date?)", ex);
            }
        } else {
            pluginFolder.mkdir();
        }
    }

    /**
     * Enable all plugins
     *
     * @param type
     */
    public void enablePlugins(PluginLoadOrder type) {
        Plugin[] plugins = pluginManager.getPlugins();

        for (Plugin plugin : plugins) {
            if ((!plugin.isEnabled()) && (plugin.getDescription().getLoad() == type)) {
                loadPlugin(plugin);
            }
        }

        if (type == PluginLoadOrder.POSTWORLD) {
            // ((SimpleCommandMap) commandMap).registerServerAliases();
        }
    }

    /**
     * Load a plugin
     *
     * @param plugin
     */
    private void loadPlugin(Plugin plugin) {
        try {
            pluginManager.enablePlugin(plugin);
        } catch (Throwable ex) {
            Logger.getLogger(CraftServer.class.getName()).log(Level.SEVERE, ex.getMessage() + " loading " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }
    }

}
