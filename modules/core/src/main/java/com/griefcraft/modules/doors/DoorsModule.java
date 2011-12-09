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

package com.griefcraft.modules.doors;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Flag;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.util.config.Configuration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Unused localized nodes that are already in LWC:
 * - protection.doors.open "The door creaks open...."
 * - protection.doors.close "The door slams shut!"
 */
public class DoorsModule extends JavaModule {

    class DoorAction {
        // The door location
        public Location location;

        // when to close the door (ms)
        public long triggerTime;
    }

    enum Action {
        /**
         * The door will open and close after <interval> seconds.
         * It will only close the door if it was open; not re-open it.
         */
        OPEN_AND_CLOSE,

        /**
         * The door will open or close, depending on its state.
         * It will not automatically close itself.
         */
        TOGGLE,

        NULL
    }

    private Configuration configuration;

    // The triggered doors
    private LinkedList<DoorAction> doors;

    /**
     * The action to perform on doors
     */
    private Action action;

    /**
     * If applicable, the interval
     */
    private int interval;

    // if the module is even enabled
    private boolean enabled;

    private class DoorTask implements Runnable {
        public void run() {
            Iterator<DoorAction> iter = doors.iterator();

            while (iter.hasNext()) {
                DoorAction doorAction = iter.next();
                Location location = doorAction.location;

                if (System.currentTimeMillis() > doorAction.triggerTime) {
                    for (Block block : getDoorBlocks(location.getBlock())) {
                        toggleDoor(block);
                    }

                    iter.remove();
                }
            }
        }
    }

    @Override
    public void load(LWC lwc) {
        configuration = Configuration.load("doors.yml");
        doors = new LinkedList<DoorAction>();
        enabled = configuration.getBoolean("doors.enabled", true);

        String action = configuration.getString("doors.action", "toggle");

        if (action == null) {
            this.action = Action.NULL;
            return;
        }

        if (action.equalsIgnoreCase("openAndClose")) {
            this.action = Action.OPEN_AND_CLOSE;
        } else if (action.equalsIgnoreCase("toggle")) {
            this.action = Action.TOGGLE;
        }
        this.interval = configuration.getInt("doors.interval", 3);

        // start the task
        DoorTask doorTask = new DoorTask();
        lwc.getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(lwc.getPlugin(), doorTask, 20, 20);
    }

    /**
     * Check if a material is usable by this (doors.)
     *
     * @param material
     * @return
     */
    private boolean isValid(Material material) {
        // note: material.getId() is used for fence gate to retain support for 1.8.1
        // -hidendra
        return material == Material.IRON_DOOR_BLOCK || material == Material.WOODEN_DOOR || material.getId() == 107;
    }

    /**
     * Toggle a door between open and closed
     *
     * @param block
     */
    private void toggleDoor(Block block) {
        if (block == null || !isValid(block.getType())) {
            return;
        }

        block.setData((byte) (block.getData() ^ 0x4));
    }

    /**
     * Find both pieces of a door
     *
     * @param block
     * @return
     */
    private List<Block> getDoorBlocks(Block block) {
        List<Block> door = new ArrayList<Block>();
        Block temp;

        if (!isValid(block.getType())) {
            return door;
        }

        // ...
        door.add(block);

        // check the block above it, which is by default the bottom half of the door is normally ALWAYS given
        if (isValid((temp = block.getRelative(BlockFace.UP)).getType())) {
            door.add(temp);
        }

        // and now check below it, just incase
        else if (isValid((temp = block.getRelative(BlockFace.DOWN)).getType())) {
            door.add(temp);
        }

        return door;
    }

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() == Result.CANCEL) {
            return;
        }

        if (!event.canAccess()) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        Player player = event.getPlayer();
        LWCPlayer lwcPlayer = lwc.wrapPlayer(player);
        Action usingAction = this.action;

        // make sure we actually want this protection
        if (!isValid(Material.getMaterial(protection.getBlockId()))) {
            return;
        }

        // Should we go forward?
        if (!enabled && !protection.hasFlag(Flag.Type.AUTOCLOSE)) {
            return;
        }

        // if the protect has the flag, we want to close it after the interval, even if action = toggle
        if (protection.hasFlag(Flag.Type.AUTOCLOSE)) {
            usingAction = Action.OPEN_AND_CLOSE;
        }

        if (action == Action.NULL) {
            return;
        }

        // get the blocks for the door
        List<Block> blocks = new ArrayList<Block>();

        // the door they clicked on
        Block clickedDoor = lwc.getProtectionSet(protection.getBukkitWorld(), protection.getX(), protection.getY(), protection.getZ()).get(0);

        // add the bottom half of the door to the set
        blocks.add(clickedDoor);

        // if /lwc fix | /lwc fixdoor is being used
        boolean fixDoor = false;

        // check for the doorfix
        if (lwcPlayer.hasAction("fix")) {
            lwcPlayer.removeAction(lwcPlayer.getAction("fix"));
            fixDoor = true;
        }

        // search around for iron doors if enabled
        if (configuration.getBoolean("doors.doubleDoors", true)) {
            Block protectionBlock = protection.getBlock();
            Block temp;

            if (protectionBlock == null) {
                // Weird..
                return;
            }

            BlockFace[] faces = new BlockFace[]{
                    BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH
            };

            for (BlockFace face : faces) {
                temp = protectionBlock.getRelative(face);

                if (temp == null) {
                    continue;
                }

                if (isValid(temp.getType())) {
                    Protection found = lwc.findProtection(temp);

                    if (found == null) {
                        continue;
                    }

                    if (lwc.canAccessProtection(player, found)) {
                        // we can access it, add the bottom half of the found door
                        blocks.add(lwc.getProtectionSet(found.getBukkitWorld(), found.getX(), found.getY(), found.getZ()).get(0));
                    }
                }
            }
        }

        for (Block block : blocks) {
            if (!isValid(block.getType())) {
                continue;
            }

            // iterate through both door blocks
            int index = 0;

            for (Block door : getDoorBlocks(block)) {
                if ((block != clickedDoor && !fixDoor) || (block == clickedDoor && block.getType() == Material.IRON_DOOR_BLOCK)) {
                    toggleDoor(door);
                }

                switch (usingAction) {
                    case TOGGLE:
                        break;

                    case OPEN_AND_CLOSE:
                        // if we are fixing the door, we shouldn't toggle it again
                        if (index == 0 && !fixDoor) {
                            Location location = new Location(block.getWorld(), block.getX(), block.getY(), block.getZ());

                            DoorAction doorAction = new DoorAction();
                            doorAction.location = location;
                            doorAction.triggerTime = System.currentTimeMillis() + (interval * 1000L);

                            doors.push(doorAction);
                        }

                        break;
                }

                index++;
            }
        }
    }

}
