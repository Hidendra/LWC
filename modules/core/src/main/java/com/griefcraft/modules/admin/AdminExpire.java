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
import com.griefcraft.sql.PhysDB;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.command.CommandSender;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AdminExpire extends JavaModule {

    @Override
    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
        if (!StringUtils.hasFlag(command, "a") && !StringUtils.hasFlag(command, "admin")) {
            return DEFAULT;
        }

        if (!args[0].equals("expire")) {
            return DEFAULT;
        }

        if (args.length < 2) {
            lwc.sendSimpleUsage(sender, "/lwc admin expire <time>");
            return CANCEL;
        }

        boolean shouldRemoveBlocks = args[1].endsWith("remove");
        String toParse = StringUtils.join(args, shouldRemoveBlocks ? 2 : 1);
        long time = StringUtils.parseTime(toParse);

        if (time == 0L) {
            lwc.sendLocale(sender, "protection.admin.expire.invalidtime");
            return CANCEL;
        }

        int threshold = (int) ((System.currentTimeMillis() / 1000L) - time);
        int count = 0;
        int completed = 0;

        List<Integer> toRemove = new LinkedList<Integer>();
        List<Block> removeBlocks = null;
        int totalProtections = lwc.getPhysicalDatabase().getProtectionCount();

        if (shouldRemoveBlocks) {
            removeBlocks = new LinkedList<Block>();
        }

        sender.sendMessage("Loading protections via STREAM mode");

        try {
            Statement resultStatement = lwc.getPhysicalDatabase().getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            resultStatement.setFetchSize(Integer.MIN_VALUE);

            String prefix = lwc.getPhysicalDatabase().getPrefix();
            ResultSet result = resultStatement.executeQuery("SELECT " + prefix + "protections.id AS protectionId, " + prefix + "protections.type AS protectionType, x, y, z, flags, blockId, world, owner, password, date, last_accessed FROM " + prefix + "protections WHERE last_accessed <= " + threshold + " AND last_accessed > 0");

            while (result.next()) {
                Protection protection = lwc.getPhysicalDatabase().resolveProtectionNoRights(result);
                World world = protection.getBukkitWorld();

                count++;

                if (count % 100000 == 0 || count == totalProtections || count == 1) {
                    sender.sendMessage(Colors.Red + count + " / " + totalProtections);
                }

                if (world == null) {
                    continue;
                }

                // remove the protection
                toRemove.add(protection.getId());

                // remove the block ?
                if(shouldRemoveBlocks) {
                    removeBlocks.add(protection.getBlock());
                }

                completed ++;
            }

            // Close the streaming statement
            result.close();
            resultStatement.close();

            // flush all of the queries
            push(lwc, sender, toRemove);

            if(shouldRemoveBlocks) {
                removeBlocks(lwc, sender, removeBlocks);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ;
        }

        // reset the cache
        if (completed > 0) {
            LWC.getInstance().getCaches().getProtections().clear();
            LWC.getInstance().getPhysicalDatabase().precache();
        }

        lwc.sendLocale(sender, "protection.admin.expire.removed", "count", count);

        return CANCEL;
    }

    /**
     * Remove a list of blocks from the world
     * 
     * @param lwc
     * @param sender
     * @param blocks
     */
    private void removeBlocks(LWC lwc, CommandSender sender, List<Block> blocks) {
        int count = 0;

        for(Block block : blocks) {
            if(block == null || !lwc.isProtectable(block)) {
                continue;
            }

            // possibility of a double chest
            if(block.getType() == Material.CHEST) {
                Block doubleChest = lwc.findAdjacentBlock(block, Material.CHEST);

                if(doubleChest != null) {
                    removeInventory(doubleChest);
                    doubleChest.setType(Material.AIR);
                }
            }

            // remove the inventory from the block if it has one
            removeInventory(block);

            // and now remove the block
            block.setType(Material.AIR);

            count ++;
        }

        sender.sendMessage("Removed " + count + " blocks from the world");
    }

    /**
     * Remove the inventory from a block
     *
     * @param block
     */
    private void removeInventory(Block block) {
        if(block == null) {
            return;
        }

        if(!(block.getState() instanceof ContainerBlock)) {
            return;
        }

        ContainerBlock container = (ContainerBlock) block.getState();
        container.getInventory().clear();
    }

    /**
     * Push removal changes to the database
     *
     * @param toRemove
     */
    public void push(LWC lwc, CommandSender sender, List<Integer> toRemove) throws SQLException {
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
                builder.append("DELETE FROM " + prefix + "protections WHERE id IN (" + protectionId);
            } else {
                builder.append("," + protectionId);
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

}