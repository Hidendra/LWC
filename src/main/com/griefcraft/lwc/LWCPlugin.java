package com.griefcraft.lwc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

import com.griefcraft.commands.Admin;
import com.griefcraft.commands.Flag;
import com.griefcraft.commands.ICommand;
import com.griefcraft.commands.Menu;
import com.griefcraft.commands.Modes;
import com.griefcraft.commands.Modify;
import com.griefcraft.commands.Owners;
import com.griefcraft.commands.Unlock;
import com.griefcraft.listeners.LWCBlockListener;
import com.griefcraft.listeners.LWCEntityListener;
import com.griefcraft.listeners.LWCPlayerListener;
import com.griefcraft.logging.Logger;
import com.griefcraft.scripting.Module.Result;
import com.griefcraft.scripting.ModuleLoader.Event;
import com.griefcraft.sql.Database;
import com.griefcraft.util.Colors;
import com.griefcraft.util.Config;
import com.griefcraft.util.ConfigValues;
import com.griefcraft.util.LWCResourceBundle;
import com.griefcraft.util.LocaleClassLoader;
import com.griefcraft.util.StringUtils;
import com.griefcraft.util.UTF8Control;
import com.griefcraft.util.Updater;

public class LWCPlugin extends JavaPlugin {

	/**
	 * The block listener
	 */
	private BlockListener blockListener;

	/**
	 * The entity listener
	 */
	private EntityListener entityListener;

	/**
	 * The locale for LWC
	 */
	private LWCResourceBundle locale;

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
	 * LWC updater
	 * 
	 * TODO: Remove when Bukkit has an updater that is working
	 */
	private Updater updater;

	public LWCPlugin() {
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

		// we want to force people who used sqlite.purejava before to switch:
		System.setProperty("sqlite.purejava", "");

		// BUT, some can't use native, so we need to give them the option to use
		// pure:
		String isPureJava = System.getProperty("lwc.purejava");

		if (isPureJava != null && isPureJava.equalsIgnoreCase("true")) {
			System.setProperty("sqlite.purejava", "true");
		}

		log("Native library: " + updater.getFullNativeLibraryPath());
	}

	/**
	 * @return the locale
	 */
	public ResourceBundle getLocale() {
		return locale;
	}

	/**
	 * @return the LWC instance
	 */
	public LWC getLWC() {
		return lwc;
	}

	/**
	 * @return the Updater instance
	 */
	public Updater getUpdater() {
		return updater;
	}

	/**
	 * Verify a command name
	 * 
	 * @param name
	 * @return
	 */
	public boolean isValidCommand(String name) {
		name = name.toLowerCase();

		if (name.equals("lwc")) {
			return true;
		} else if (name.equals("cpublic")) {
			return true;
		} else if (name.equals("cpassword")) {
			return true;
		} else if (name.equals("cprivate")) {
			return true;
		} else if (name.equals("cinfo")) {
			return true;
		} else if (name.equals("cmodify")) {
			return true;
		} else if (name.equals("cunlock")) {
			return true;
		} else if (name.equals("cremove")) {
			return true;
		} else if (name.equals("climits")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Load the database
	 */
	public void loadDatabase() {
		String database = ConfigValues.DATABASE.getString().toLowerCase();

		if (database.equals("mysql")) {
			Database.DefaultType = Database.Type.MySQL;
		} else {
			Database.DefaultType = Database.Type.SQLite;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		String commandName = command.getName().toLowerCase();
		String argString = StringUtils.join(args, 0);
		boolean isPlayer = (sender instanceof Player); // check if they're a player

		if (!isValidCommand(commandName)) {
			return false;
		}

		// these can only apply to players, not the console (who has absolute player :P)
		if (isPlayer) {
			if (lwc.getPermissions() != null && !lwc.getPermissions().permission((Player) sender, "lwc.protect")) {
				sender.sendMessage(Colors.Red + "You do not have permission to do that");
				return true;
			}

			/*
			 * Aliases
			 */
			if (commandName.equals("cpublic")) {
				lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "create", "public".split(" "));
				return true;
			} else if (commandName.equals("cpassword")) {
				lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "create", ("password" + argString).split(" "));
				return true;
			} else if (commandName.equals("cprivate")) {
				lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "create", ("private" + argString).split(" "));
				return true;
			} else if (commandName.equals("cmodify")) {
				lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "modify", argString.split(" "));
				return true;
			} else if (commandName.equals("cinfo")) {
				lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "info", "".split(" "));
				return true;
			} else if (commandName.equals("cunlock")) {
				lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "unlock", argString.split(" "));
				return true;
			} else if (commandName.equals("cremove")) {
				lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "remote", "protection".split(" "));
				return true;
			} else if (commandName.equals("climits")) {
				lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "info", "limits".split(" "));
				return true;
			}
		}

		if (args.length == 0) {
			lwc.sendFullHelp(sender);
			return true;
		}
		
		///// Dispatch command to modules
		if(lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, args[0].toLowerCase(), args.length > 1 ? StringUtils.join(args, 1).split(" ") : new String[0]) == Result.CANCEL) {
			sender.sendMessage("(MODULE)");
			return true;
		}

		for (ICommand cmd : lwc.getCommands()) {
			if (!cmd.validate(lwc, sender, args)) {
				continue;
			}

			if (!isPlayer && !cmd.supportsConsole()) {
				continue;
			}

			try {
				cmd.execute(lwc, sender, args);
			} catch (Exception e) {
				log("Oh no! An LWC command threw an exception!");
				e.printStackTrace();
			}

			return true;
		}

		if (!isPlayer) {
			sender.sendMessage(Colors.Red + "That LWC command is not supported through the console :-)");
			return true;
		}

		return false;
	}

	@Override
	public void onDisable() {
		if (lwc != null) {
			lwc.destruct();
		}
	}

	@Override
	public void onEnable() {
		Config.init();

		try {
			ResourceBundle defaultBundle = null;
			ResourceBundle optionalBundle = null;

			// load the default locale first
			defaultBundle = ResourceBundle.getBundle("lang.lwc", new Locale("en"), new UTF8Control());

			// and now check if a bundled locale the same as the server's locale exists
			try {
				optionalBundle = ResourceBundle.getBundle("lang.lwc", new Locale(ConfigValues.LOCALE.getString()), new UTF8Control());
			} catch (MissingResourceException e) {
			}
			
			// ensure both bundles arent the same
			if(defaultBundle == optionalBundle) {
				optionalBundle = null;
			}

			locale = new LWCResourceBundle(defaultBundle);
			
			if(optionalBundle != null) {
				locale.addExtensionBundle(optionalBundle);
			}
		} catch (MissingResourceException e) {
			log("We are missing the default locale in LWC.jar.. What happened to it? :-(");
			log("###########################");
			log("## SHUTTING DOWN LWC !!! ##");
			log("###########################");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		// located in plugins/LWC/locale/, values in that overrides the ones in the default :-)
		ResourceBundle optionalBundle = null;

		try {
			optionalBundle = ResourceBundle.getBundle("lwc", new Locale(ConfigValues.LOCALE.getString()), new LocaleClassLoader(), new UTF8Control());
		} catch (MissingResourceException e) {
		}

		if (optionalBundle != null) {
			locale.addExtensionBundle(optionalBundle);
			log("Loaded override bundle: " + optionalBundle.getLocale().toString());
		}

		int overrides = optionalBundle != null ? optionalBundle.keySet().size() : 0;

		log("Loaded " + locale.keySet().size() + " locale strings (" + overrides + " overrides)");

		loadDatabase();
		registerCommands();
		registerEvents();
		updater.loadVersions(false);

		lwc.load();

		log("At version: " + LWCInfo.FULL_VERSION);
	}

	/**
	 * Log a string to the console
	 * 
	 * @param str
	 */
	private void log(String str) {
		logger.log(str);
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
			logger.log("Loaded command: " + command.getName(), Level.CONFIG);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/***
	 * Load all of the commands
	 */
	private void registerCommands() {
		registerCommand(Admin.class);
		registerCommand(Modes.class);
		registerCommand(Modify.class);
		registerCommand(Unlock.class);
		registerCommand(Owners.class);
		registerCommand(Menu.class);
		registerCommand(Flag.class);
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
		registerEvent(listener, eventType, Priority.Highest);
	}

	/**
	 * Register a hook
	 * 
	 * @param hook
	 *            the hook to register
	 * @priority the priority to use
	 */
	private void registerEvent(Listener listener, Type eventType, Priority priority) {
		logger.log("-> " + eventType.toString(), Level.CONFIG);

		getServer().getPluginManager().registerEvent(eventType, listener, priority, this);
	}

	/**
	 * Register all of the events used by LWC
	 * 
	 * TODO: Change priority back to NORMAL when real permissions are in
	 */
	private void registerEvents() {
		/* Player events */
		registerEvent(playerListener, Type.PLAYER_QUIT, Priority.Monitor);
		registerEvent(playerListener, Type.PLAYER_DROP_ITEM);
		registerEvent(playerListener, Type.PLAYER_INTERACT);

		/* Entity events */
		registerEvent(entityListener, Type.ENTITY_EXPLODE);

		/* Block events */
		registerEvent(blockListener, Type.BLOCK_DAMAGE);
		registerEvent(blockListener, Type.BLOCK_BREAK);
		registerEvent(blockListener, Type.BLOCK_PLACE);
		registerEvent(blockListener, Type.REDSTONE_CHANGE);
		registerEvent(blockListener, Type.SIGN_CHANGE);
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

		// check for existing file
		File configFile = new File("lwc.properties");

		if (!configFile.exists()) {
			return;
		}

		log("Migration required");

		log(" + creating folder plugins/LWC");
		folder.mkdir();

		log(" - loading old lwc.properties");
		Config config = Config.getInstance("lwc.properties");

		// People's initial flush-db was 60 seconds.. then i lowered to 30..
		// which both is too high let's take this opportunity to change that,
		// shall we?
		if (Integer.parseInt((String) config.get("flush-db-interval")) > 20) {
			config.setProperty("flush-db-interval", "10");
		}

		log(" - inspecting lwc.db");
		File databaseFile = new File("lwc.db");

		if (databaseFile.exists()) {
			try {
				FileChannel inChannel = new FileInputStream(databaseFile).getChannel();
				FileChannel outChannel = new FileOutputStream("plugins/LWC/lwc.db").getChannel();

				outChannel.transferFrom(inChannel, 0, inChannel.size());

				log(" ++ lwc.db moved");

				config.setProperty("db-path", "plugins/LWC/lwc.db");

				inChannel.close();
				outChannel.close();
			} catch (Exception e) {
				log(" -- move failed, reason: " + e.getMessage());
			}
		} else {
			log(" -- lwc.db not found, ignoring");
		}

		// Now initialize the regular config so we can copy the loaded values
		Config.init();

		log(" - moving the old lwc.properties");

		// Copy
		for(Map.Entry<Object, Object> entry : config.entrySet()) {
			Config.getInstance().put(entry.getKey(), entry.getValue());
		}

		Config.getInstance().save();
		log(" ++ saved " + config.size() + " config values");

		log(" - cleaning up");
		databaseFile.delete();
		new File("lwc.properties").delete();
		log(" +++ migration complete");
	}

}
