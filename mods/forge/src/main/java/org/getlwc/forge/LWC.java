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

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.getlwc.Engine;
import org.getlwc.ItemStack;
import org.getlwc.World;
import org.getlwc.forge.asm.AbstractTransformer;
import org.getlwc.forge.asm.LWCCorePlugin;

import java.util.HashMap;
import java.util.Map;

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
    @SidedProxy(clientSide = "org.getlwc.forge.client.LWCClientProxy", serverSide = "org.getlwc.forge.server.LWCServerProxy")
    public static CommonProxy proxy;

    /**
     * The LWC engine
     */
    private Engine engine;

    /**
     * The server layer
     */
    private ForgeServerLayer layer;

    @Mod.PreInit
    public void preInit(FMLPreInitializationEvent event) {

    }

    @Mod.Init
    public void load(FMLInitializationEvent event) {
        if (!LWCCorePlugin.INITIALIZED) {
            System.out.println("!!! ====================== !!!");
            System.out.println("======  LWC MUST BE PLACED IN coremods/ !!");
            System.out.println("======  Without it inside of coremods you will be missing a lot of events!");
            System.out.println("======  LWC will not load further. Please take note and move it to coremods/ :-)");
            System.out.println("!!! ====================== !!!");
        }
    }

    @Mod.PostInit
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Mod.ServerStarting
    public void serverStarting(FMLServerStartingEvent event) {
        if (LWCCorePlugin.INITIALIZED) {
            proxy.init();
            layer.init();
            engine.onLoad();
        }
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
    public void setupServer(Engine engine, ForgeServerLayer layer) {
        if (this.engine != null) {
            throw new UnsupportedOperationException("LWC was already setup, and cannot be reset");
        }
        this.engine = engine;
        this.layer = layer;
        AbstractTransformer.init();
    }

    public ForgeServerLayer getServerLayer() {
        return layer;
    }

    public void setServerLayer(ForgeServerLayer layer) {
        this.layer = layer;
    }

    /**
     * Cast a map of enchantments to our native enchantment mappings
     *
     * @param enchantments
     * @return
     */
    public Map<Integer, Integer> castEnchantments(NBTTagList enchantments) {
        Map<Integer, Integer> ret = new HashMap<Integer, Integer>();

        for (int index = 0; index < enchantments.tagCount(); index ++) {
            NBTTagCompound compound = (NBTTagCompound) enchantments.tagAt(index);

            ret.put((int) compound.getShort("id"), (int) compound.getShort("lvl"));
        }

        return ret;
    }

    /**
     * Cast an item stack to our native ItemStack
     * @param item
     * @return
     */
    public ItemStack castItemStack(net.minecraft.item.ItemStack item) {
        if (item == null) {
            return null;
        }

        return new ItemStack(item.itemID, item.stackSize, (short) item.getItemDamage(), item.getMaxStackSize(), castEnchantments(item.getEnchantmentTagList()));
    }

}
