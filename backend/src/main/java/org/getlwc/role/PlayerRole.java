package org.getlwc.role;

import org.getlwc.entity.Player;

import java.util.UUID;

public class PlayerRole extends AbstractRole {

    public static final String TYPE = "player";

    private UUID uuid;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String serialize() {
        return uuid.toString();
    }

    @Override
    public void deserialize(String value) {
        uuid = UUID.fromString(value);
    }

    @Override
    public boolean included(Player player) {
        return player.getUUID().equals(uuid);
    }

}
