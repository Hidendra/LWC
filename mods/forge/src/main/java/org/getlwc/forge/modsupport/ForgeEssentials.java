package org.getlwc.forge.modsupport;

import net.minecraft.entity.player.EntityPlayer;

public class ForgeEssentials {

    /**
     * Check a permission node on a player
     *
     * @param handle
     * @param node
     * @return
     */
    public static boolean checkPermission(EntityPlayer handle, String node) {
        try {
            Class.forName("com.ForgeEssentials.api.permissions.PermissionsAPI");

            com.ForgeEssentials.permission.query.PermQuery query = new com.ForgeEssentials.permission.query.PermQueryPlayer(handle, node);
            return com.ForgeEssentials.api.permissions.PermissionsAPI.checkPermAllowed(query);
        } catch (Exception e) { } // Not installed - OK

        return false;
    }

}
