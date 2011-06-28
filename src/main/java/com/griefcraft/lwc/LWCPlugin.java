package com.griefcraft.lwc;

import com.griefcraft.listeners.LWCBlockListener;
import com.griefcraft.listeners.LWCEntityListener;
import com.griefcraft.listeners.LWCPlayerListener;
import com.griefcraft.listeners.LWCServerListener;
import com.griefcraft.logging.Logger;
import com.griefcraft.scripting.Module.Result;
import com.griefcraft.scripting.ModuleLoader.Event;
import com.griefcraft.sql.Database;
import com.griefcraft.util.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;

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
     * The player listener
     */
    private PlayerListener playerListener;

    /**
     * The server listener
     */
    private ServerListener serverListener;

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
     * LWC updater
     */
    private Updater updater;

    public LWCPlugin() {
        log("Loading shared objects");

        updater = new Updater();
        lwc = new LWC(this);
        playerListener = new LWCPlayerListener(this);
        blockListener = new LWCBlockListener(this);
        entityListener = new LWCEntityListener(this);
        serverListener = new LWCServerListener(this);

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
        } else if (name.equals("credstone")) {
            return true;
        } else if (name.equals("cmagnet")) {
            return true;
        } else if (name.equals("cdroptransfer")) {
            return true;
        } else if (name.equals("cpersist")) {
            return true;
        } else if (name.equals("cadmin")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Load the database
     */
    public void loadDatabase() {
        String database = lwc.getConfiguration().getString("database.adapter");

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
            // Aliases
            if (commandName.equals("cpublic")) {
                lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "create", "public".split(" "));
                return true;
            } else if (commandName.equals("cpassword")) {
                lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "create", ("password " + argString).split(" "));
                return true;
            } else if (commandName.equals("cprivate")) {
                lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "create", ("private " + argString).split(" "));
                return true;
            } else if (commandName.equals("cmodify")) {
                lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "modify", argString.isEmpty() ? new String[0] : argString.split(" "));
                return true;
            } else if (commandName.equals("cinfo")) {
                lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "info", new String[0]);
                return true;
            } else if (commandName.equals("cunlock")) {
                lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "unlock", argString.isEmpty() ? new String[0] : argString.split(" "));
                return true;
            } else if (commandName.equals("cremove")) {
                lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "remove", "protection".split(" "));
                return true;
            } else if (commandName.equals("climits")) {
                lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "limits", argString.isEmpty() ? new String[0] : argString.split(" "));
                return true;
            } else if (commandName.equals("cadmin")) {
                lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "admin", argString.isEmpty() ? new String[0] : argString.split(" "));
                return true;
            }

            // Flag aliases
            if (commandName.equals("credstone")) {
                lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "flag", ("redstone " + argString).split(" "));
                return true;
            } else if (commandName.equals("cmagnet")) {
                lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "flag", ("magnet " + argString).split(" "));
                return true;
            }

            // Mode aliases
            if (commandName.equals("cdroptransfer")) {
                lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "mode", ("droptransfer " + argString).split(" "));
                return true;
            } else if (commandName.equals("cpersist")) {
                lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, "mode", ("persist " + argString).split(" "));
                return true;
            }
        }

        if (args.length == 0) {
            lwc.sendFullHelp(sender);
            return true;
        }

        ///// Dispatch command to modules
        if (lwc.getModuleLoader().dispatchEvent(Event.COMMAND, sender, args[0].toLowerCase(), args.length > 1 ? StringUtils.join(args, 1).split(" ") : new String[0]) == Result.CANCEL) {
            return true;
        }

        if (!isPlayer) {
            sender.sendMessage(Colors.Red + "That LWC command is not supported through the console :-)");
            return true;
        }

        return false;
    }

    public void onDisable() {
        LWC.ENABLED = false;
        
        if (lwc != null) {
            lwc.destruct();
        }
    }

    public void onEnable() {
        LWC.ENABLED = true;
        String localization = lwc.getConfiguration().getString("core.locale");

        try {
            ResourceBundle defaultBundle = null;
            ResourceBundle optionalBundle = null;

            // load the default locale first
            defaultBundle = ResourceBundle.getBundle("lang.lwc", new Locale("en"), new UTF8Control());

            // and now check if a bundled locale the same as the server's locale exists
            try {
                optionalBundle = ResourceBundle.getBundle("lang.lwc", new Locale(localization), new UTF8Control());
            } catch (MissingResourceException e) {
            }

            // ensure both bundles arent the same
            if (defaultBundle == optionalBundle) {
                optionalBundle = null;
            }

            locale = new LWCResourceBundle(defaultBundle);

            if (optionalBundle != null) {
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
            optionalBundle = ResourceBundle.getBundle("lwc", new Locale(localization), new LocaleClassLoader(), new UTF8Control());
        } catch (MissingResourceException e) {
        }

        if (optionalBundle != null) {
            locale.addExtensionBundle(optionalBundle);
            log("Loaded override bundle: " + optionalBundle.getLocale().toString());
        }

        int overrides = optionalBundle != null ? optionalBundle.keySet().size() : 0;

        log("Loaded " + locale.keySet().size() + " locale strings (" + overrides + " overrides)");

        loadDatabase();
        registerEvents();

        if (lwc.getConfiguration().getBoolean("core.autoUpdate", false)) {
            updater.loadVersions(true);
        }

        lwc.load();

        String version = getDescription().getVersion();
        LWCInfo.setVersion(version);
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
     * Register a hook with default priority
     *
     * @param listener
     * @param eventType
     */
    private void registerEvent(Listener listener, Type eventType) {
        registerEvent(listener, eventType, Priority.Highest);
    }

    /**
     * Register a hook
     *
     * @param listener
     * @param eventType
     * @param priority
     */
    private void registerEvent(Listener listener, Type eventType, Priority priority) {
        logger.log("-> " + eventType.toString(), Level.CONFIG);

        getServer().getPluginManager().registerEvent(eventType, listener, priority, this);
    }

    /**
     * Register all of the events used by LWC
     */
    private void registerEvents() {
        /* Player events */
        registerEvent(playerListener, Type.PLAYER_QUIT, Priority.Monitor);
        registerEvent(playerListener, Type.PLAYER_DROP_ITEM);
        registerEvent(playerListener, Type.PLAYER_INTERACT);
        registerEvent(playerListener, Type.PLAYER_CHAT);

        /* Entity events */
        registerEvent(entityListener, Type.ENTITY_EXPLODE);

        /* Block events */
        registerEvent(blockListener, Type.BLOCK_BREAK);
        registerEvent(blockListener, Type.BLOCK_PLACE);
        registerEvent(blockListener, Type.REDSTONE_CHANGE);
        registerEvent(blockListener, Type.SIGN_CHANGE);

        /* Server events */
        registerEvent(serverListener, Type.PLUGIN_DISABLE);
    }

}
