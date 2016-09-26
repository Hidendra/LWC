package com.griefcraft.model;

import com.griefcraft.lwc.LWC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerInfo extends AbstractSavable {

    /**
     * Internal id for the player
     */
    private int id;

    /**
     * The player's UUID
     */
    private UUID uuid;

    /**
     * The last known name for the player
     */
    private String name;

    /**
     * If the player info has been modified
     */
    private boolean modified = false;

    public PlayerInfo(int id, UUID uuid, String name) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("PlayerInfo(id = %d, uuid = %s, name = %s)", id, uuid != null ? uuid.toString() : "unknown", name != null ? name : "unknown");
    }

    /**
     * Get the Bukkit player represented by this player info. This only returns non-null if they are online.
     *
     * @return
     */
    public Player getBukkitPlayer() {
        if (uuid != null) {
            return Bukkit.getPlayer(uuid);
        } else {
            return Bukkit.getPlayer(name);
        }
    }

    /**
     * Checks if a player is equal to the player represented to this player info
     *
     * @param player
     * @return
     */
    public boolean equalTo(Player player) {
        if (uuid != null) {
            return uuid.equals(player.getUniqueId());
        } else {
            return name.equals(player.getName());
        }
    }

    /**
     * Pretty format the player's name. If the name is unknown, the UUID is returned.
     *
     * @return
     */
    public String prettyFormat() {
        return name != null ? name : uuid.toString();
    }

    public int getId() {
        return id;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
        modified = true;
    }

    public void setName(String name) {
        this.name = name;
        modified = true;
    }

    @Override
    public void saveNow() {
        LWC.getInstance().getPhysicalDatabase().savePlayerInfo(this);
    }

}
