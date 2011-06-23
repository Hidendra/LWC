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

import com.griefcraft.logging.Logger;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.iConomyModule;
import com.griefcraft.scripting.Module;
import com.iConomy.iConomy;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin {
    private Logger logger = Logger.getLogger("LWC-iConomy");

    /**
     * The LWC object
     */
    private LWC lwc;

    /**
     * The iConomy plugin
     */
    private iConomy money;

    /**
     * The module class for iConomy
     */
    private Module iConomyModule = null;

    /**
     * Our server listener, listens for iConomy to be loaded
     */
    private Listener serverListener = null;

    private boolean initialized = false;

    public BukkitPlugin() {
        iConomyModule = new iConomyModule(this);
        serverListener = new iConomyServerListener(this);
    }

    /**
     * @return iConomy
     */
    public iConomy getIConomy() {
        return money;
    }

    /**
     * Initialize LWC-iConomy
     *
     * @param lwc
     * @param money
     */
    public void init(LWC lwc, iConomy money) {
        if (isInitialized()) {
            return; // already initialized
        }

        this.lwc = lwc;
        this.money = money;

        // now we can register our iConomy module
        lwc.getModuleLoader().registerModule(this, iConomyModule);
        info("Registered iConomy Module into LWC successfully! Version: " + getDescription().getVersion());
        initialized = true;
    }

    public void onEnable() {
        // register the server listener
        getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
    }

    public void onDisable() {

    }

    /**
     * @return true if iConomy is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    public void info(String message) {
        logger.log("LWC-iConomy: " + message);
    }

}
