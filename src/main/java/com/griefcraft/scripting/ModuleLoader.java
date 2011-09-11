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

package com.griefcraft.scripting;


import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.Module.Result;
import com.griefcraft.scripting.event.LWCAccessEvent;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCDropItemEvent;
import com.griefcraft.scripting.event.LWCEvent;
import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.scripting.event.LWCProtectionRegistrationPostEvent;
import com.griefcraft.scripting.event.LWCProtectionRemovePostEvent;
import com.griefcraft.scripting.event.LWCRedstoneEvent;
import com.griefcraft.scripting.event.LWCSendLocaleEvent;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ModuleLoader {

    public enum Event {
        /**
         * Called when a module is loaded
         */
        LOAD(0),

        /**
         * Called when a module is unloaded
         */
        UNLOAD(0),

        /**
         * Called when a console or player issues a command
         */
        COMMAND(3),

        /**
         * Called when redstone is passed to a protection
         */
        REDSTONE(3),

        /**
         * Called when a protection is destroyed
         */
        DESTROY_PROTECTION(5),

        /**
         * Called when a valid protection is left clicked
         */
        INTERACT_PROTECTION(5),

        /**
         * Called when a block is left clicked
         */
        INTERACT_BLOCK(3),

        /**
         * Called before a protection is registered
         */
        REGISTER_PROTECTION(2),

        /**
         * Called when a protection needs to be checked if a player can access it
         */
        ACCESS_PROTECTION(2),

        /**
         * Called when a protection needs to be checked if a player can admin it
         */
        ADMIN_PROTECTION(2),

        /**
         * Called when a player drops an item
         */
        DROP_ITEM(3),

        /**
         * Called after a protection is registered
         */
        POST_REGISTRATION(1),

        /**
         * Called after a protection is confirmed to be set to be removed (cannot be cancelled here.)
         */
        POST_REMOVAL(1),

        /**
         * Called when a localized message is sent to a player (e.g lwc.accessdenied)
         */
        SEND_LOCALE(2),


        // new / temp
        ACCESS_REQUEST();

        Event() {
        }

        Event(int arguments) {
            this.arguments = arguments;
        }

        public int getExpectedArguments() {
            return arguments;
        }

        /**
         * Expected amount of arguments (not counting the lwc object!)
         */
        int arguments;
    }

    private static Logger logger = Logger.getLogger("Loader");

    /**
     * Path to the root of scripts
     */
    public final static String ROOT_PATH = "plugins/LWC/";

    /**
     * Map of loaded modules
     */
    private final Map<Plugin, List<MetaData>> pluginModules = new LinkedHashMap<Plugin, List<MetaData>>();

    public ModuleLoader() {
        // initialize the map with the LWC plugin
        pluginModules.put(LWC.getInstance().getPlugin(), new ArrayList<MetaData>());
    }

    /**
     * Dispatch an event
     *
     * @param event
     */
    public void dispatchEvent(LWCEvent event) {
        if (event == null) {
            return;
        }

        try {
            for (List<MetaData> modules : pluginModules.values()) {
                for (MetaData metaData : modules) {
                    Module module = metaData.getModule();

                    if (event instanceof LWCAccessEvent) {
                        module.protectionAccessRequest((LWCAccessEvent) event);
                    } else if (event instanceof LWCBlockInteractEvent) {
                        module.onBlockInteract((LWCBlockInteractEvent) event);
                    } else if (event instanceof LWCCommandEvent) {
                        module.onCommand((LWCCommandEvent) event);
                    } else if (event instanceof LWCDropItemEvent) {
                        module.onDropItem((LWCDropItemEvent) event);
                    } else if (event instanceof LWCProtectionDestroyEvent) {
                        module.onDestroyProtection((LWCProtectionDestroyEvent) event);
                    } else if (event instanceof LWCProtectionInteractEvent) {
                        module.onProtectionInteract((LWCProtectionInteractEvent) event);
                    } else if (event instanceof LWCProtectionRegisterEvent) {
                        module.onRegisterProtection((LWCProtectionRegisterEvent) event);
                    } else if (event instanceof LWCProtectionRemovePostEvent) {
                        module.onPostRemoval((LWCProtectionRemovePostEvent) event);
                    } else if (event instanceof LWCProtectionRegistrationPostEvent) {
                        module.onPostRegistration((LWCProtectionRegistrationPostEvent) event);
                    } else if (event instanceof LWCSendLocaleEvent) {
                        module.onSendLocale((LWCSendLocaleEvent) event);
                    } else if (event instanceof LWCRedstoneEvent) {
                        module.onRedstone((LWCRedstoneEvent) event);
                    }
                }
            }
        } catch (Throwable throwable) {
            throw new ModuleException("LWC Module threw an uncaught exception! LWC version: " + LWCInfo.FULL_VERSION, throwable);
        }
    }

    /**
     * Dispatch an event
     *
     * @param event
     * @param args
     * @deprecated
     */
    public Result dispatchEvent(Event event, Object... args) {
        if (event.getExpectedArguments() > args.length) {
            return Result.DEFAULT;
        }

        LWC lwc = LWC.getInstance();
        Result result = Result.DEFAULT;

        try {
            for (List<MetaData> modules : pluginModules.values()) {
                for (MetaData metaData : modules) {
                    Module module = metaData.getModule();
                    Result temp = Result.DEFAULT;

                    switch (event) {

                        case COMMAND:
                            temp = module.onCommand(lwc, (CommandSender) args[0], (String) args[1], (String[]) args[2]);
                            break;

                        case REDSTONE:
                            temp = module.onRedstone(lwc, (Protection) args[0], (Block) args[1], (Integer) args[2]);
                            break;

                        case DESTROY_PROTECTION:
                            temp = module.onDestroyProtection(lwc, (Player) args[0], (Protection) args[1], (Block) args[2], (Boolean) args[3], (Boolean) args[4]);
                            break;

                        case INTERACT_PROTECTION:
                            temp = module.onProtectionInteract(lwc, (Player) args[0], (Protection) args[1], (List<String>) args[2], (Boolean) args[3], (Boolean) args[4]);
                            break;

                        case INTERACT_BLOCK:
                            temp = module.onBlockInteract(lwc, (Player) args[0], (Block) args[1], (List<String>) args[2]);
                            break;

                        case REGISTER_PROTECTION:
                            temp = module.onRegisterProtection(lwc, (Player) args[0], (Block) args[1]);
                            break;

                        case ACCESS_PROTECTION:
                            temp = module.canAccessProtection(lwc, (Player) args[0], (Protection) args[1]);
                            break;

                        case ADMIN_PROTECTION:
                            temp = module.canAdminProtection(lwc, (Player) args[0], (Protection) args[1]);
                            break;

                        case DROP_ITEM:
                            temp = module.onDropItem(lwc, (Player) args[0], (Item) args[1], (ItemStack) args[2]);
                            break;

                        case POST_REGISTRATION:
                            module.onPostRegistration(lwc, (Protection) args[0]);
                            break;

                        case POST_REMOVAL:
                            module.onPostRemoval(lwc, (Protection) args[0]);
                            break;

                        case SEND_LOCALE:
                            temp = module.onSendLocale(lwc, (Player) args[0], (String) args[1]);
                            break;

                        case ACCESS_REQUEST:
                            throw new OperationNotSupportedException("ACCESS_REQUEST");
                    }

                    if (temp != Result.DEFAULT) {
                        result = temp;
                    }

                    if (result == Result.CANCEL) {
                        return result;
                    }
                }
            }
        } catch (Throwable throwable) {
            throw new ModuleException("LWC Module threw an uncaught exception! LWC version: " + LWCInfo.FULL_VERSION, throwable);
        }

        if (result == null) {
            result = Result.DEFAULT;
        }

        return result;
    }

    /**
     * Shutdown the plugin loader
     *
     * @todo broadcast UNLOAD
     */
    public void shutdown() {
        pluginModules.clear();
    }

    /**
     * Load all of the modules not marked as loaded
     */
    public void loadAll() {
        LWC lwc = LWC.getInstance();

        for (List<MetaData> modules : pluginModules.values()) {
            for (MetaData metaData : modules) {
                if (!metaData.isLoaded()) {
                    metaData.getModule().load(lwc);
                    metaData.trigger();
                }
            }
        }
    }

    /**
     * Get the first module represented by a class
     *
     * @param clazz
     * @return
     */
    public Module getModule(Class<? extends Module> clazz) {
        for (List<MetaData> modules : pluginModules.values()) {
            for (MetaData metaData : modules) {
                Module module = metaData.getModule();

                if (module.getClass() == clazz) {
                    return module;
                }
            }
        }

        return null;
    }

    /**
     * Not intended to be used a lot -- use sparingly!
     * The map returned is NOT modifiable.
     *
     * @return the registered modules
     */
    public Map<Plugin, List<MetaData>> getRegisteredModules() {
        return Collections.unmodifiableMap(pluginModules);
    }

    /**
     * @return the number of registered modules
     */
    public int getModuleCount() {
        int count = 0;

        for (List<MetaData> modules : pluginModules.values()) {
            count += modules.size();
        }

        return count;
    }

    /**
     * Register a module for a plugin
     *
     * @param plugin
     * @param module
     */
    public void registerModule(Plugin plugin, Module module) {
        List<MetaData> modules = null;

        if (plugin != null) {
            modules = pluginModules.get(plugin);
        }

        if (modules == null) {
            modules = new ArrayList<MetaData>();
        }

        MetaData metaData = new MetaData(module);
        modules.add(metaData);
        pluginModules.put(plugin, modules);
    }

    /**
     * Remove the modules for a plugin
     *
     * @param plugin
     */
    public void removeModules(Plugin plugin) {
        pluginModules.remove(plugin);
    }

}
