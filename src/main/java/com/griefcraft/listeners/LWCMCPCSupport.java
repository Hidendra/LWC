package com.griefcraft.listeners;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LWCMCPCSupport extends JavaModule implements Listener {

    /**
     * The shared multiplayer block id used by transport pipes
     */
    public static final int TRANSPORT_PIPE_ID = 166;

    /**
     * The LWC object
     */
    private LWC lwc;

    /**
     * A set of blacklisted blocks
     */
    private final Set<Integer> blacklistedBlocks = new HashSet<Integer>();

    /**
     * A set of blacklisted players that are blocked from destroying any blocks protected by LWC. Mainly useful for MCPC
     * where mods can remove blocks and try to break the block by sending an event first (e.g turtle)
     */
    private final Set<String> blacklistedPlayers = new HashSet<String>();

    public LWCMCPCSupport(LWC lwc) {
        this.lwc = lwc;
        loadAndProcessConfig();
        registerEvents();
    }

    /**
     * Register the MCPC events
     */
    private void registerEvents() {
        Bukkit.getServer().getPluginManager().registerEvents(this, lwc.getPlugin());
    }

    /**
     * Load and process the configuration
     */
    public void loadAndProcessConfig() {
        blacklistedBlocks.clear();
        blacklistedPlayers.clear();

        for (String player : lwc.getConfiguration().getStringList("optional.blacklistedPlayers", new ArrayList<String>())) {
            blacklistedPlayers.add(player.toLowerCase());
        }

        List<String> ids = lwc.getConfiguration().getStringList("optional.blacklistedBlocks", new ArrayList<String>());

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
     * Check if MCPC is installed to the server (which Tekkit uses)
     *
     * @return
     */
    public boolean mcpcInstalled() {
        // quickest and easiest check
        if (Bukkit.getBukkitVersion().contains("MCPC")) {
            return true;
        }

        // attempt to detect Forge, tekkit definitely contains forge, most other MCPC mods are going to have it ?
        try {
            Class.forName("forge.MinecraftForge");
            return true;
        } catch (Exception e) { } // ignore failure

        return false; // no mcpc
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();

        // check if the block is blacklisted
        boolean blockIsBlacklisted = blacklistedBlocks.contains(block.getTypeId()) || blacklistedBlocks.contains(hashCode(block.getTypeId(), block.getData()));

        if (blockIsBlacklisted) {
            // it's blacklisted, check for a protected chest
            Protection protection = lwc.findAdjacentProtectionOnAllSides(block);

            if (protection != null) {
                if (!lwc.canAccessProtection(player, protection)) {
                    // they can't access the protection ..
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Called when a protection is destroyed
     *
     * @param event
     */
    public void onDestroyProtection(LWCProtectionDestroyEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player bPlayer = event.getPlayer();
        LWCPlayer player = lwc.wrapPlayer(bPlayer);
        String lowerPlayerName = player.getName().toLowerCase();

        if (blacklistedPlayers.contains(lowerPlayerName)) {
            event.setCancelled(true);
            player.sendLocale("protection.accessdenied");
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
