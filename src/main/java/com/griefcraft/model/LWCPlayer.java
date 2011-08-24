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
