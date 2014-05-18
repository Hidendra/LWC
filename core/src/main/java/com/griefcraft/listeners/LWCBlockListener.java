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

package com.griefcraft.listeners;

import com.griefcraft.cache.ProtectionCache;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.scripting.event.LWCProtectionRegistrationPostEvent;
import com.griefcraft.scripting.event.LWCRedstoneEvent;
import com.griefcraft.util.Colors;
import com.griefcraft.util.matchers.DoubleChestMatcher;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.material.PistonBaseMaterial;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LWCBlockListener implements Listener {

    /**
     * The plugin instance
     */
    private LWCPlugin plugin;

    /**
     * A set of blacklisted blocks
     */
    private final Set<Integer> blacklistedBlocks = new HashSet<Integer>();

    public LWCBlockListener(LWCPlugin plugin) {
        this.plugin = plugin;
        loadAndProcessConfig();
    }

    @EventHandler
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Block block = event.getBlock();

        if (block == null) {
            return;
        }

        Protection protection = lwc.findProtection(block.getLocation());

        if (protection == null) {
            return;
        }

        LWCRedstoneEvent evt = new LWCRedstoneEvent(event, protection);
        lwc.getModuleLoader().dispatchEvent(evt);

        if (evt.isCancelled()) {
            event.setNewCurrent(event.getOldCurrent());
        }
    }

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = LWC.getInstance();
        // the blocks that were changed / replaced
        List<BlockState> blocks = event.getBlocks();

        for (BlockState block : blocks) {
            if (!lwc.isProtectable(block.getBlock())) {
                continue;
            }

            // we don't have the block id of the block before it
            // so we have to do some raw lookups (these are usually cache hits however, at least!)
            Protection protection = lwc.getPhysicalDatabase().loadProtection(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());

            if (protection != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!LWC.ENABLED || event.isCancelled()) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block == null) {
            return;
        }

        Protection protection = lwc.findProtection(block.getLocation());

        if (protection == null) {
            return;
        }

        boolean canAccess = lwc.canAccessProtection(player, protection);

        if (!canAccess) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!LWC.ENABLED || event.isCancelled()) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Player player = event.getPlayer();
        Block block = event.getBlock();

        boolean ignoreBlockDestruction = Boolean.parseBoolean(lwc.resolveProtectionConfiguration(block, "ignoreBlockDestruction"));

        if (ignoreBlockDestruction) {
            return;
        }

        ProtectionCache cache = lwc.getProtectionCache();
        String cacheKey = cache.cacheKey(block.getLocation());

        // In the event they place a block, remove any known nulls there
        if (cache.isKnownNull(cacheKey)) {
            cache.remove(cacheKey);
        }

        Protection protection = lwc.findProtection(block.getLocation());

        if (protection == null) {
            return;
        }

        boolean canAccess = lwc.canAccessProtection(player, protection);
        boolean canAdmin = lwc.canAdminProtection(player, protection);

        // when destroying a chest, it's possible they are also destroying a double chest
        // in the event they're trying to destroy a double chest, we should just move
        // the protection to the chest that is not destroyed, if it is not that one already.
        if (protection.isOwner(player) && DoubleChestMatcher.PROTECTABLES_CHESTS.contains(block.getType())) {
            Block doubleChest = lwc.findAdjacentDoubleChest(block);

            if (doubleChest != null) {
                // if they destroyed the protected block we want to move it aye?
                if (lwc.blockEquals(protection.getBlock(), block)) {
                    // correct the block
                    protection.setBlockId(doubleChest.getTypeId());
                    protection.setX(doubleChest.getX());
                    protection.setY(doubleChest.getY());
                    protection.setZ(doubleChest.getZ());
                    protection.saveNow();
                }

                // Repair the cache
                protection.radiusRemoveCache();

                if (protection.getProtectionFinder() != null) {
                    protection.getProtectionFinder().removeBlock(block);
                }

                lwc.getProtectionCache().addProtection(protection);

                return;
            }
        }

        try {
            LWCProtectionDestroyEvent evt = new LWCProtectionDestroyEvent(player, protection, LWCProtectionDestroyEvent.Method.BLOCK_DESTRUCTION, canAccess, canAdmin);
            lwc.getModuleLoader().dispatchEvent(evt);

            if (evt.isCancelled() || !canAccess) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            event.setCancelled(true);
            lwc.sendLocale(player, "protection.internalerror", "id", "BLOCK_BREAK");
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (!LWC.ENABLED || event.isCancelled()) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Block piston = event.getBlock();
        BlockState state = piston.getState();
        MaterialData data = state.getData();
        BlockFace direction = null;

        // Check the block it pushed directly
        if (data instanceof PistonBaseMaterial) {
            direction = ((PistonBaseMaterial) data).getFacing();
        }

        if (direction == null) {
            return;
        }

        // the block that the piston moved
        Block moved = piston.getRelative(direction, 2);

        // TODO remove this when spout fixes their shit
        if (moved.getType() == Material.WOODEN_DOOR || moved.getType() == Material.IRON_DOOR_BLOCK) {
            Block below = moved.getRelative(BlockFace.DOWN).getRelative(direction.getOppositeFace());

            if (lwc.findProtection(below.getLocation()) != null) {
                event.setCancelled(true);
                return;
            }
        }

        if (lwc.findProtection(moved.getLocation()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (!LWC.ENABLED || event.isCancelled()) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Block piston = event.getBlock();
        BlockState state = piston.getState();
        MaterialData data = state.getData();
        BlockFace direction = null;

        // Check the block it pushed directly
        if (data instanceof PistonBaseMaterial) {
            direction = ((PistonBaseMaterial) data).getFacing();
            Block block = event.getBlock().getRelative(direction);

            Protection protection = lwc.findProtection(block.getLocation());

            if (protection != null) {
                event.setCancelled(true);
                return;
            }
        }

        // if no direction was found, no point in going on
        if (direction == null) {
            return;
        }

        // Check the affected blocks
        for (int i = 0; i < event.getLength() + 2; i++) {
            Block block = piston.getRelative(direction, i);
            Protection protection = lwc.findProtection(block.getLocation());

            // We don't want that!
            if (block.getType() == Material.AIR) {
                break;
            }

            if (protection != null) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        ProtectionCache cache = lwc.getProtectionCache();
        String cacheKey = cache.cacheKey(block.getLocation());

        // In the event they place a block, remove any known nulls there
        if (cache.isKnownNull(cacheKey)) {
            cache.remove(cacheKey);
        }

        // check if the block is blacklisted
        boolean blockIsBlacklisted = blacklistedBlocks.contains(block.getTypeId()) || blacklistedBlocks.contains(hashCode(block.getTypeId(), block.getData()));

        if (blockIsBlacklisted) {
            // it's blacklisted, check for a protected chest
            for (Protection protection : lwc.findAdjacentProtectionsOnAllSides(block)) {
                if (protection != null) {
                    if (!lwc.canAccessProtection(player, protection) || (protection.getType() == Protection.Type.DONATION && !lwc.canAdminProtection(player, protection))) {
                        // they can't access the protection ..
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Used for auto registering placed protections
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlaceMonitor(BlockPlaceEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        // Update the cache if a protection is matched here
        Protection current = lwc.findProtection(block.getLocation());
        if (current != null) {
            if (!current.isBlockInWorld()) {
                // Corrupted protection
                lwc.log("Removing corrupted protection: " + current);
                current.remove();
            } else {
                if (current.getProtectionFinder() != null) {
                    current.getProtectionFinder().fullMatchBlocks();
                    lwc.getProtectionCache().addProtection(current);
                }

                return;
            }
        }

        // The placable block must be protectable
        if (!lwc.isProtectable(block)) {
            return;
        }

        String autoRegisterType = lwc.resolveProtectionConfiguration(block, "autoRegister");

        // is it auto protectable?
        if (!autoRegisterType.equalsIgnoreCase("private") && !autoRegisterType.equalsIgnoreCase("public")) {
            return;
        }

        if (!lwc.hasPermission(player, "lwc.create." + autoRegisterType, "lwc.create", "lwc.protect")) {
            return;
        }

        // Parse the type
        Protection.Type type;

        try {
            type = Protection.Type.valueOf(autoRegisterType.toUpperCase());
        } catch (IllegalArgumentException e) {
            // No auto protect type found
            return;
        }

        // Is it okay?
        if (type == null) {
            player.sendMessage(Colors.Red + "LWC_INVALID_CONFIG_autoRegister");
            return;
        }

        // If it's a chest, make sure they aren't placing it beside an already registered chest
        if (DoubleChestMatcher.PROTECTABLES_CHESTS.contains(block.getType())) {
            BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

            for (BlockFace blockFace : faces) {
                Block face = block.getRelative(blockFace);

                //They're placing it beside a chest, check if it's already protected
                if (face.getType() == block.getType()) {
                    if (lwc.findProtection(face.getLocation()) != null) {
                        return;
                    }
                }
            }
        }

        try {
            LWCProtectionRegisterEvent evt = new LWCProtectionRegisterEvent(player, block);
            lwc.getModuleLoader().dispatchEvent(evt);

            // something cancelled registration
            if (evt.isCancelled()) {
                return;
            }

            // All good!
            Protection protection = lwc.getPhysicalDatabase().registerProtection(block.getTypeId(), type, block.getWorld().getName(), player.getUniqueId().toString(), "", block.getX(), block.getY(), block.getZ());

            if (!Boolean.parseBoolean(lwc.resolveProtectionConfiguration(block, "quiet"))) {
                lwc.sendLocale(player, "protection.onplace.create.finalize", "type", lwc.getPlugin().getMessageParser().parseMessage(autoRegisterType.toLowerCase()), "block", LWC.materialToString(block));
            }

            if (protection != null) {
                lwc.getModuleLoader().dispatchEvent(new LWCProtectionRegistrationPostEvent(protection));
            }
        } catch (Exception e) {
            lwc.sendLocale(player, "protection.internalerror", "id", "PLAYER_INTERACT");
            e.printStackTrace();
        }
    }

    /**
     * Load and process the configuration
     */
    public void loadAndProcessConfig() {
        List<String> ids = LWC.getInstance().getConfiguration().getStringList("optional.blacklistedBlocks", new ArrayList<String>());

        for (String sId : ids) {
            String[] idParts = sId.trim().split(":");

            int id = Integer.parseInt(idParts[0].trim());
            int data = 0;

            if (idParts.length > 1) {
                data = Integer.parseInt(idParts[1].trim());
            }

            if (data == 0) {
                blacklistedBlocks.add(id);
            } else {
                blacklistedBlocks.add(hashCode(id, data));
            }
        }
    }

    /**
     * Get the hashcode of two integers
     *
     * @param int1
     * @param int2
     * @return
     */
    private int hashCode(int int1, int int2) {
        int hash = int1 * 17;
        hash *= 37 + int2;
        return hash;
    }

}
