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

package org.getlwc;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import net.minecraft.entity.player.EntityPlayer;
import org.getlwc.asm.AbstractTransformer;

/**
 * FORGE DONE WRONG
 */
@Mod(modid = "LWC", name = "LWC", version = "5.0.0-UNSTABLE")
@NetworkMod(clientSideRequired = false, serverSideRequired = true)
public class LWC {

    // The instance of your mod that Forge uses.
    @Mod.Instance
    public static LWC instance;

    // Says where the client and server 'proxy' code is loaded.
    @SidedProxy(clientSide = "org.getlwc.client.LWCClientProxy", serverSide = "org.getlwc.server.LWCServerProxy")
    public static CommonProxy proxy;

    /**
     * The LWC engine
     */
    private Engine engine;

    /**
     * The server layer
     */
    private ServerLayer layer;

    @Mod.PreInit
    public void preInit(FMLPreInitializationEvent event) {

    }

    @Mod.Init
    public void load(FMLInitializationEvent event) {

    }

    @Mod.PostInit
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Mod.ServerStarting
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.init();
    }

    /**
     * Wrap a native Canary player
     *
     * @param player
     * @return
     */
    public org.getlwc.entity.Player wrapPlayer(EntityPlayer player) {
        return layer.getPlayer(player.getEntityName());
    }

    /**
     * Get a World object for the native Canary world
     *
     * @param worldName
     * @return
     */
    public World getWorld(String worldName) {
        return layer.getWorld(worldName);
    }

    /**
     * Get the LWC engine
     *
     * @return
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * Set the engine
     *
     * @param engine
     */
    public void setupServer(Engine engine, ServerLayer layer) {
        if (this.engine != null) {
            throw new UnsupportedOperationException("LWC was already setup, and cannot be reset");
        }
        this.engine = engine;
        this.layer = layer;
        AbstractTransformer.init();
    }

}
