package org.getlwc.forge.permission;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.ServerConfigurationManager;
import org.getlwc.entity.Player;
import org.getlwc.forge.asm.AbstractMultiClassTransformer;
import org.getlwc.forge.asm.CompilationType;
import org.getlwc.forge.entity.ForgePlayer;
import org.getlwc.permission.PermissionHandler;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

// might as well be no permission plugin
public class ForgePermissionHandler implements PermissionHandler {

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean hasPermission(Player player, String node) {
        if (!node.startsWith("lwc.mod") && !node.startsWith("lwc.admin")) {
            return true;
        } else if (node.startsWith("lwc.admin")) {
            return isOP(player);
        } else {
            return isOP(player);
        }
    }

    @Override
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
            ForgePlayer forgePlayer = (ForgePlayer) player;

            // accessing the method directly currently throws a compile error because
            // GameProfile cannot be resolved.
            try {
                Method isPlayerOpped = null; // (func_152596_g)

                for (Method method : ServerConfigurationManager.class.getDeclaredMethods()) {
                    if (method.getName().equals("func_152596_g")) {
                        isPlayerOpped = method;
                        break;
                    }
                }

                Method getGameProfile = EntityPlayer.class.getDeclaredMethod(AbstractMultiClassTransformer.getMethodName("EntityPlayer", "getGameProfile", CompilationType.SRG));

                // retrieve the player's profile
                Object gameProfile = getGameProfile.invoke(forgePlayer.getHandle());

                return (boolean) isPlayerOpped.invoke(server.getConfigurationManager(), gameProfile);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

}
