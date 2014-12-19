/**
 * Copyright (c) 2011-2014 Tyler Blair
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */
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
        return new HashSet<>();
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
