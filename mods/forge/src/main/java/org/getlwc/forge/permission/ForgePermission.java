package org.getlwc.forge.permission;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import org.getlwc.entity.Player;
import org.getlwc.permission.Permission;

import java.util.HashSet;
import java.util.Set;

// might as well be no permission plugin
public class ForgePermission implements Permission {

    public boolean isEnabled() {
        return true;
    }

    public boolean hasPermission(Player player, String node) {
        if (!node.startsWith("lwc.mod") && !node.startsWith("lwc.admin")) {
            return true;
        } else if (node.startsWith("lwc.admin")) {
            return isOP(player);
        } else {
            return isOP(player);
        }
    }

    public Set<String> getGroups(Player player) {
        return new HashSet<String>();
    }

    /**
     * Checks if a player is an OP. This is either an OP on a MP server or the owner of a LAN/SSP server
     *
     * @param player
     * @return
     */
    private boolean isOP(Player player) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

        if (server.isSinglePlayer()) {
            return server instanceof IntegratedServer && server.getServerOwner().equalsIgnoreCase(player.getName());
        } else {
            return server.getConfigurationManager().getOps().contains(player.getName().toLowerCase());
        }
    }

}
