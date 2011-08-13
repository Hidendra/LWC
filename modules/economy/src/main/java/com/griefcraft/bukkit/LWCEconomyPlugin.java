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


import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.EconomyModule;
import com.griefcraft.scripting.Module;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class LWCEconomyPlugin extends JavaPlugin {
    private Logger logger = Logger.getLogger("LWC-iConomy");

    /**
     * The LWC object
     */
    private LWC lwc;
    
    /**
     * The module class for iConomy
     */
    private Module iConomyModule = null;

    /**
     * Our server listener, listens for iConomy to be loaded
     */
    private Listener serverListener = null;

    public LWCEconomyPlugin() {
        iConomyModule = new EconomyModule(this);
        serverListener = new EconomyServerListener(this);
    }

    /**
     * Initialize LWC-iConomy
     */
    public void init() {
        LWC.getInstance().getModuleLoader().registerModule(this, iConomyModule);
        info("Registered Economy Module into LWC successfully! Version: " + getDescription().getVersion());
    }

    public void onEnable() {
        Plugin lwc = getServer().getPluginManager().getPlugin("LWC");

        if(lwc != null && lwc.isEnabled()) {
            init();
        } else {
            // register the server listener
            getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);

            info("Waiting for LWC & iConomy to be enabled...");
        }
    }

    public void onDisable() {

    }

    private void info(String message) {
        logger.info("LWC-iConomy: " + message);
    }

}
