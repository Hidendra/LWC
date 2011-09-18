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
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LWCPlayer implements CommandSender {

    /**
     * The LWC instance
     */
    private LWC lwc;

    /**
     * The player instance
     */
    private Player player;

    /**
     * Cache of LWCPlayer objects
     */
    private final static Map<Player, LWCPlayer> playerCache = new HashMap<Player, LWCPlayer>();

    /**
     * The modes bound to all players
     */
    private final static Map<LWCPlayer, Set<Mode>> modes = Collections.synchronizedMap(new HashMap<LWCPlayer, Set<Mode>>());

    /**
     * The actions bound to all players
     */
    private final static Map<LWCPlayer, Set<Action>> actions = Collections.synchronizedMap(new HashMap<LWCPlayer, Set<Action>>());

    /**
     * Map of protections a player can temporarily access
     */
    private final static Map<LWCPlayer, Set<Protection>> accessibleProtections = Collections.synchronizedMap(new HashMap<LWCPlayer, Set<Protection>>());

    public LWCPlayer(LWC lwc, Player player) {
        this.lwc = lwc;
        this.player = player;
    }

    /**
     * Get the LWCPlayer object from a Player object
     *
     * @param player
     * @return
     */
    public static LWCPlayer getPlayer(Player player) {
        if (!playerCache.containsKey(player)) {
            playerCache.put(player, new LWCPlayer(LWC.getInstance(), player));
        }

        return playerCache.get(player);
    }

    /**
     * Remove a player from the player cache
     * 
     * @param player
     */
    public static void removePlayer(Player player) {
        LWCPlayer lwcPlayer = getPlayer(player);

        // remove everything accessible by them
        if (lwcPlayer != null) {
            modes.remove(lwcPlayer);
            actions.remove(lwcPlayer);
            accessibleProtections.remove(lwcPlayer);
        }

        // uncache them
        playerCache.remove(player);
    }

    /**
     * @return the Bukkit Player object
     */
    public Player getBukkitPlayer() {
        return player;
    }

    /**
     * @return the player's name
     */
    public String getName() {
        return player.getName();
    }

    /**
     * Enable a mode on the player
     *
     * @param mode
     * @return
     */
    public boolean enableMode(Mode mode) {
        return getModes().add(mode);
    }

    /**
     * Disable a mode on the player
     *
     * @param mode
     * @return
     */
    public boolean disableMode(Mode mode) {
        return getModes().remove(mode);
    }

    /**
     * Disable all modes enabled by the player
     *
     * @return
     */
    public void disableAllModes() {
        getModes().clear();
    }

    /**
     * Check if the player has an action
     *
     * @param name
     * @return
     */
    public boolean hasAction(String name) {
        return getAction(name) != null;
    }

    /**
     * Get the action represented by the name
     *
     * @param name
     * @return
     */
    public Action getAction(String name) {
        for (Action action : getActions()) {
            if (action.getName().equals(name)) {
                return action;
            }
        }

        return null;
    }

    /**
     * Add an action
     *
     * @param action
     * @return
     */
    public boolean addAction(Action action) {
        return getActions().add(action);
    }

    /**
     * Remove an action
     * 
     * @param action
     * @return
     */
    public boolean removeAction(Action action) {
        return getActions().remove(action);
    }

    /**
     * Remove all actions
     */
    public void removeAllActions() {
        getActions().clear();
    }

    /**
     * Retrieve a Mode object for a player
     *
     * @param name
     * @return
     */
    public Mode getMode(String name) {
        for (Mode mode : getModes()) {
            if (mode.getName().equals(name)) {
                return mode;
            }
        }

        return null;
    }

    /**
     * Check if the player has the given mode
     *
     * @param name
     * @return
     */
    public boolean hasMode(String name) {
        return getMode(name) != null;
    }

    /**
     * @return the Set of modes the player has activated
     */
    public Set<Mode> getModes() {
        if (!modes.containsKey(this)) {
            modes.put(this, new HashSet<Mode>());
        }

        return modes.get(this);
    }

    /**
     * @return the Set of actions the player has
     */
    public Set<Action> getActions() {
        if (!actions.containsKey(this)) {
            actions.put(this, new HashSet<Action>());
        }

        return actions.get(this);
    }

    /**
     * @return a Set containing all of the action names
     */
    public Set<String> getActionNames() {
        Set<Action> actions = getActions();
        Set<String> names = new HashSet<String>(actions.size());

        for (Action action : actions) {
            names.add(action.getName());
        }

        return names;
    }

    /**
     * @return the set of protections the player can temporarily access
     */
    public Set<Protection> getAccessibleProtections() {
        if (!accessibleProtections.containsKey(this)) {
            accessibleProtections.put(this, new HashSet<Protection>());
        }

        return accessibleProtections.get(this);
    }

    /**
     * Add an accessible protection for the player
     *
     * @param protection
     * @return
     */
    public boolean addAccessibleProtection(Protection protection) {
        return getAccessibleProtections().add(protection);
    }

    /**
     * Remove an accessible protection from the player
     *
     * @param protection
     * @return
     */
    public boolean removeAccessibleProtection(Protection protection) {
        return getAccessibleProtections().remove(protection);
    }

    /**
     * Remove all accessible protections
     */
    public void removeAllAccessibleProtections() {
        getAccessibleProtections().clear();
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

    public void sendMessage(String s) {
        player.sendMessage(s);
    }

    public Server getServer() {
        return player.getServer();
    }

    public boolean isPermissionSet(String s) {
        return player.isPermissionSet(s);
    }

    public boolean isPermissionSet(Permission permission) {
        return player.isPermissionSet(permission);
    }

    public boolean hasPermission(String s) {
        return player.hasPermission(s);
    }

    public boolean hasPermission(Permission permission) {
        return player.hasPermission(permission);
    }

    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
        return player.addAttachment(plugin, s, b);
    }

    public PermissionAttachment addAttachment(Plugin plugin) {
        return player.addAttachment(plugin);
    }

    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
        return player.addAttachment(plugin, s, b, i);
    }

    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        return player.addAttachment(plugin, i);
    }

    public void removeAttachment(PermissionAttachment permissionAttachment) {
        player.removeAttachment(permissionAttachment);
    }

    public void recalculatePermissions() {
        player.recalculatePermissions();
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return player.getEffectivePermissions();
    }

    public boolean isOp() {
        return player.isOp();
    }

    public void setOp(boolean b) {
        player.setOp(b);
    }
}
