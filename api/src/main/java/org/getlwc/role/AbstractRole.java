package org.getlwc.role;

import org.getlwc.entity.Player;
import org.getlwc.model.Protection;

public abstract class AbstractRole implements Role {

    /**
     * The access the role has
     */
    private Protection.Access access;

    /**
     * If the access has changed
     */
    private boolean accessChanged;

    /**
     * Checks if a player is inside this role
     *
     * @param player
     * @return
     */
    public abstract boolean included(Player player);

    @Override
    public Protection.Access getAccess(Protection protection, Player player) {
        if (included(player)) {
            return access;
        } else {
            return Protection.Access.NONE;
        }
    }

    @Override
    public Protection.Access getAccess() {
        return access;
    }

    @Override
    public void setAccess(Protection.Access access) {
        this.access = access;
        accessChanged = true;
    }

    @Override
    public boolean accessChanged() {
        return accessChanged;
    }

    @Override
    public void markUnchanged() {
        accessChanged = false;
    }

}
