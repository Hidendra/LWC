/*
 * Copyright (c) 2011, 2012, Tyler Blair
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

import com.griefcraft.ServerLayer;
import com.griefcraft.World;
import com.griefcraft.entity.Player;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Field;

public class ForgeServerLayer extends ServerLayer {

    /**
     * The mod instance
     */
    private LWC mod;

    public ForgeServerLayer() {
        mod = LWC.instance;
    }

    @Override
    public World getDefaultWorld() {
        return internalGetWorld("world");
    }

    @Override
    protected Player internalGetPlayer(String playerName) {
        MinecraftServer server = ModLoader.getMinecraftServerInstance();

        try {
            // player handler
            Field player_handler_handle = MinecraftServer.class.getDeclaredField("t");
            player_handler_handle.setAccessible(true);

            // NATIVE
            gm player_handler = (gm) player_handler_handle.get(server);

            // loop over the player list
            for (Object object : player_handler.b) {
                // NATIVE
                if (object instanceof qx) {
                    qx handle = (qx) object;

                    if (handle.bQ.equals(playerName)) {
                        System.out.println("RET");
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
            for (xv world_handle : server.c) {
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
