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

package com.griefcraft.modules.doors;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.util.config.Configuration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Door;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
                    Block block = location.getBlock();

                    Door door = new Door(block.getType(), block.getData());
                    byte data = initializeDoorData(door);

                    // Close the door
                    if (isDoorOpen(door)) {
                        if ((block.getData() & 0x4) != 0x4) {
                            data |= 0x4;
                        }
                    }

                    block.setData(data);
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

        String action = configuration.getString("doors.action");

        if (action == null) {
            this.action = Action.NULL;
            return;
        }

        if (action.equalsIgnoreCase("openAndClose")) {
            this.action = Action.OPEN_AND_CLOSE;
            this.interval = configuration.getInt("doors.interval", 3);
        } else if (action.equalsIgnoreCase("toggle")) {
            this.action = Action.TOGGLE;
        }

        // start the task
        DoorTask doorTask = new DoorTask();
        lwc.getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(lwc.getPlugin(), doorTask, 20, 20);
    }

    /**
     * Scrape data from a doot
     *
     * @param door
     * @return
     */
    private byte initializeDoorData(Door door) {
        byte data = 0x00;

        // get the hinge position
        switch (door.getHingeCorner()) {
            case NORTH_EAST:
                data |= 0x0;
                break;

            case SOUTH_EAST:
                data |= 0x1;
                break;

            case SOUTH_WEST:
                data |= 0x2;
                break;

            case NORTH_WEST:
                data |= 0x3;
                break;
        }

        if (door.isTopHalf()) {
            data |= 0x8;
        }

        return data;
    }

    /**
     * @param door
     * @return
     */
    private boolean isDoorOpen(Door door) {
        switch (door.getHingeCorner()) {
            default:
                return door.isOpen();
        }
    }

    /**
     * Check if a material is usable by this (doors.)
     *
     * @param material
     * @return
     */
    private boolean isValid(Material material) {
        return material == Material.IRON_DOOR_BLOCK || material == Material.WOODEN_DOOR;
    }

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if(event.getResult() == Result.CANCEL) {
            return;
        }

        if (!enabled || action == Action.NULL) {
            return;
        }

        if (!event.canAccess()) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        Player player = event.getPlayer();

        // get the blocks for the door
        List<Block> blocks = lwc.getProtectionSet(protection.getBukkitWorld(), protection.getX(), protection.getY(), protection.getZ());

        // ignore the door they clicked if it's a wooden door
        if(protection.getBlockId() == Material.WOODEN_DOOR.getId()) {
            blocks.clear();
        }

        // only send them one message :-)
        boolean sentMessage = false;

        // search around for iron doors if enabled
        if (configuration.getBoolean("doors.doubleDoors", true)) {
            Block protectionBlock = protection.getBlock();
            Block temp;

            BlockFace[] faces = new BlockFace[]{
                    BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH
            };

            for (BlockFace face : faces) {
                if (isValid((temp = protectionBlock.getRelative(face)).getType())) {
                    Protection found = lwc.findProtection(temp);

                    if (found == null) {
                        continue;
                    }

                    if (lwc.canAccessProtection(player, found)) {
                        // we can access it, add it to the blocks
                        blocks.addAll(lwc.getProtectionSet(found.getBukkitWorld(), found.getX(), found.getY(), found.getZ()));
                    }
                }
            }
        }

        for (Block block : blocks) {
            if (!isValid(block.getType())) {
                continue;
            }

            // create the door instance
            Door door = new Door(block.getType(), block.getData());

            // process the current door's data
            byte data = initializeDoorData(door);

            switch (this.action) {
                case TOGGLE:

                    if ((block.getData() & 0x4) != 0x4) {
                        data |= 0x4;
                    }

                    if (!sentMessage) {
                        sentMessage = true;

                        if (isDoorOpen(door)) {
                            // lwc.sendLocale(player, "protection.doors.open");
                        } else {
                            // lwc.sendLocale(player, "protection.doors.close");
                        }
                    }

                    break;

                case OPEN_AND_CLOSE:

                    if ((block.getData() & 0x4) != 0x4) {
                        data |= 0x4;
                    }

                    if (!sentMessage) {
                        sentMessage = true;

                        if (isDoorOpen(door)) {
                            // lwc.sendLocale(player, "protection.doors.open");
                        } else {
                            // lwc.sendLocale(player, "protection.doors.close");
                        }
                    }

                    if (!isDoorOpen(door)) {
                        Location location = new Location(block.getWorld(), block.getX(), block.getY(), block.getZ());

                        DoorAction doorAction = new DoorAction();
                        doorAction.location = location;
                        doorAction.triggerTime = System.currentTimeMillis() + (interval * 1000L);

                        doors.push(doorAction);
                    }

                    break;
            }

            // update the door
            block.setData(data);
        }

        return;
    }

}
