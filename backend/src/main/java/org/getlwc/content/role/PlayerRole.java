package org.getlwc.content.role;

import org.getlwc.entity.Player;
import org.getlwc.role.AbstractRole;

import java.util.UUID;

public class PlayerRole extends AbstractRole {

    public static final String TYPE = "player";

    private UUID uuid;

    public PlayerRole(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String serialize() {
        return uuid.toString();
    }

    @Override
    public boolean included(Player player) {
        return player.getUUID().equals(uuid);
    }

}
