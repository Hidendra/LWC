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

package com.griefcraft.listeners;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.scripting.Module.Result;
import com.griefcraft.scripting.ModuleLoader.Event;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;

public class LWCBlockListener extends BlockListener {

    /**
     * The plugin instance
     */
    private LWCPlugin plugin;
    
    public LWCBlockListener(LWCPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Block block = event.getBlock();

        if (block == null) {
            return;
        }

        Protection protection = lwc.findProtection(block);

        if (protection == null) {
            return;
        }

        Result result = lwc.getModuleLoader().dispatchEvent(Event.REDSTONE, protection, block, event.getOldCurrent());

        if (result == Result.CANCEL) {
            event.setNewCurrent(event.getOldCurrent());
        }
    }

    @Override
    public void onSignChange(SignChangeEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block == null) {
            return;
        }

        Protection protection = lwc.findProtection(block);

        if (protection == null) {
            return;
        }

        boolean canAccess = lwc.canAccessProtection(player, protection);

        if (!canAccess) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();

        boolean ignoreBlockDestruction = Boolean.parseBoolean(lwc.resolveProtectionConfiguration(material, "ignoreBlockDestruction"));

        if (ignoreBlockDestruction) {
            return;
        }

        Protection protection = lwc.findProtection(block);

        if (protection == null) {
            return;
        }

        boolean canAccess = lwc.canAccessProtection(player, protection);
        boolean canAdmin = lwc.canAdminProtection(player, protection);

        Result result = lwc.getModuleLoader().dispatchEvent(Event.DESTROY_PROTECTION, player, protection, block, canAccess, canAdmin);

        if (result == Result.CANCEL) {
            event.setCancelled(true);
        }
    }

    /**
     * Used for auto registering placed protections
     */
    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        // check for an adjacent chest
        if (block.getType() == Material.CHEST) {
            if (lwc.findAdjacentDoubleChest(block) != null) {
                event.setCancelled(true);
                return;
            }
        }

        String autoRegisterType = plugin.getLWC().resolveProtectionConfiguration(block.getType(), "autoRegister");

        /*
           * Check if it's enabled
           */
        if (!autoRegisterType.equalsIgnoreCase("private") && !autoRegisterType.equalsIgnoreCase("public")) {
            return;
        }

        /**
         * Check permissions
         */
        if (!lwc.hasPermission(player, "lwc.create." + autoRegisterType, "lwc.create", "lwc.protect")) {
            lwc.sendLocale(player, "protection.accessdenied");
            return;
        }

        // default to public
        int type = ProtectionTypes.PUBLIC;

        if (autoRegisterType.equalsIgnoreCase("private")) {
            type = ProtectionTypes.PRIVATE;
        }

        /*
           * If the block isn't protectable, don't let them
           */
        if (!lwc.isProtectable(block)) {
            return;
        }
        
        Result registerProtection = lwc.getModuleLoader().dispatchEvent(Event.REGISTER_PROTECTION, player, block);

        // something cancelled registration
        if (registerProtection == Result.CANCEL) {
            return;
        }

        /*
           * If it's a chest, make sure they aren't trying to place it beside an already registered chest
           */
        if (block.getType() == Material.CHEST) {
            BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

            for (BlockFace blockFace : faces) {
                Block face = block.getFace(blockFace);

                /*
                     * They're placing it beside a chest, check if it's protected
                     */
                if (face.getType() == Material.CHEST) {
                    /*
                          * If it's protected, just return -- don't auto protect it
                          */
                    if (lwc.getPhysicalDatabase().loadProtection(face.getWorld().getName(), face.getX(), face.getY(), face.getZ()) != null) {
                        return;
                    }
                }
            }
        }

        /*
           * All's good, protect the object!
           */
        lwc.getPhysicalDatabase().registerProtection(block.getTypeId(), type, block.getWorld().getName(), player.getName(), "", block.getX(), block.getY(), block.getZ());

        /*
           * Tell them
           */
        lwc.sendLocale(player, "protection.onplace.create.finalize", "type", lwc.getLocale(autoRegisterType.toLowerCase()), "block", LWC.materialToString(block));
    }

}
