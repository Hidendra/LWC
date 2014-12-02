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
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.util.config.Configuration;
import com.griefcraft.util.matchers.DoorMatcher;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class DoorsModule extends JavaModule {

    /**
     * The amount of server ticks there usually are per second
     */
    private final static int TICKS_PER_SECOND = 20;

    /**
     * The course of action for opening doors
     */
    private enum Action {

        /**
         * The door should automatically open and then close
         */
        OPEN_AND_CLOSE,

        /**
         * The doors should just be opened, not closed after a set amount of time
         */
        TOGGLE;

        /**
         * Resolve an Action using its case insensitive name
         *
         * @param name
         * @return
         */
        public static Action resolve(String name) {
            for (Action action : values()) {
                if (action.toString().equalsIgnoreCase(name)) {
                    return action;
                }
            }

            return null;
        }

    }

    /**
     * The configuration file
     */
    private final Configuration configuration = Configuration.load("doors.yml");

    /**
     * The LWC object, set by load ()
     */
    private LWC lwc;

    /**
     * The current action to use, default to toggling the door open and closed
     */
    private Action action = Action.TOGGLE;

    @Override
    public void load(LWC lwc) {
        this.lwc = lwc;
        loadAction();
    }

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() == Result.CANCEL || !isEnabled()) {
            return;
        }

        // The more important check
        if (!event.canAccess()) {
            return;
        }

        Protection protection = event.getProtection();
        Block block = event.getEvent().getClickedBlock(); // The block they actually clicked :)
        Player player = event.getPlayer();

        // Check if the block is even something that should be opened
        if (!isValid(block.getType())) {
            return;
        }

        // Are we looking at the top half?
        // If we are, we need to get the bottom half instead
        if (block.getType() != Material.TRAP_DOOR && (block.getData() & 0x8) == 0x8) {
            // Inspect the bottom half instead, fool!
            block = block.getRelative(BlockFace.DOWN);
        }

        // Should we look for double doors?
        boolean doubleDoors = usingDoubleDoors();

        // The BOTTOM half of the other side of the double door
        Block doubleDoorBlock = null;

        // Only waste CPU if we need the double door block
        if (doubleDoors) {
            doubleDoorBlock = getDoubleDoor(block);

            if (doubleDoorBlock != null) {
                Protection other = lwc.findProtection(doubleDoorBlock.getLocation());
                if (!lwc.canAccessProtection(player, other)) {
                    doubleDoorBlock = null; // don't open the other door :-)
                }
            }
        }

        // toggle the other side of the door open
        boolean opensWhenClicked = (DoorMatcher.WOODEN_DOORS.contains(block.getType()) || block.getType() == Material.FENCE_GATE || block.getType() == Material.TRAP_DOOR);
        changeDoorStates(true, (opensWhenClicked ? null : block) /* opens when clicked */, doubleDoorBlock);

        if (action == Action.OPEN_AND_CLOSE || protection.hasFlag(Flag.Type.AUTOCLOSE)) {
            // Abuse the fact that we still use final variables inside the task
            // The double door block object is initially only assigned if we need
            // it, so we just create a second variable ^^
            final Block finalBlock = block;
            final Block finalDoubleDoorBlock = doubleDoorBlock;

            // Calculate the wait time
            // This is basically Interval * TICKS_PER_SECOND
            int wait = getAutoCloseInterval() * TICKS_PER_SECOND;

            // Create the task
            // If we are set to close the door after a set period, let's create a sync task for it
            lwc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(lwc.getPlugin(), new Runnable() {
                public void run() {

                    // Essentially all we need to do is reset the door states
                    // But DO NOT open the door if it's closed !
                    changeDoorStates(false, finalBlock, finalDoubleDoorBlock);

                }
            }, wait);
        }

    }

    /**
     * Change all of the given door states to be inverse; that is, if a door is open, it will be closed afterwards.
     * If the door is closed, it will become open.
     * <p/>
     * Note that the blocks given must be the bottom block of the door.
     *
     * @param allowDoorToOpen If FALSE, and the door is currently CLOSED, it will NOT be opened!
     * @param doors Blocks given must be the bottom block of the door
     */
    private void changeDoorStates(boolean allowDoorToOpen, Block... doors) {
        for (Block door : doors) {
            if (door == null) {
                continue;
            }

            // If we aren't allowing the door to open, check if it's already closed
            if (!allowDoorToOpen && (door.getData() & 0x4) == 0) {
                // The door is already closed and we don't want to open it
                // the bit 0x4 is set when the door is open
                continue;
            }

            // Get the top half of the door
            Block topHalf = door.getRelative(BlockFace.UP);

            // Now xor both data values with 0x4, the flag that states if the door is open
            door.setData((byte) (door.getData() ^ 0x4));

            // Play the door open/close sound
            door.getWorld().playEffect(door.getLocation(), Effect.DOOR_TOGGLE, 0);

            // Only change the block above it if it is something we can open or close
            if (isValid(topHalf.getType())) {
                topHalf.setData((byte) (topHalf.getData() ^ 0x4));
            }
        }
    }

    /**
     * Get the double door for the given block
     *
     * @param block
     * @return
     */
    private Block getDoubleDoor(Block block) {
        if (!isValid(block.getType())) {
            return null;
        }

        Block found;

        for (Material material : DoorMatcher.PROTECTABLES_DOORS) {
            if ((found = lwc.findAdjacentBlock(block, material)) != null) {
                return found;
            }
        }

        return null;
    }

    /**
     * Check if automatic door opening is enabled
     *
     * @return
     */
    public boolean isEnabled() {
        return configuration.getBoolean("doors.enabled", true);
    }

    /**
     * Check if the material is auto openable/closable
     *
     * @param material
     * @return
     */
    private boolean isValid(Material material) {
        if (DoorMatcher.PROTECTABLES_DOORS.contains(material)) {
            return true;
        }

        return material == Material.FENCE_GATE || material == Material.TRAP_DOOR;
    }

    /**
     * Get the amount of seconds after opening a door it should be closed
     *
     * @return
     */
    private int getAutoCloseInterval() {
        return configuration.getInt("doors.interval", 3);
    }

    /**
     * Get if we are allowing double doors to be used
     *
     * @return
     */
    private boolean usingDoubleDoors() {
        return configuration.getBoolean("doors.doubleDoors", true);
    }

    /**
     * Load the action from the configuration
     */
    private void loadAction() {
        String strAction = configuration.getString("doors.action");

        if (strAction.equalsIgnoreCase("openAndClose")) {
            this.action = Action.OPEN_AND_CLOSE;
        } else {
            this.action = Action.TOGGLE;
        }
    }

}
