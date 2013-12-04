package org.getlwc.permission;

import org.getlwc.entity.Player;

import java.util.Set;

public interface PermissionHandler {

    /**
     * Get the name of the permission handler
     *
     * @return
     */
    public String getName();

    /**
     * Check if the permission endpoint is enabled and usable
     *
     * @return
     */
    public boolean isEnabled();

    /**
     * Check if a player has a given permission
     *
     * @param node
     * @return
     */
    public boolean hasPermission(Player player, String node);

    /**
     * Get the groups a player is in (if supported)
     *
     * @param player
     * @return
     */
    public Set<String> getGroups(Player player);

}
