package com.griefcraft.model;

import com.griefcraft.lwc.LWC;

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
