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
import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.lwc.ManagementModule;
import com.griefcraft.lwc.PasswordRequestModule;
import com.griefcraft.scripting.ModuleLoader;
import com.griefcraft.spout.SpoutInputListener;
import com.griefcraft.spout.SpoutScreenListener;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.Screen;
import org.getspout.spoutapi.gui.WidgetAnchor;

import java.util.logging.Logger;

public class LWCSpoutPlugin extends JavaPlugin {
    private Logger logger = Logger.getLogger("LWC-Spout");

    public void onEnable() {
        // register events into LWC
        ModuleLoader moduleLoader = LWC.getInstance().getModuleLoader();
        moduleLoader.registerModule(this, new ManagementModule(this));
        moduleLoader.registerModule(this, new PasswordRequestModule(this));

        // register events into Bukkit
        getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, new SpoutScreenListener(this), Event.Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, new SpoutInputListener(this), Event.Priority.Normal, this);

        log("Hooked into LWC!");
    }

    public void onDisable() {
        log("Now disabled!");
    }

    /**
     * Log a message to the logger
     *
     * @param message
     */
    public void log(String message) {
        logger.info("LWC-Spout: " + message);
    }

    /**
     * Put the LWC name and author on the screen
     *
     * @param screen
     */
    public void bindLogo(Screen screen) {
        GenericLabel name = new GenericLabel("LWC " + LWCInfo.FULL_VERSION);
        name.setAlign(WidgetAnchor.BOTTOM_LEFT);
        name.setAnchor(WidgetAnchor.BOTTOM_LEFT);
        name.setTextColor(new Color(0.31f, 0.78f, 0.47f)); // Emerald
        name.shiftXPos(5).shiftYPos(-10);

        GenericLabel author = new GenericLabel("by Hidendra");
        author.setAlign(WidgetAnchor.BOTTOM_LEFT);
        author.setAnchor(WidgetAnchor.BOTTOM_LEFT);
        author.setTextColor(new Color(0.31f, 0.78f, 0.47f));
        author.shiftXPos(5);
        
        screen.attachWidget(this, name);
        screen.attachWidget(this, author);
    }

}
