package com.griefcraft;

import com.griefcraft.entity.Player;
import com.griefcraft.model.Protection;

/**
 * An access provider is an object that can say whether or not a player is allowed to access
 * a specific protection.
 */
public interface AccessProvider {

    /**
     * Get the access level to the given {@link Protection} for a {@link Player}
     * @param player
     * @return
     */
    public ProtectionAccess getAccess(Protection protection, Player player);

}
