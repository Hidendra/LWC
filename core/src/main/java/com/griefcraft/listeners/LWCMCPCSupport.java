package com.griefcraft.listeners;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.LWCPlayer;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LWCMCPCSupport extends JavaModule {

    /**
     * The shared multiplayer block id used by transport pipes
     */
    public static final int TRANSPORT_PIPE_ID = 166;

    /**
     * The LWC object
     */
    private LWC lwc;

    /**
     * A set of blacklisted players that are blocked from destroying any blocks protected by LWC. Mainly useful for MCPC
     * where mods can remove blocks and try to break the block by sending an event first (e.g turtle)
     */
    private final Set<String> blacklistedPlayers = new HashSet<String>();

    public LWCMCPCSupport(LWC lwc) {
        this.lwc = lwc;
        loadAndProcessConfig();
    }

    /**
     * Load and process the configuration
     */
    public void loadAndProcessConfig() {
        blacklistedPlayers.clear();

        for (String player : lwc.getConfiguration().getStringList("optional.blacklistedPlayers", new ArrayList<String>())) {
            blacklistedPlayers.add(player.toLowerCase());
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

}
