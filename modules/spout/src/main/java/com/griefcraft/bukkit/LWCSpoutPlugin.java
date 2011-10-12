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
