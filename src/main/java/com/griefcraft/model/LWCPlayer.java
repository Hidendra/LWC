package com.griefcraft.model;

import com.griefcraft.lwc.LWC;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LWCPlayer {

    private LWC lwc;
    private Player player;

    public LWCPlayer(LWC lwc, Player player) {
        this.lwc = lwc;
        this.player = player;
    }

    /**
     * Create a History object that is attached to this protection
     *
     * @return
     */
    public History createHistoryObject() {
        History history = new History();

        history.setPlayer(player.getName());
        history.setStatus(History.Status.INACTIVE);

        return history;
    }

    /**
     * Send a locale to the player
     * 
     * @param key
     * @param args
     */
    public void sendLocale(String key, Object... args) {
        lwc.sendLocale(player, key, args);
    }

    /**
     * Get the player's history
     *
     * @return
     */
    public List<History> getRelatedHistory() {
        return lwc.getPhysicalDatabase().loadHistory(player);
    }

    /**
     * Get the player's history pertaining to the type
     *
     * @param type
     * @return
     */
    public List<History> getRelatedHistory(History.Type type) {
        List<History> related = new ArrayList<History>();

        for(History history : getRelatedHistory()) {
            if(history.getType() == type) {
                related.add(history);
            }
        }

        return related;
    }

}
