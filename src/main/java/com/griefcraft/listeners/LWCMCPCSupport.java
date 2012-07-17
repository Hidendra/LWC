package com.griefcraft.listeners;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class LWCMCPCSupport implements Listener {

    /**
     * The shared multiplayer block id used by transport pipes
     */
    public static final int TRANSPORT_PIPE_ID = 166;

    /**
     * The LWC object
     */
    private LWC lwc;

    public LWCMCPCSupport(LWC lwc) {
        this.lwc = lwc;
    }

    /**
     * Check if MCPC is installed to the server (which Tekkit uses)
     *
     * @return
     */
    public boolean mcpcInstalled() {
        return Bukkit.getBukkitVersion().contains("MCPC");
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // if we matched the block to our own blacklist
        boolean blockIsBlacklisted = false;

        // transport pipes
        if (lwc.getConfiguration().getBoolean("optional.blockTransportPipes", true)) {
            if (block.getTypeId() == TRANSPORT_PIPE_ID) {
                blockIsBlacklisted = true;
            }
        }

        if (blockIsBlacklisted) {
            // it's blacklisted, check for a protected chest
            Block chest = lwc.findAdjacentBlock(block, Material.CHEST);

            if (chest != null) {
                Protection protection = lwc.findProtection(chest);

                if (protection != null) {
                    if (!lwc.canAccessProtection(player, protection)) {
                        // they can't access the protection ..
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

}
