package org.getlwc.role;

import org.getlwc.Engine;
import org.getlwc.entity.Player;
import org.getlwc.model.Protection;

public class PlayerRole extends ProtectionRole {

    public PlayerRole(Engine engine, String roleName) {
        super(engine, null, roleName, null);
    }

    public PlayerRole(Engine engine, Protection protection) {
        super(engine, protection, null, null);
    }

    public PlayerRole(Engine engine, Protection protection, String roleName, ProtectionRole.Access roleAccess) {
        super(engine, protection, roleName, roleAccess);
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
        if (player.getUUID().equalsIgnoreCase(getName())) {
            return getAccess();
        } else if (player.getName().equalsIgnoreCase(getName())) {
            setName(player.getUUID());
            save();
            return getAccess();
        } else {
            return Access.NONE;
        }
    }

}