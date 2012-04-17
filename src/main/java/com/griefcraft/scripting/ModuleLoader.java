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

package com.griefcraft.scripting;


import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCInfo;
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
import com.griefcraft.scripting.event.LWCReloadEvent;
import com.griefcraft.scripting.event.LWCSendLocaleEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

        //
        ACCESS_REQUEST(),

        /**
         * Called when LWC's config is reloaded
         */
        RELOAD_EVENT;

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
     * The LWC instance this object belongs to
     */
    private LWC lwc;

    /**
     * Path to the root of scripts
     */
    public final static String ROOT_PATH = "plugins/LWC/";

    /**
     * Map of loaded modules
     */
    private final Map<Plugin, List<MetaData>> pluginModules = Collections.synchronizedMap(new LinkedHashMap<Plugin, List<MetaData>>());

    /**
     * A cache used to get a list of modules for any given event. Reflection is used to find which events modules implement.
     * This was mainly added for backwards compatibility reasons (vs events to be individually registered). This still
     * achieves the same effect by using reflection.
     */
    private final Map<Event, List<Module>> fastModuleCache = new HashMap<Event, List<Module>>();

    /**
     * Toasty caches for doesObjectOverrideMethod
     */
    private final Map<String, Boolean> overrideCache = new HashMap<String, Boolean>();

    public ModuleLoader(LWC lwc) {
        this.lwc = lwc;
        populateFastModuleCache();
    }

    /**
     * Populate the fast module cache
     */
    private void populateFastModuleCache() {
        for (Event event : Event.values()) {
            fastModuleCache.put(event, new ArrayList<Module>(10));
        }
    }

    /**
     * Register a module into the fast cache
     * @param module
     */
    private void registerFastCache(Module module) {
        // get all of the methods from the module's superclass
        Class<?> superclass = module.getClass().getSuperclass();

        // Impossible
        if (superclass == null) {
            throw new IllegalArgumentException("Method cannot be its own superclass (?)");
        }

        // The methods that are possible to implement
        Method[] methods = superclass.getDeclaredMethods();

        // Now check each method to see if the module implements it
        for (Method method : methods) {
            boolean doesOverride = doesObjectOverrideMethod(module, method);

            // It does override it! Add it to the fast cache o/
            if (doesOverride) {
                // But adding it to the fast cache isn't so easy, we need the event object
                Class<?>[] parameters = method.getParameterTypes();

                // If it's not 1 we have a method we do not want
                if (parameters.length != 1) {
                    continue;
                }

                // Start comparing on the parameter to find the event type
                Event event = null;
                Class<?> parameter = parameters[0];

                if (parameter == LWCAccessEvent.class) {
                    event = Event.ACCESS_REQUEST;
                } else if (parameter == LWCBlockInteractEvent.class) {
                    event = Event.INTERACT_BLOCK;
                } else if (parameter == LWCCommandEvent.class) {
                    event = Event.COMMAND;
                } else if (parameter == LWCDropItemEvent.class) {
                    event = Event.DROP_ITEM;
                } else if (parameter == LWCProtectionDestroyEvent.class) {
                    event = Event.DESTROY_PROTECTION;
                } else if (parameter == LWCProtectionInteractEvent.class) {
                    event = Event.INTERACT_PROTECTION;
                } else if (parameter == LWCProtectionRegisterEvent.class) {
                    event = Event.REGISTER_PROTECTION;
                } else if (parameter == LWCProtectionRemovePostEvent.class) {
                    event = Event.POST_REMOVAL;
                } else if (parameter == LWCProtectionRegistrationPostEvent.class) {
                    event = Event.POST_REGISTRATION;
                } else if (parameter == LWCSendLocaleEvent.class) {
                    event = Event.SEND_LOCALE;
                } else if (parameter == LWCRedstoneEvent.class) {
                    event = Event.REDSTONE;
                } else if (parameter == LWCReloadEvent.class) {
                    event = Event.RELOAD_EVENT;
                }
                
                // ok!
                if (event != null) {
                    List<Module> modules = fastModuleCache.get(event);
                    modules.add(module);
                }
            }
        }
    }

    /**
     * Check if a method overrides a method using reflection. This method uses a cache for constant access after
     * the caches are warm and toasty.
     * <p/>
     * This assumes the object is overriding JavaModule
     *
     * @param object
     * @param method
     * @return
     */
    public boolean doesObjectOverrideMethod(Object object, Method method) {
        if (object == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        if (method == null) {
            throw new IllegalArgumentException("Method cannot be null");
        }
        String methodName = method.getName();

        String cacheKey = object.getClass().getSimpleName() + methodName;
        
        // Check the cache
        if (overrideCache.containsKey(cacheKey)) {
            return overrideCache.get(cacheKey);
        }

        // The class to compare to; the assumed superclass
        Class<?> compare = JavaModule.class;

        // The result; does it actually override the method?
        boolean result = false;

        // Compare the methods the object declares
        for (Method declaredMethod : object.getClass().getDeclaredMethods()) {
            if (declaredMethod.getName().equals(methodName)) {
                result = true;
                break;
            }
        }

        overrideCache.put(cacheKey, result);
        return result;
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
            List<Module> modules = fastModuleCache.get(event.getEventType());
            Event type = event.getEventType();
            for (Module module : modules) {
                long start = System.nanoTime();
                if (type == Event.INTERACT_PROTECTION) {
                    module.onProtectionInteract((LWCProtectionInteractEvent) event);
                } else if (type == Event.INTERACT_BLOCK) {
                    module.onBlockInteract((LWCBlockInteractEvent) event);
                } else if (type == Event.SEND_LOCALE) {
                    module.onSendLocale((LWCSendLocaleEvent) event);
                } else if (type == Event.ACCESS_REQUEST) {
                    module.onAccessRequest((LWCAccessEvent) event);
                } else if (type == Event.COMMAND) {
                    module.onCommand((LWCCommandEvent) event);
                } else if (type == Event.DROP_ITEM) {
                    module.onDropItem((LWCDropItemEvent) event);
                } else if (type == Event.DESTROY_PROTECTION) {
                    module.onDestroyProtection((LWCProtectionDestroyEvent) event);
                }else if (type == Event.REGISTER_PROTECTION) {
                    module.onRegisterProtection((LWCProtectionRegisterEvent) event);
                } else if (type == Event.POST_REMOVAL) {
                    module.onPostRemoval((LWCProtectionRemovePostEvent) event);
                } else if (type == Event.POST_REGISTRATION) {
                    module.onPostRegistration((LWCProtectionRegistrationPostEvent) event);
                } else if (type == Event.REDSTONE) {
                    module.onRedstone((LWCRedstoneEvent) event);
                } else if (type == Event.RELOAD_EVENT) {
                    module.onReload((LWCReloadEvent) event);
                }
            }
        } catch (Throwable throwable) {
            throw new ModuleException("LWC Module threw an uncaught exception! LWC version: " + LWCInfo.FULL_VERSION, throwable);
        }
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
        // Ensure LWC is at the head of the list
        synchronized (pluginModules) {
            Map<Plugin, List<MetaData>> newMap = new LinkedHashMap<Plugin, List<MetaData>>();

            // Add LWC
            newMap.put(lwc.getPlugin(), pluginModules.get(lwc.getPlugin()));

            // Add the rest
            newMap.putAll(pluginModules);

            // Clear the old map
            pluginModules.clear();

            // Add the new values in
            pluginModules.putAll(newMap);
        }

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

        // Populate the fast cache
        registerFastCache(module);
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
