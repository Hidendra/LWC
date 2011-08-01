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

package com.griefcraft.modules.admin;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.StringUtils;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AdminCleanup extends JavaModule {

    @Override
    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
        if (!StringUtils.hasFlag(command, "a") && !StringUtils.hasFlag(command, "admin")) {
            return DEFAULT;
        }

        if (!args[0].equals("cleanup")) {
            return DEFAULT;
        }

        lwc.sendLocale(sender, "protection.admin.cleanup.start", "count", lwc.getPhysicalDatabase().getProtectionCount());

        // do the work in a separate thread so we don't fully lock the server
        // new Thread(new Admin_Cleanup_Thread(lwc, sender)).start();
        new Admin_Cleanup_Thread(lwc, sender).run();
        return CANCEL;
    }

    /**
     * Class that handles cleaning up the LWC database usage: /lwc admin cleanup
     */
    private static class Admin_Cleanup_Thread implements Runnable {

        private LWC lwc;
        private CommandSender sender;

        public Admin_Cleanup_Thread(LWC lwc, CommandSender sender) {
            this.lwc = lwc;
            this.sender = sender;
        }

        public void run() {
            Server server = sender.getServer();
            long start = System.currentTimeMillis();
            int completed = 0;
            List<Protection> protections = lwc.getPhysicalDatabase().loadProtections();
            List<Integer> ignore = new ArrayList<Integer>(); // list of protect ids to ignore

            Iterator<Protection> iterator = protections.iterator();

            // we need to batch the updates to the database
            try {
                lwc.getPhysicalDatabase().getConnection().setAutoCommit(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                while (iterator.hasNext()) {
                    Protection protection = iterator.next();

                    if (ignore.contains(protection.getId())) {
                        continue;
                    }

                    World world = protection.getBukkitWorld();

                    if (world == null) {
                        lwc.sendLocale(sender, "protection.admin.cleanup.noworld", "world", world.getName());
                        continue;
                    }

                    // now we can check the world for the protection
                    Block block = world.getBlockAt(protection.getX(), protection.getY(), protection.getZ());

                    // remove protections not found in the world
                    if (block == null || !lwc.isProtectable(block)) {
                        protection.remove();
                        // sender.sendMessage(Colors.Green + "Found:" + block.getType() + ". Removed protection #" + protection.getId() + " located in the world " + worldName);
                        lwc.sendLocale(sender, "protection.admin.cleanup.removednoexist", "protection", protection.toString());
                        completed++;
                    }

                    // remove excess protections
                    // (i.e, clean up the mess from conversions where most other protection plugins protected both chest blocks !!)
                    else {
                        List<Block> protectionSet = lwc.getProtectionSet(world, block.getX(), block.getY(), block.getZ());
                        List<Protection> tmpProtections = new ArrayList<Protection>();

                        for (Block protectableBlock : protectionSet) {
                            if (!lwc.isProtectable(protectableBlock)) {
                                continue;
                            }

                            // Protection tmp = lwc.getPhysicalDatabase().loadProtection(protectableBlock.getWorld().getName(), protectableBlock.getX(), protectableBlock.getY(), protectableBlock.getZ());
                            List<Protection> tmp = getAll(protections, protectableBlock.getWorld().getName(), protectableBlock.getX(), protectableBlock.getY(), protectableBlock.getZ());
                            tmpProtections.addAll(tmp);
                        }

                        if (tmpProtections.size() > 1) {
                            int toRemove = tmpProtections.size() - 1;

                            for (int i = 0; i < toRemove; i++) {
                                Protection remove = tmpProtections.get(i);

                                protection.remove();
                                lwc.sendLocale(sender, "protection.admin.cleanup.removeddupe", "protection", remove.toString());
                                completed++;

                                ignore.add(remove.getId());
                            }
                        }
                    }

                    iterator.remove();
                }
            } catch (Exception e) {
                sender.sendMessage("Uh-oh, something bad happened while cleaning up the LWC database!");
                lwc.sendLocale(sender, "protection.internalerror", "id", "cleanup");
                e.printStackTrace();
            }

            long finish = System.currentTimeMillis();
            float timeInSeconds = (finish - start) / 1000.0f;

            lwc.sendLocale(sender, "protection.admin.cleanup.complete", "count", completed, "seconds", timeInSeconds);

            // commit the updates
            try {
                lwc.getPhysicalDatabase().getConnection().commit();
                lwc.getPhysicalDatabase().getConnection().setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private List<Protection> getAll(List<Protection> protections, String world, int x, int y, int z) {
            List<Protection> tmp = new ArrayList<Protection>();

            for (Protection protection : protections) {
                if (protection.getWorld() != null && world != null && protection.getWorld().equals(world)) {
                    if (protection.getX() == x && protection.getY() == y && protection.getZ() == z) {
                        tmp.add(protection);
                    }
                }
            }

            return tmp;
        }

    }

}