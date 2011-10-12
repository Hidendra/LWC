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
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.sql.Database;
import com.griefcraft.util.Colors;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AdminCleanup extends JavaModule {

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

        if (!args[0].equals("cleanup")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        // if we shouldn't output
        boolean silent = false;

        if (args.length > 1 && args[1].equalsIgnoreCase("silent")) {
            silent = true;
        }

        lwc.sendLocale(sender, "protection.admin.cleanup.start", "count", lwc.getPhysicalDatabase().getProtectionCount());

        // do the work in a separate thread so we don't fully lock the server
        // new Thread(new Admin_Cleanup_Thread(lwc, sender)).start();
        new Admin_Cleanup_Thread(lwc, sender, silent).run();
    }

    /**
     * Class that handles cleaning up the LWC database usage: /lwc admin cleanup
     */
    private static class Admin_Cleanup_Thread implements Runnable {

        private LWC lwc;
        private CommandSender sender;
        private boolean silent;

        public Admin_Cleanup_Thread(LWC lwc, CommandSender sender, boolean silent) {
            this.lwc = lwc;
            this.sender = sender;
            this.silent = silent;
        }

        /**
         * Push removal changes to the database
         *
         * @param toRemove
         */
        public void push(List<Integer> toRemove) throws SQLException {
            final StringBuilder builder = new StringBuilder();
            final int total = toRemove.size();
            int count = 0;

            // iterate over the items to remove
            Iterator<Integer> iter = toRemove.iterator();

            // the database prefix
            String prefix = lwc.getPhysicalDatabase().getPrefix();

            // create the statement to use
            Statement statement = lwc.getPhysicalDatabase().getConnection().createStatement();

            while (iter.hasNext()) {
                int protectionId = iter.next();

                if (count % 100000 == 0) {
                    builder.append("DELETE FROM ").append(prefix).append("protections WHERE id IN (").append(protectionId);
                } else {
                    builder.append(",").append(protectionId);
                }

                if (count % 100000 == 99999 || count == (total - 1)) {
                    builder.append(")");
                    statement.executeUpdate(builder.toString());
                    builder.setLength(0);

                    sender.sendMessage(Colors.Green + "REMOVED " + (count + 1) + " / " + total);
                }

                count++;
            }

            statement.close();
        }

        public void run() {
            long start = System.currentTimeMillis();
            int completed = 0;
            int count = 0;

            List<Integer> toRemove = new LinkedList<Integer>();
            int totalProtections = lwc.getPhysicalDatabase().getProtectionCount();

            sender.sendMessage("Loading protections via STREAM mode");

            try {
                Statement resultStatement = lwc.getPhysicalDatabase().getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

                if(lwc.getPhysicalDatabase().getType() == Database.Type.MySQL) {
                    resultStatement.setFetchSize(Integer.MIN_VALUE);
                }

                String prefix = lwc.getPhysicalDatabase().getPrefix();
                ResultSet result = resultStatement.executeQuery("SELECT id, owner, type, x, y, z, data, blockId, world, password, date, last_accessed FROM " + prefix + "protections");

                while (result.next()) {
                    Protection protection = lwc.getPhysicalDatabase().resolveProtection(result);
                    World world = protection.getBukkitWorld();

                    count++;

                    if (count % 100000 == 0 || count == totalProtections || count == 1) {
                        sender.sendMessage(Colors.Red + count + " / " + totalProtections);
                    }

                    if (world == null) {
                        if (!silent) {
                            lwc.sendLocale(sender, "protection.admin.cleanup.noworld", "world", protection.getWorld());
                        }

                        continue;
                    }

                    // now we can check the world for the protection
                    Block block = protection.getBlock();

                    // remove protections not found in the world
                    if (block == null || !lwc.isProtectable(block)) {
                        toRemove.add(protection.getId());
                        completed++;

                        if (!silent) {
                            lwc.sendLocale(sender, "protection.admin.cleanup.removednoexist", "protection", protection.toString());
                        }
                    }
                }

                // Close the streaming statement
                result.close();
                resultStatement.close();

                // flush all of the queries
                push(toRemove);
            } catch (Exception e) {
                sender.sendMessage("Uh-oh, something bad happened while cleaning up the LWC database!");
                lwc.sendLocale(sender, "protection.internalerror", "id", "cleanup");
                e.printStackTrace();
            }

            long finish = System.currentTimeMillis();
            float timeInSeconds = (finish - start) / 1000.0f;

            lwc.sendLocale(sender, "protection.admin.cleanup.complete", "count", completed, "seconds", timeInSeconds);
        }

    }

}