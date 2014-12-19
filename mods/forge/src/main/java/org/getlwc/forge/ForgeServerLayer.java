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
package org.getlwc.forge;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeVersion;
import org.getlwc.ServerLayer;
import org.getlwc.World;
import org.getlwc.entity.Player;
import org.getlwc.forge.entity.ForgePlayer;
import org.getlwc.forge.modsupport.BuildCraft;
import org.getlwc.forge.modsupport.ModSupport;
import org.getlwc.forge.world.ForgeWorld;

import java.io.File;
import java.util.UUID;

public class ForgeServerLayer extends ServerLayer {

    /**
     * The mod instance
     */
    private ForgeMod mod;

    public ForgeServerLayer() {
        mod = ForgeMod.instance;
    }

    /**
     * Initialize the server layer
     */
    public void init() {
        try {
            if (ModSupport.isModInstalled(ModSupport.Mod.BUILDCRAFT)) {
                BuildCraft.run(mod.getEngine());
            }
        } catch (Throwable t) {
            System.err.println(" !!!! LWC is likely not compatible with this version of Minecraft. You need to update!");
            t.printStackTrace();
        }
    }

    /**
     * Clear the internal server layer caches
     */
    protected void clearCache() {
        players.clear();
        worlds.clear();
    }

    @Override
    public UUID getOfflinePlayer(String ident) {
        Player player = getPlayer(ident);

        if (player != null) {
            return player.getUUID();
        }

        // TODO
        return null;
    }

    @Override
    public File getDataFolder() {
        String path = ForgeServerLayer.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        if (path.startsWith("file:")) {
            path = path.substring(5);
        }

        // drive letters (windows)
        if (path.charAt(0) == '\\' && path.charAt(2) == ':') {
            path = path.substring(3);
        } else if (path.charAt(1) == ':') {
            path = path.substring(2);
        }

        int index = path.indexOf(".jar!");

        if (index != -1) {
            path = path.substring(0, index + 4);
        }

        File runningFromJar = new File(path);
        return new File(new File(runningFromJar.getParentFile().getParent(), "config"), "LWC");
    }

    @Override
    public String getServerImplementationTitle() {
        return "Forge";
    }

    @Override
    public String getServerImplementationVersion() {
        return ForgeVersion.getVersion();
    }

    @Override
    public World getDefaultWorld() {
        return internalGetWorld("world");
    }

    @Override
    protected Player internalGetPlayer(String playerName) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

        try {
            // loop over the player list
            for (Object object : server.getConfigurationManager().playerEntityList) {
                // NATIVE
                if (object instanceof EntityPlayer) {
                    EntityPlayer handle = (EntityPlayer) object;

                    if (handle.getCommandSenderName().equals(playerName)) {
                        return new ForgePlayer(handle);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected World internalGetWorld(String worldName) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

        try {
            for (Object world_handle : server.worldServers) {
                // just create a world object to avoid duplicate native calls
                ForgeWorld world = new ForgeWorld(world_handle);

                if (world.getName().equals(worldName)) {
                    return world;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
