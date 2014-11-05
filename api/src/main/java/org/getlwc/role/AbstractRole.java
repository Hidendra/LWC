package org.getlwc.role;

import org.getlwc.entity.Player;
import org.getlwc.model.Protection;

public abstract class AbstractRole implements Role {

    /**
     * The access the role has
     */
    private Protection.Access access;

    /**
     * Checks if a player is inside this role
     *
     * @param player
     * @return
     */
    public abstract boolean included(Player player);

    public Protection.Access getAccess(Protection protection, Player player) {
        if (included(player)) {
            return access;
        } else {
            return Protection.Access.NONE;
        }
    }

    public Protection.Access getAccess() {
        return access;
    }

    public void setAccess(Protection.Access access) {
        this.access = access;
    }

}
