package com.griefcraft.lwc;

import java.io.File;

import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import com.griefcraft.commands.Admin;
import com.griefcraft.commands.Create;
import com.griefcraft.commands.Free;
import com.griefcraft.commands.ICommand;
import com.griefcraft.commands.Info;
import com.griefcraft.commands.Modes;
import com.griefcraft.commands.Modify;
import com.griefcraft.commands.Unlock;
import com.griefcraft.listeners.LWCBlockListener;
import com.griefcraft.listeners.LWCEntityListener;
import com.griefcraft.listeners.LWCPlayerListener;
import com.griefcraft.logging.Logger;
import com.griefcraft.util.Config;
import com.griefcraft.util.Updater;

public class LWCPlugin extends JavaPlugin {

	/**
	 * The logging object
	 */
	private Logger logger = Logger.getLogger("LWC");

	/**
	 * The LWC instance
	 */
	private LWC lwc;
	
	/**
	 * The player listener
	 */
	private PlayerListener playerListener;

	/**
	 * The block listener
	 */
	private BlockListener blockListener;
	
	/**
	 * The entity listener
	 */
	private EntityListener entityListener;
	
	/**
	 * LWC updater
	 * 
	 * TODO: Remove when Bukkit has an updater that is working
	 */
	private Updater updater;
	
	public LWCPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
		
		log("Loading shared objects");

		Config.init();
		
		lwc = new LWC(this);
		playerListener = new LWCPlayerListener(this);
		blockListener = new LWCBlockListener(this);
		entityListener = new LWCEntityListener(this);
		updater = new Updater();
		
		try {
			updater.check();
			updater.update();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		lwc.destruct();
	}

	@Override
	public void onEnable() {
		try {
			updater.check();
			updater.update();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		registerCommands();
		registerEvents();
		
		lwc.load();
	}
	
	/**
	 * @return the LWC instance
	 */
	public LWC getLWC() {
		return lwc;
	}
	
	/**
	 * Register all of the events used by LWC
	 * 
	 * TODO: Change priority back to NORMAL when real permissions are in
	 */
	private void registerEvents() {
		/* Player events */
		registerEvent(playerListener, Type.PLAYER_COMMAND);
		registerEvent(playerListener, Type.PLAYER_QUIT);
		
		/* Entity events */
		registerEvent(entityListener, Type.ENTITY_EXPLODE);
		
		/* Block events */
		registerEvent(blockListener, Type.BLOCK_INTERACT);
		registerEvent(blockListener, Type.BLOCK_DAMAGED);
		registerEvent(blockListener, Type.BLOCK_RIGHTCLICKED);
	}

	/***
	 * Load all of the commands
	 */
	private void registerCommands() {
		registerCommand(Admin.class);
		registerCommand(Create.class);
		registerCommand(Free.class);
		registerCommand(Info.class);
		registerCommand(Modes.class);
		registerCommand(Modify.class);
		registerCommand(Unlock.class);
	}

	/**
	 * Register a hook with default priority
	 * 
	 * TODO: Change priority back to NORMAL when real permissions are in
	 * 
	 * @param hook
	 *            the hook to register
	 */
	private void registerEvent(Listener listener, Type eventType) {
		registerEvent(listener, eventType, Priority.Monitor);
	}

	/**
	 * Register a hook
	 * 
	 * @param hook
	 *            the hook to register
	 * @priority the priority to use
	 */
	private void registerEvent(Listener listener, Type eventType, Priority priority) {
		log("-> " + eventType.toString());

		getServer().getPluginManager().registerEvent(eventType, listener, priority, this);
	}

	/**
	 * Register a command
	 * 
	 * @param command
	 */
	private void registerCommand(Class<?> clazz) {
		try {
			ICommand command = (ICommand) clazz.newInstance();
			lwc.getCommands().add(command);
			log("Loaded command : " + clazz.getSimpleName());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Log a string to the console
	 * 
	 * @param str
	 */
	private void log(String str) {
		logger.info(str);
	}

}
