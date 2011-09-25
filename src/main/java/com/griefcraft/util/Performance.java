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

package com.griefcraft.util;

import com.griefcraft.cache.LRUCache;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.scripting.MetaData;
import com.griefcraft.sql.Database;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Performance {

    private static Logger logger = Logger.getLogger("Performance");

    /**
     * Number of queries generated for mem db
     */
    private static int memDBQueries = 0;

    /**
     * Number of queries generated for phys db
     */
    private static int physDBQueries = 0;

    /**
     * Time when LWC was started
     */
    private static long startTime = 0L;

    /**
     * Add a query
     */
    public static void addMemDBQuery() {
        memDBQueries++;
    }

    /**
     * Add a query
     */
    public static void addPhysDBQuery() {
        physDBQueries++;
    }

    /**
     * @return the average of a value
     */
    public static double getAverage(long value) {
        return (double) value / getTimeRunningSeconds();
    }

    /**
     * Send a performance report to a Console Sender
     *
     * @param sender
     */
    public static void sendReport(CommandSender sender) {
        LWC lwc = LWC.getInstance();
        
        sender.sendMessage(" ");
        sender.sendMessage(Colors.Red + "LWC Report");
        sender.sendMessage("  Version: " + Colors.Green + LWCInfo.FULL_VERSION);
        sender.sendMessage("  Running time: " + Colors.Green + StringUtils.timeToString(getTimeRunningSeconds()));
        sender.sendMessage("  Players: " + Colors.Green + Bukkit.getServer().getOnlinePlayers().length + "/" + Bukkit.getServer().getMaxPlayers());
        sender.sendMessage(" ");
        sender.sendMessage(Colors.Red + " ==== Modules ====");
        
        for (Map.Entry<Plugin, List<MetaData>> entry : lwc.getModuleLoader().getRegisteredModules().entrySet()) {
            Plugin plugin = entry.getKey();
            List<MetaData> modules = entry.getValue();

            sender.sendMessage("  " + Colors.Green + plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion() + Colors.Yellow + " -> " + Colors.Green + modules.size() + Colors.Yellow + " registered modules");
        }
        sender.sendMessage(" ");

        sender.sendMessage(Colors.Red + " ==== Database ====");
        sender.sendMessage("  Engine: " + Colors.Green + Database.DefaultType);
        sender.sendMessage("  Protections: " + Colors.Green + lwc.getPhysicalDatabase().getProtectionCount());
        sender.sendMessage("  Physical Database: " + Colors.Green + physDBQueries + " queries | " + String.format("%.2f", getAverage(physDBQueries)) + " / second");
        sender.sendMessage("  Memory Database: " + Colors.Green + memDBQueries + " queries | " + String.format("%.2f", getAverage(memDBQueries)) + " / second");
        sender.sendMessage(" ");

        sender.sendMessage(Colors.Red + " ==== Cache ==== ");
        sender.sendMessage("  Size: " + lwc.getCaches().getProtections().size() + "/" + lwc.getConfiguration().getInt("core.cacheSize", 10000));

        LRUCache protections = lwc.getCaches().getProtections();
        sender.sendMessage("  Reads: " + protections.getReads()  + " | " + String.format("%.2f", getAverage(protections.getReads())) + " / second");
        sender.sendMessage("  Writes: " + protections.getWrites()  + " | " + String.format("%.2f", getAverage(protections.getWrites())) + " / second");
    }

    /**
     * @return time in seconds it was being ran
     */
    public static int getTimeRunningSeconds() {
        return (int) ((System.currentTimeMillis() - startTime) / 1000);
    }

    /**
     * Initialize
     */
    public static void init() {
        startTime = System.currentTimeMillis();
    }

}
