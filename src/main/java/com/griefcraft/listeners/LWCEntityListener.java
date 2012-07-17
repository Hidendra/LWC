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

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Flag;
import com.griefcraft.model.Protection;
import com.nitnelave.CreeperHeal.CreeperHeal;
import com.nitnelave.CreeperHeal.WorldConfig;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.plugin.Plugin;

public class LWCEntityListener implements Listener {

    /**
     * The plugin instance
     */
    private LWCPlugin plugin;

    public LWCEntityListener(LWCPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void entityInteract(EntityInteractEvent event) {
        Block block = event.getBlock();

        Protection protection = plugin.getLWC().findProtection(block);

        if (protection != null) {
            boolean allowEntityInteract = Boolean.parseBoolean(plugin.getLWC().resolveProtectionConfiguration(block.getType(), "allowEntityInteract"));

            if (!allowEntityInteract) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void entityBreakDoor(EntityBreakDoorEvent event) {
        Block block = event.getBlock();

        // See if there is a protection there
        Protection protection = plugin.getLWC().findProtection(block);

        if (protection != null) {
            // protections.allowEntityBreakDoor
            boolean allowEntityBreakDoor = Boolean.parseBoolean(plugin.getLWC().resolveProtectionConfiguration(block.getType(), "allowEntityBreakDoor"));

            if (!allowEntityBreakDoor) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!LWC.ENABLED || event.isCancelled()) {
            return;
        }
        
        LWC lwc = LWC.getInstance();

        for (Block block : event.blockList()) {
            Protection protection = plugin.getLWC().findProtection(block);

            if (protection != null) {
                boolean ignoreExplosions = Boolean.parseBoolean(lwc.resolveProtectionConfiguration(protection.getBlock().getType(), "ignoreExplosions"));

                if (ignoreExplosions || protection.hasFlag(Flag.Type.ALLOWEXPLOSIONS)) {
                    // If creeper heal is active for the block, halt all thrusters!
                    if (isCreeperHealActive(event.getEntity())) {
                        break;
                    }

                    protection.remove();
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Check if the CreeperHeal plugin is active. If it is, we shouldn't remove protections
     *
     * @return
     */
    private boolean isCreeperHealActive(Entity entity) {
        if (entity == null) {
            return false;
        }

        Plugin creeperHealPlugin = plugin.getServer().getPluginManager().getPlugin("CreeperHeal");

        if (creeperHealPlugin != null) {
            CreeperHeal creeperHeal = (CreeperHeal) creeperHealPlugin;
            WorldConfig worldConfig = creeperHeal.loadWorld(entity.getWorld());

            if (worldConfig == null) {
                return false; // Uh-oh?
            }

            if (entity instanceof Creeper) {
                return "true".equalsIgnoreCase(worldConfig.creepers);
            } else if (entity instanceof TNTPrimed) {
                return "true".equalsIgnoreCase(worldConfig.tnt);
            }
        }

        return false;
    }

}
