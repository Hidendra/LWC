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

import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

public class LWCEntityListener extends EntityListener {

    /**
     * The plugin instance
     */
    private LWCPlugin plugin;

    /**
     * Blast radius for TNT / Creepers
     */
    public final static int BLAST_RADIUS = 4;

    public LWCEntityListener(LWCPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        boolean ignoreExplosions = plugin.getLWC().getConfiguration().getBoolean("core.ignoreExplosions", false);

        for (Block block : event.blockList()) {
            Protection protection = plugin.getLWC().getPhysicalDatabase().loadProtection(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());

            if (protection != null) {
                if(ignoreExplosions) {
                	protection.remove();
                } else {
                	event.setCancelled(true);
                }
            }
        }
    }

}
