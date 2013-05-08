/*
 * Copyright (c) 2011-2013 Tyler Blair
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
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR,
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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.ModLoader;
import net.minecraft.world.WorldServer;
import org.getlwc.ServerLayer;
import org.getlwc.World;
import org.getlwc.command.Command;
import org.getlwc.entity.Player;
import org.getlwc.forge.entity.ForgePlayer;
import org.getlwc.forge.world.ForgeWorld;

import java.io.File;
import java.util.Map;

public class ForgeServerLayer extends ServerLayer {

    /**
     * The mod instance
     */
    private LWC mod;

    /**
     * The internal Minecraft map of commands
     */
    private Map internal_commands = null;

    public ForgeServerLayer() {
        mod = LWC.instance;
        try {
            initCommandManager();
        } catch (Throwable t) {
            System.err.println(" !!!! LWC is likely not compatible with this version of Minecraft. You need to update!");
            t.printStackTrace();
        }
    }

    /**
     * Initialize the command manager and hook into it. This resolves to NATIVE code during obfuscation.
     */
    public void initCommandManager() {
        MinecraftServer server = ModLoader.getMinecraftServerInstance();
        internal_commands = server.getCommandManager().getCommands();
    }

    @Override
    public void onRegisterBaseCommand(String baseCommand, Command command) {
        internal_commands.put(baseCommand, new NativeCommandHandler(baseCommand));
    }

    @Override
    public File getEngineHomeFolder() {
        return new File("coremods/LWC/");
    }

    @Override
    public World getDefaultWorld() {
        return internalGetWorld("world");
    }

    @Override
    protected Player internalGetPlayer(String playerName) {
        MinecraftServer server = ModLoader.getMinecraftServerInstance();

        try {
            // loop over the player list
            for (Object object : server.getConfigurationManager().playerEntityList) {
                // NATIVE
                if (object instanceof EntityPlayer) {
                    EntityPlayer handle = (EntityPlayer) object;

                    if (handle.getEntityName().equals(playerName)) {
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
        MinecraftServer server = ModLoader.getMinecraftServerInstance();

        try {
            // NATIVE
            for (WorldServer world_handle : server.worldServers) {
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
