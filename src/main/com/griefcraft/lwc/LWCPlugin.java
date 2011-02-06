package com.griefcraft.lwc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import com.griefcraft.commands.Admin;
import com.griefcraft.commands.Create;
import com.griefcraft.commands.Owners;
import com.griefcraft.commands.Remove;
import com.griefcraft.commands.ICommand;
import com.griefcraft.commands.Info;
import com.griefcraft.commands.Modes;
import com.griefcraft.commands.Modify;
import com.griefcraft.commands.Unlock;
import com.griefcraft.listeners.LWCBlockListener;
import com.griefcraft.listeners.LWCEntityListener;
import com.griefcraft.listeners.LWCPlayerListener;
import com.griefcraft.logging.Logger;
import com.griefcraft.util.Colors;
import com.griefcraft.util.Config;
import com.griefcraft.util.ConfigValues;
import com.griefcraft.util.StringUtils;
import com.griefcraft.util.Updater;
import com.nijikokun.bukkit.Permissions.Permissions;

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

		update147();

		log("Loading shared objects");

		Config.init();

		lwc = new LWC(this);
		playerListener = new LWCPlayerListener(this);
		blockListener = new LWCBlockListener(this);
		entityListener = new LWCEntityListener(this);
		updater = new Updater();
		
		/*
		 * Set the SQLite native library path
		 */
		System.setProperty("org.sqlite.lib.path", updater.getOSSpecificFolder());

		log("Native library: " + updater.getFullNativeLibraryPath());

		try {
			if(ConfigValues.AUTO_UPDATE.getBool()) {
				updater.checkDist();
			} else {
				updater.check();
			}
			updater.update();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		String commandName = command.getName().toLowerCase();

		/*
		 * The only command the console could use is -admin ??, make it compatible sometime
		 */
		if(!(sender instanceof Player)) {
			return false;
		}
		
		Player player = (Player) sender;
		String argString = StringUtils.join(args, 0);

		/*
		 * Aliases
		 */
		if (commandName.equals("cpublic")) {
			lwc.getCommand(Create.class).execute(lwc, player, "-create public".split(" "));
			return true;
		} else if (commandName.equals("cpassword")) {
			lwc.getCommand(Create.class).execute(lwc, player, ("-create password " + argString).split(" "));
			return true;
		} else if (commandName.equals("cprivate")) {
			lwc.getCommand(Create.class).execute(lwc, player, ("-create private " + argString).split(" "));
			return true;
		} else if (commandName.equals("cinfo")) {
			lwc.getCommand(Info.class).execute(lwc, player, "-info".split(" "));
			return true;
		} else if (commandName.equals("cunlock")) {
			lwc.getCommand(Unlock.class).execute(lwc, player, ("-unlock " + argString).split(" "));
			return true;
		} else if (commandName.equals("cremove")) {
			lwc.getCommand(Remove.class).execute(lwc, player, "-remove protection".split(" "));
			return true;
		}

		// TODO: check if they can use the command ??
		/*
		 * if (!player.canUseCommand(split[0])) { return; }
		 */

		if (!"lwc".equalsIgnoreCase(commandName)) {
			return true;
		}

		if (lwc.getPermissions() != null && !Permissions.Security.permission(player, "lwc.protect")) {
			player.sendMessage(Colors.Red + "You do not have permission to do that");
			return true;
		}

		if (args.length == 0) {
			lwc.sendFullHelp(player);
			return true;
		}

		for (ICommand cmd : lwc.getCommands()) {
			if (!cmd.validate(lwc, player, args)) {
				continue;
			}

			cmd.execute(lwc, player, args);
			return true;
		}
		
		return false;
	}
	
	/**
	 * @return the Updater instance
	 */
	public Updater getUpdater() {
		return updater;
	}

	/**
	 * Check if an update is needed for 1.xx->1.47
	 */
	private void update147() {
		File folder = new File("plugins/LWC");

		/*
		 * Appears to already be updated
		 */
		if (folder.isDirectory()) {
			return;
		}

		log("Migration required");

		log(" + creating folder plugins/LWC");
		folder.mkdir();

		log(" - loading old lwc.properties");
		Config config = Config.getInstance("lwc.properties");

		/*
		 * People's initial flush-db was 60 seconds.. then i lowered to 30.. which both is too high let's take this opportunity to change that, shall we?
		 */
		if (Integer.parseInt((String) config.get("flush-db-interval")) > 20) {
			config.setProperty("flush-db-interval", "10");
		}

		log(" - inspecting lwc.db");
		File databaseFile = new File("lwc.db");

		if (databaseFile.exists()) {
			try {
				/*
				 * Get the file channels
				 */
				FileChannel inChannel = new FileInputStream(databaseFile).getChannel();
				FileChannel outChannel = new FileOutputStream("plugins/LWC/lwc.db").getChannel();

				/*
				 * Now copy the file
				 */
				outChannel.transferFrom(inChannel, 0, inChannel.size());

				log(" ++ lwc.db moved");

				config.setProperty("db-path", "plugins/LWC/lwc.db");

				/*
				 * We're done
				 */
				inChannel.close();
				outChannel.close();
			} catch (Exception e) {
				log(" -- move failed, reason: " + e.getMessage());
			}
		} else {
			log(" -- lwc.db not found, ignoring");
		}

		/*
		 * Now initialize the regular config so we can copy the loaded values
		 */
		Config.init();

		log(" - moving the old lwc.properties");

		/*
		 * Copy
		 */
		for (Object key : config.keySet()) {
			Config.getInstance().put(key, config.get(key));
		}

		Config.getInstance().save();
		log(" ++ saved " + config.size() + " config values");

		log(" - cleaning up");
		databaseFile.delete();
		new File("lwc.properties").delete();
		log(" +++ migration complete");
	}

	@Override
	public void onDisable() {
		Config.getInstance().save();
		updater.saveInternal();
		
		lwc.destruct();
	}

	@Override
	public void onEnable() {
		try {
			if(ConfigValues.AUTO_UPDATE.getBool()) {
				updater.checkDist();
			} else {
				updater.check();
			}
			updater.update();
		} catch (Exception e) {
			e.printStackTrace();
		}

		registerCommands();
		registerEvents();

		lwc.load();

		Config.getInstance().save();
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
		registerEvent(playerListener, Type.PLAYER_QUIT, Priority.Monitor);

		/* Entity events */
		registerEvent(entityListener, Type.ENTITY_EXPLODE);

		/* Block events */
		registerEvent(blockListener, Type.BLOCK_INTERACT);
		registerEvent(blockListener, Type.BLOCK_DAMAGED);
	}

	/***
	 * Load all of the commands
	 */
	private void registerCommands() {
		registerCommand(Admin.class);
		registerCommand(Create.class);
		registerCommand(Remove.class);
		registerCommand(Info.class);
		registerCommand(Modes.class);
		registerCommand(Modify.class);
		registerCommand(Unlock.class);
		registerCommand(Owners.class);
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
			log("Loaded command: " + command.getName());
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
