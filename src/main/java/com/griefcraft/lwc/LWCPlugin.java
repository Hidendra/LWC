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

package com.griefcraft.lwc;

import com.griefcraft.listeners.LWCBlockListener;
import com.griefcraft.listeners.LWCEntityListener;
import com.griefcraft.listeners.LWCPlayerListener;
import com.griefcraft.listeners.LWCServerListener;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.sql.Database;
import com.griefcraft.util.Colors;
import com.griefcraft.util.LWCResourceBundle;
import com.griefcraft.util.LocaleClassLoader;
import com.griefcraft.util.StringUtils;
import com.griefcraft.util.UTF8Control;
import com.griefcraft.util.Updater;
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.jar.JarFile;
import java.util.logging.Logger;

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
        } else if (name.equals("cnospam")) {
            return true;
        } else if (name.equals("cexempt")) {
            return true;
        } else return name.equals("cadmin");
    }

    /**
     * Load the database
     */
    public void loadDatabase() {
        String database = lwc.getConfiguration().getString("database.adapter");

        if (database.equalsIgnoreCase("mysql")) {
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
            String aliasCommand = null;
            String[] aliasArgs = new String[0];

            if (commandName.equals("cpublic")) {
                aliasCommand = "create";
                aliasArgs = new String[]{"public"};
            } else if (commandName.equals("cpassword")) {
                aliasCommand = "create";
                aliasArgs = ("password " + argString).split(" ");
            } else if (commandName.equals("cprivate")) {
                aliasCommand = "create";
                aliasArgs = ("private " + argString).split(" ");
            } else if (commandName.equals("cmodify")) {
                aliasCommand = "modify";
                aliasArgs = argString.isEmpty() ? new String[0] : argString.split(" ");
            } else if (commandName.equals("cinfo")) {
                aliasCommand = "info";
            } else if (commandName.equals("cunlock")) {
                aliasCommand = "unlock";
                aliasArgs = argString.isEmpty() ? new String[0] : argString.split(" ");
            } else if (commandName.equals("cremove")) {
                aliasCommand = "remove";
                aliasArgs = new String[]{"protection"};
            } else if (commandName.equals("climits")) {
                aliasCommand = "limits";
                aliasArgs = argString.isEmpty() ? new String[0] : argString.split(" ");
            } else if (commandName.equals("cadmin")) {
                aliasCommand = "admin";
                aliasArgs = argString.isEmpty() ? new String[0] : argString.split(" ");
            }

            // Flag aliases
            if (commandName.equals("credstone")) {
                aliasCommand = "flag";
                aliasArgs = ("redstone " + argString).split(" ");
            } else if (commandName.equals("cmagnet")) {
                aliasCommand = "flag";
                aliasArgs = ("magnet " + argString).split(" ");
            } else if (commandName.equals("cexempt")) {
                aliasCommand = "flag";
                aliasArgs = ("exemption " + argString).split(" ");
            }

            // Mode aliases
            if (commandName.equals("cdroptransfer")) {
                aliasCommand = "mode";
                aliasArgs = ("droptransfer " + argString).split(" ");
            } else if (commandName.equals("cpersist")) {
                aliasCommand = "mode";
                aliasArgs = ("persist " + argString).split(" ");
            } else if (commandName.equals("cnospam")) {
                aliasCommand = "mode";
                aliasArgs = ("nospam " + argString).split(" ");
            }

            if (aliasCommand != null) {
                lwc.getModuleLoader().dispatchEvent(new LWCCommandEvent(sender, aliasCommand, aliasArgs));
                return true;
            }
        }

        if (args.length == 0) {
            lwc.sendFullHelp(sender);
            return true;
        }

        ///// Dispatch command to modules
        LWCCommandEvent evt = new LWCCommandEvent(sender, args[0].toLowerCase(), args.length > 1 ? StringUtils.join(args, 1).split(" ") : new String[0]);
        lwc.getModuleLoader().dispatchEvent(evt);

        if (evt.isCancelled()) {
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

        // cancel all tasks we created
        getServer().getScheduler().cancelTasks(this);
    }

    public void onEnable() {
        String version = getDescription().getVersion();
        LWCInfo.setVersion(version);
        LWC.ENABLED = true;

        loadLocales();
        loadDatabase();
        try {
            registerEvents();
        } catch(NoSuchFieldError e) { }

        // Load the rest of LWC
        lwc.load();

        // let the updater do its thang
        updater.init();

        log("At version: " + LWCInfo.FULL_VERSION);
    }

    /**
     * @return the current locale in use
     */
    public String getCurrentLocale() {
        return lwc.getConfiguration().getString("core.locale", "en");
    }

    /**
     * Load LWC localizations
     */
    public void loadLocales() {
        String localization = getCurrentLocale();

        try {
            ResourceBundle defaultBundle = null;
            ResourceBundle optionalBundle = null;

            // Open the LWC jar file
            JarFile file = new JarFile(getFile());

            // Attempt to load the default locale
            defaultBundle = new PropertyResourceBundle(new InputStreamReader(file.getInputStream(file.getJarEntry("lang/lwc_en.properties")), "UTF-8"));

            // and now check if a bundled locale the same as the server's locale exists
            try {
                optionalBundle = new PropertyResourceBundle(new InputStreamReader(file.getInputStream(file.getJarEntry("lang/lwc_" + localization + ".properties")), "UTF-8"));
            } catch (MissingResourceException e) {
            } catch (NullPointerException e) {
                // file wasn't found :p - that's ok
            }

            // ensure both bundles aren't the same
            if (defaultBundle == optionalBundle) {
                optionalBundle = null;
            }

            locale = new LWCResourceBundle(defaultBundle);

            if (optionalBundle != null) {
                locale.addExtensionBundle(optionalBundle);
            }
        } catch (MissingResourceException e) {
            log("We are missing the default locale in LWC.jar.. What happened to it? :-(");
            throw e;
        } catch (IOException e) {
            log("Uh-oh: " + e.getMessage());
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
    }

    /**
     * Log a string to the console
     *
     * @param str
     */
    private void log(String str) {
        logger.info("LWC: " + str);
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
        // logger.info("-> " + eventType.toString());

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

        // post-1.7 event
        registerEvent(blockListener, Type.BLOCK_PISTON_EXTEND);
        registerEvent(blockListener, Type.BLOCK_PISTON_RETRACT);
    }

}
