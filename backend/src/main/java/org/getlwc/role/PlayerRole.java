package org.getlwc.role;

import org.getlwc.Engine;
import org.getlwc.entity.Player;
import org.getlwc.model.Protection;
import org.getlwc.model.State;

import java.util.UUID;

public class PlayerRole extends ProtectionRole {

    private UUID uuid;

    public PlayerRole(Engine engine, Protection protection) {
        super(engine, protection, null, null);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        this.uuid = UUID.fromString(name);
    }

    @Override
    public String getType() {
        // TODO remove
        return "lwc:rolePlayer";
    }

    @Override
    public boolean included(Player player) {
        return getName().equalsIgnoreCase(player.getName());
    }

    /**
     * {@inheritDoc}
     */
    public ProtectionRole.Access getAccess(Protection protection, Player player) {
        if (player.getUUID().equals(uuid)) {
            return getAccess();
        } else {
            return Access.NONE;
        }
    }

}