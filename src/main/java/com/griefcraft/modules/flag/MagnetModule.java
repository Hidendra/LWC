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

package com.griefcraft.modules.flag;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Flag;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.config.Configuration;
import com.narrowtux.showcase.Showcase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MagnetModule extends JavaModule {

    private Configuration configuration = Configuration.load("magnet.yml");

    /**
     * The LWC object
     */
    private LWC lwc;

    /**
     * If this module is enabled
     */
    private boolean enabled = false;

    /**
     * The item blacklist
     */
    private List<Integer> itemBlacklist;

    /**
     * The radius around the container in which to suck up items
     */
    private int radius;

    /**
     * How many items to check each time
     */
    private int perSweep;

    /**
     * The current entity queue
     */
    private final Queue<Node> items = new LinkedList<Node>();

    /**
     * A queue of the items that need to be worked on
     */
    private final Queue<Item> work = new LinkedList<Item>();

    private final class Node {

        /**
         * The item that is to be picked up
         */
        final Item item;

        /**
         * The block that the item should be placed into
         */
        final Protection protection;

        public Node(Item item, Protection protection) {
            this.item = item;
            this.protection = protection;
        }

    }

    /**
     * Sync task, freshens the item queue when it is empty
     */
    private class MagnetRefreshTask implements Runnable {

        public void run() {
            Server server = Bukkit.getServer();

            if (work.size() == 0 && items.size() == 0) {
                for (World world : server.getWorlds()) {
                    for (Item item : world.getEntitiesByClass(Item.class)) {
                        work.add(item);
                    }
                }
            } else if (work.size() > 0) {
                Item item;
                int checked = 0;
                while ((item = work.poll()) != null) {
                    if (item.isDead()) {
                        continue;
                    }

                    if (checked > 500) {
                        break;
                    }
                    checked ++;

                    // native stack handle
                    ItemStack stack = item.getItemStack();

                    // check if it is in the blacklist
                    if (itemBlacklist.contains(stack.getTypeId())) {
                        continue;
                    }

                    // check if the item is valid
                    if (stack.getAmount() <= 0) {
                        continue;
                    }

                    // has the item been living long enough?
                    if (item.getPickupDelay() > item.getTicksLived()) {
                        continue; // a player wouldn't have had a chance to pick it up yet
                    }

                    // Check for usable blocks
                    Location location = item.getLocation();
                    Protection protection = lwc.getPhysicalDatabase().loadProtection(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), radius);

                    // nothing found
                    if (protection == null) {
                        continue;
                    }

                    if (!protection.hasFlag(Flag.Type.MAGNET)) {
                        continue;
                    }

                    // load the protection's block
                    protection.getBlock();

                    items.offer(new Node(item, protection));
                }
            }
        }
    }

    // does all of the work
    // searches the worlds for items and magnet chests nearby
    private class MagnetTask implements Runnable {
        public void run() {
            final Server server = Bukkit.getServer();
            final LWC lwc = LWC.getInstance();

            // Throttle amount of items polled
            int count = 0;
            Node node;

            while ((node = items.poll()) != null) {
                final Item item = node.item;
                final Protection protection = node.protection;
                final Block block = protection.getBlock();
                final World world = item.getWorld();

                if (item.isDead()) {
                    continue;
                }

                if (isShowcaseItem(item)) {
                    // it's being used by the Showcase plugin ... ignore it
                    continue;
                }

                if (block != null) {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            ItemStack itemStack = item.getItemStack();

                            // Remove the items and suck them up :3
                            Map<Integer, ItemStack> remaining = new HashMap<Integer, ItemStack>();

                            try {
                                remaining = lwc.depositItems(block, itemStack);
                            } catch (Exception e) {
                                lwc.log("Exception occurred while depositing into the block: " + block.toString());
                                e.printStackTrace();
                                return;
                            }

                            // we cancelled the item drop for some reason
                            if (remaining == null) {
                                return;
                            }

                            if (remaining.size() == 1) {
                                ItemStack other = remaining.values().iterator().next();

                                if (itemStack.getTypeId() == other.getTypeId() && itemStack.getAmount() == other.getAmount()) {
                                    return;
                                }
                            }

                            // remove the item on the ground
                            item.remove();

                            // if we have a remainder, we need to drop them
                            if (remaining.size() > 0) {
                                for (ItemStack stack : remaining.values()) {
                                    world.dropItemNaturally(item.getLocation(), stack);
                                }
                            }
                        }
                    };

                    server.getScheduler().scheduleSyncDelayedTask(lwc.getPlugin(), runnable);
                }

                // Time to throttle?
                if (count > perSweep) {
                    break;
                }

                count++;
            }
        }
    }

    /**
     * Check for the Showcase plugin and if it exists we also want to make sure the block doesn't have a showcase
     * on it.
     *
     * @param item
     * @return
     */
    private boolean isShowcaseItem(Item item) {
        if (item == null) {
            return false;
        }

        // check for the showcase plugin
        boolean hasShowcase = Bukkit.getServer().getPluginManager().getPlugin("Showcase") != null;

        if (hasShowcase) {
            return Showcase.instance.getItemByDrop(item) != null;
        }

        return false;
    }

    @Override
    public void load(LWC lwc) {
        this.lwc = lwc;
        enabled = configuration.getBoolean("magnet.enabled", false);
        itemBlacklist = new ArrayList<Integer>();
        radius = configuration.getInt("magnet.radius", 3);
        perSweep = configuration.getInt("magnet.perSweep", 20);

        if (!enabled) {
            return;
        }

        // get the item blacklist
        List<String> temp = configuration.getStringList("magnet.blacklist", new ArrayList<String>());

        for (String item : temp) {
            Material material = Material.matchMaterial(item);

            if (material != null) {
                itemBlacklist.add(material.getId());
            }
        }

        // register our search thread schedule
        // lwc.getPlugin().getServer().getScheduler().scheduleAsyncRepeatingTask(lwc.getPlugin(), new MagnetTask(), 50, 50);
        // lwc.getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(lwc.getPlugin(), new MagnetRefreshTask(), 50, 50);
    }

}