/**
 * This file is part of LWC (https://github.com/Hidendra/LWC)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.griefcraft.bukkit;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.scheduler.CraftScheduler;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitScheduler;

import com.avaje.ebean.config.ServerConfig;

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
	
	private Logger logger = Logger.getLogger("MockServer");
	
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
	
	public MockServer() {
		// bind the mock server to Bukkit
		Bukkit.setServer(this);
		
		worlds = new LinkedHashMap<String, World>();
		pluginManager = new SimplePluginManager(this);
		scheduler = new CraftScheduler(null);
		loadPlugins();
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
	 * Load all plugins
	 */
	public void loadPlugins() {
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

	public World getWorld(String arg0) {
		methodCalled("getWorld(%s)", arg0);
		
		return worlds.get(arg0);
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
		return null;
	}

	public Player getPlayer(String arg0) {
		methodCalled("getPlayer(%s)", arg0);
		return null;
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

	public List<Player> matchPlayer(String arg0) {
		methodCalled("matchPlayer(%s)", arg0);
		return null;
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

	
}
