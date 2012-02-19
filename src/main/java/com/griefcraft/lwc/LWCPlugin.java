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

package com.griefcraft.lwc;

import com.griefcraft.listeners.LWCBlockListener;
import com.griefcraft.listeners.LWCEntityListener;
import com.griefcraft.listeners.LWCPlayerListener;
import com.griefcraft.listeners.LWCServerListener;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.sql.Database;
import com.griefcraft.util.StopWatch;
import com.griefcraft.util.StringUtil;
import com.griefcraft.util.Updater;
import com.griefcraft.util.Version;
import com.griefcraft.util.locale.LWCResourceBundle;
import com.griefcraft.util.locale.LocaleClassLoader;
import com.griefcraft.util.locale.UTF8Control;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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
     * The LWC instance
     */
    private LWC lwc;

    /**
     * The block listener
     */
    private LWCBlockListener blockListener;

    /**
     * The entity listener
     */
    private LWCEntityListener entityListener;

    /**
     * The player listener
     */
    private LWCPlayerListener playerListener;

    /**
     * The server listener
     */
    private LWCServerListener serverListener;

    /**
     * The locale for LWC
     */
    private LWCResourceBundle locale;

    /**
     * The logging object
     */
    private Logger logger = Logger.getLogger("LWC");

    /**
     * LWC updater
     */
    private Updater updater;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String commandName = command.getName().toLowerCase();
        String argString = StringUtil.join(args, 0);
        boolean isPlayer = (sender instanceof Player); // check if they're a player

        // Timing
        StopWatch stopWatch = new StopWatch("onCommand");
        stopWatch.start();

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
            } else if (commandName.equals("cprivate") || commandName.equals("lock")) {
                aliasCommand = "create";
                aliasArgs = ("private " + argString).split(" ");
            } else if (commandName.equals("cdonation")) {
                aliasCommand = "create";
                aliasArgs = ("donation " + argString).split(" ");
            } else if (commandName.equals("cmodify")) {
                aliasCommand = "modify";
                aliasArgs = argString.isEmpty() ? new String[0] : argString.split(" ");
            } else if (commandName.equals("cinfo")) {
                aliasCommand = "info";
            } else if (commandName.equals("cunlock")) {
                aliasCommand = "unlock";
                aliasArgs = argString.isEmpty() ? new String[0] : argString.split(" ");
            } else if (commandName.equals("cremove") || commandName.equals("unlock")) {
                aliasCommand = "remove";
                aliasArgs = new String[]{"protection"};
            } else if (commandName.equals("climits")) {
                aliasCommand = "limits";
                aliasArgs = argString.isEmpty() ? new String[0] : argString.split(" ");
            } else if (commandName.equals("cadmin")) {
                aliasCommand = "admin";
                aliasArgs = argString.isEmpty() ? new String[0] : argString.split(" ");
            } else if (commandName.equals("cremoveall")) {
                aliasCommand = "remove";
                aliasArgs = new String[]{"allprotections"};
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
            } else if (commandName.equals("cautoclose")) {
                aliasCommand = "flag";
                aliasArgs = ("autoclose " + argString).split(" ");
            } else if (commandName.equals("callowexplosions") || commandName.equals("ctnt")) {
                aliasCommand = "flag";
                aliasArgs = ("allowexplosions " + argString).split(" ");
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
                lwc.completeStopwatch(stopWatch, (Player) sender);
                return true;
            }
        }

        if (args.length == 0) {
            lwc.sendFullHelp(sender);
            return true;
        }

        ///// Dispatch command to modules
        LWCCommandEvent evt = new LWCCommandEvent(sender, args[0].toLowerCase(), args.length > 1 ? StringUtil.join(args, 1).split(" ") : new String[0]);
        lwc.getModuleLoader().dispatchEvent(evt);

        // Send timings if they're a player
        if (isPlayer) {
            lwc.completeStopwatch(stopWatch, (Player) sender);
        }

        if (evt.isCancelled()) {
            return true;
        }

        if (!isPlayer) {
            lwc.sendLocale(sender, "lwc.commandnotsupported");
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
        preload();
        lwc = new LWC(this);

        LWCInfo.setVersion(getDescription().getVersion());
        LWC.ENABLED = true;

        loadLocales();
        loadDatabase();
        try {
            registerEvents();
        } catch (NoSuchFieldError e) {
        }

        // Load the rest of LWC
        lwc.load();

        // let the updater do its thang
        updater.init();

        Version version = LWCInfo.FULL_VERSION;
        log("At version: " + version.toString());
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

    /**
     * Load LWC localizations
     */
    public void loadLocales() {
        String localization = getCurrentLocale();

        // located in plugins/LWC/locale/, values in that overrides the ones in the default :-)
        ResourceBundle optionalBundle = null;

        try {
            ResourceBundle defaultBundle;

            // Open the LWC jar file
            JarFile file = new JarFile(getFile());

            // Attempt to load the default locale
            defaultBundle = new PropertyResourceBundle(new InputStreamReader(file.getInputStream(file.getJarEntry("lang/lwc_en.properties")), "UTF-8"));
            locale = new LWCResourceBundle(defaultBundle);

            try {
                optionalBundle = ResourceBundle.getBundle("lwc", new Locale(localization), new LocaleClassLoader(), new UTF8Control());
            } catch (MissingResourceException e) {
            }

            if (optionalBundle != null) {
                locale.addExtensionBundle(optionalBundle);
            }

            int overrides = optionalBundle != null ? optionalBundle.keySet().size() : 0;
            log("Loaded " + locale.keySet().size() + " locale strings (" + overrides + " overrides)");

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
    }

    /**
     * Load shared libraries and other misc things
     */
    private void preload() {
        log("Loading shared objects");

        updater = new Updater();
        playerListener = new LWCPlayerListener(this);
        blockListener = new LWCBlockListener(this);
        entityListener = new LWCEntityListener(this);
        serverListener = new LWCServerListener(this);

        // Set the SQLite native library path
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
     * Log a string to the console
     *
     * @param str
     */
    private void log(String str) {
        logger.info("LWC: " + str);
    }

    /**
     * Register all of the events used by LWC
     */
    private void registerEvents() {
        PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        pluginManager.registerEvents(playerListener, this);
        pluginManager.registerEvents(entityListener, this);
        pluginManager.registerEvents(blockListener, this);
        pluginManager.registerEvents(serverListener, this);
    }

    /**
     * @return the current locale in use
     */
    public String getCurrentLocale() {
        return lwc.getConfiguration().getString("core.locale", "en");
    }

    /**
     * @return the LWC instance
     */
    public LWC getLWC() {
        return lwc;
    }

    /**
     * @return the locale
     */
    public ResourceBundle getLocale() {
        return locale;
    }

    /**
     * @return the Updater instance
     */
    public Updater getUpdater() {
        return updater;
    }

    @Override
    public File getFile() {
        return super.getFile();
    }

    @Override
    public ClassLoader getClassLoader() {
        return super.getClassLoader();
    }
}
