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
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;
import org.getlwc.Engine;
import org.getlwc.ItemStack;
import org.getlwc.SimpleEngine;
import org.getlwc.World;
import org.getlwc.event.server.ServerStartingEvent;
import org.getlwc.event.server.ServerStoppingEvent;
import org.getlwc.forge.asm.AbstractTransformer;
import org.getlwc.forge.asm.LWCCorePlugin;
import org.getlwc.forge.listeners.ForgeListener;
import org.getlwc.forge.permission.ForgePermissionHandler;

import java.util.HashMap;
import java.util.Map;

@Mod(modid = "LWC", name = "LWC", version = "0.0.1-SNAPSHOT", acceptableRemoteVersions = "*")
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
    private SimpleEngine engine;

    /**
     * The server layer
     */
    private ForgeServerLayer layer;

    /**
     * The event listener
     */
    private ForgeListener listener;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

    }

    @Mod.EventHandler
    public void load(FMLInitializationEvent event) {
        if (!LWCCorePlugin.INITIALIZED) {
            System.out.println("!!! ====================== !!!");
            System.out.println("======  LWC MUST BE PLACED IN coremods/ !!");
            System.out.println("======  Without it inside of coremods you will be missing a lot of events!");
            System.out.println("======  LWC will not load further. Please take note and move it to coremods/ :-)");
            System.out.println("!!! ====================== !!!");
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        if (LWCCorePlugin.INITIALIZED) {
            if (listener == null) {
                listener = new ForgeListener(this);
            }

            proxy.init();
            layer.init();
            engine.setPermissionHandler(new ForgePermissionHandler());
            engine.getEventBus().post(new ServerStartingEvent());
            MinecraftForge.EVENT_BUS.register(listener);
        }
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        if (LWCCorePlugin.INITIALIZED) {
            MinecraftForge.EVENT_BUS.unregister(listener);
            layer.clearCache();
            engine.getEventBus().post(new ServerStoppingEvent());
            engine = null;
        }
    }

    /**
     * Ensures LWC is loaded past the CoreMod phase and the LWC objects are correct for
     * the Mod stage.
     */
    public void ensurePostLoaded() {
        if (engine == null) {
            final Engine engine = SimpleEngine.getInstance();
            setupServer(engine, (ForgeServerLayer) engine.getServerLayer());
        }
    }

    /**
     * Wrap a native Canary player
     *
     * @param player
     * @return
     */
    public org.getlwc.entity.Player wrapPlayer(EntityPlayer player) {
        return layer.getPlayer(player.getCommandSenderName());
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
        this.engine = (SimpleEngine) engine;
        this.layer = layer;
        AbstractTransformer.init();
    }

    /**
     * Cast a map of enchantments to our native enchantment mappings
     *
     * @param enchantments
     * @return
     */
    public Map<Integer, Integer> castEnchantments(NBTTagList enchantments) {
        Map<Integer, Integer> ret = new HashMap<Integer, Integer>();

        for (int index = 0; index < enchantments.tagCount(); index++) {
            NBTTagCompound compound = enchantments.getCompoundTagAt(index);

            ret.put((int) compound.getShort("id"), (int) compound.getShort("lvl"));
        }

        return ret;
    }

    /**
     * Cast an item stack to our native ItemStack
     *
     * @param item
     * @return
     */
    public ItemStack castItemStack(net.minecraft.item.ItemStack item) {
        if (item == null) {
            return null;
        }

        int itemId = GameData.getItemRegistry().getId(item.getItem());

        return new ItemStack(itemId, item.stackSize, (short) item.getItemDamage(), item.getMaxStackSize(), castEnchantments(item.getEnchantmentTagList()));
    }

}
