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

package com.griefcraft.modules.admin;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.History;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.Colors;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.util.Iterator;
import java.util.List;

/**
 * Rebuild a LWC database re: Feb 15 2012 incident
 */
public class AdminRebuild extends JavaModule {

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("a", "admin")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if (!args[0].equals("rebuild")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        if (args.length == 1 || !args[1].equalsIgnoreCase("confirm")) {
            sender.sendMessage("This will attempt to rebuild the entire LWC database from scratch.");
            sender.sendMessage("This WILL repair most of the database, regarding the incident on February 15, 2012. However, please note that old protections may be restored.");
            sender.sendMessage("This may take some time so it is recommended it is ran inside the console.");
            sender.sendMessage(Colors.Red + "Are you sure you would like to go ahead? Use /lwc admin rebuild confirm to confirm.");
        } else if (args[1].equalsIgnoreCase("confirm")) {
            rebuildDatabase(sender);
        }
    }

    /**
     * Attempt to rebuild the database with what we have
     *
     * @param sender
     */
    private void rebuildDatabase(CommandSender sender) {
        LWC lwc = LWC.getInstance();
        sender.sendMessage("Now rebuilding the LWC database.");

        // Get all of the currently active history objects
        lwc.getProtectionCache().clear();
        List<History> fullHistory = lwc.getPhysicalDatabase().loadHistory(History.Status.ACTIVE);

        sender.sendMessage("Loaded " + fullHistory.size() + " history objects");

        // Our start time
        long start = System.currentTimeMillis();

        // The amount of protections we created
        int created = 0;

        // The amount of protections we failed to create
        int failed = 0;

        Iterator iter = fullHistory.iterator();
        while (iter.hasNext()) {
            History history = (History) iter.next();

            // Is it active?
            if (history.getProtection() != null) {
                iter.remove();
                continue;
            }

            // Coordinates
            int x = history.getX();
            int y = history.getY();
            int z = history.getZ();

            // Very old history object
            if (x == 0 && y == 0 && z == 0) {
                iter.remove();
                continue;
            }
            
            // Protection's creator
            String creator = history.getString("creator");
            
            if (creator == null) {
                sender.sendMessage(String.format("Unable to match owner at Id:%d", history.getId()));
                failed ++;
                iter.remove();
                continue;
            }

            // Bruteforce that shit, yo
            Block block = findProtectableBlock(x, y, z);
            
            if (block == null) {
                sender.sendMessage(String.format("Unable to match block at Id:%d [%d, %d, %d] (this is probably OK)", history.getId(), x, y, z));
                failed ++;
                iter.remove();
                continue;
            }

            // Create the protection!
            Protection protection = lwc.getPhysicalDatabase().registerProtection(block.getTypeId(), Protection.Type.PRIVATE,
                    block.getWorld().getName(), creator, "", x, y, z);
            
            if (protection == null) {
                sender.sendMessage(String.format("Failed to create protection at Id:%d", history.getId()));
                failed ++;
                iter.remove();
                continue;
            }

            // Hell yeah
            created ++;
            history.remove();
            protection.saveNow();
            iter.remove();

            // Clean up the cache, we want to conserve as much memory as possible at this time
            lwc.getProtectionCache().clear();
        }

        int total = created + failed; // the total amount of history objects operated on
        long runningTime = System.currentTimeMillis() - start;
        int runningTimeSeconds = (int) runningTime / 1000;
        float ratio = ((float) created / (total)) * 100;
        sender.sendMessage(String.format("LWC rebuild complete (%ds). %.2f%% conversion ratio; %d success and %d failures", runningTimeSeconds, ratio, created, failed));
    }

    /**
     * Look for a protectable block in each world. Return the first one found. Starts in the main world.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    private Block findProtectableBlock(int x, int y, int z) {
        LWC lwc = LWC.getInstance();
        Server server = Bukkit.getServer();

        for (World world : server.getWorlds()) {
            Block block = world.getBlockAt(x, y, z);

            if (lwc.isProtectable(block)) {
                // Woo!
                return block;
            }
        }

        // Bad news...
        return null;
    }

}