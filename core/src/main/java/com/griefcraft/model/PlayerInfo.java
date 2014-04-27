package com.griefcraft.model;

import java.util.UUID;

public class PlayerInfo {

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
    }

    public void setName(String name) {
        this.name = name;
    }
}
