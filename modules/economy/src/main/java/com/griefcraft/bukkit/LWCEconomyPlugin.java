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


import com.griefcraft.lwc.EconomyModule;
import com.griefcraft.lwc.LWC;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class LWCEconomyPlugin extends JavaPlugin {
    private Logger logger = Logger.getLogger("LWC-Economy");

    /**
     * The LWC object
     */
    private LWC lwc;

    /**
     * Our server listener, listens for iConomy to be loaded
     */
    private Listener serverListener = null;

    public LWCEconomyPlugin() {
        serverListener = new EconomyServerListener(this);
    }

    /**
     * Initialize LWC-iConomy
     */
    public void init() {
        LWC.getInstance().getModuleLoader().registerModule(this, new EconomyModule(this));
        info("Registered Economy Module into LWC successfully! Version: " + getDescription().getVersion());
    }

    public void onEnable() {
        Plugin lwc = getServer().getPluginManager().getPlugin("LWC");

        if (lwc != null) {
            init();
        } else {
            // register the server listener
            getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);

            info("Waiting for LWC to be enabled...");
        }
    }

    public void onDisable() {

    }

    private void info(String message) {
        logger.info("LWC-Economy: " + message);
    }

}
