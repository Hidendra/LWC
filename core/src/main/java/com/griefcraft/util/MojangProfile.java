package com.griefcraft.util;

import java.util.UUID;

public class MojangProfile {

    /**
     * The UUID for the profile
     */
    private UUID uuid;

    /**
     * The name of the user for the profile
     */
    private String name;

    public MojangProfile(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("MojangProfile(id = %s, name = %s)", uuid.toString(), name);
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
