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

import cpw.mods.fml.common.FMLCommonHandler;
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
import org.getlwc.command.Command;
import org.getlwc.command.CommandContext;
import org.getlwc.command.CommandException;
import org.getlwc.command.CommandSender;
import org.getlwc.event.server.ServerStartingEvent;
import org.getlwc.event.server.ServerStoppingEvent;
import org.getlwc.forge.asm.AbstractMultiClassTransformer;
import org.getlwc.forge.asm.AbstractTransformer;
import org.getlwc.forge.asm.LWCCorePlugin;
import org.getlwc.forge.asm.TransformerStatus;
import org.getlwc.forge.listeners.ForgeListener;
import org.getlwc.forge.permission.ForgePermissionHandler;

import java.util.HashMap;
import java.util.Map;

@Mod(modid = "LWC", name = "LWC", version = "0.0.1-SNAPSHOT", acceptableRemoteVersions = "*")
public class ForgeMod {

    // The instance of your mod that Forge uses.
    @Mod.Instance
    public static ForgeMod instance;

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
        ensureEngineLoaded();

        if (listener == null) {
            listener = new ForgeListener(this);
        }

        proxy.init();
        layer.init();

        MinecraftForge.EVENT_BUS.register(listener);
        FMLCommonHandler.instance().bus().register(listener); // required for FML events

        engine.setPermissionHandler(new ForgePermissionHandler());
        engine.getEventBus().subscribe(new EngineEventListener(engine, this));
        engine.getEventBus().post(new ServerStartingEvent());

        try {
            engine.getCommandHandler().registerCommands(this);
        } catch (CommandException e) {
            e.printStackTrace();
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

    @Command(
            command = "lwc transformers",
            description = "Shows the status of Forge transformers",
            permission = "lwc.admin"
    )
    public void commandTransformer(CommandContext context) {
        CommandSender sender = context.getCommandSender();

        int numSuccessful = 0;
        int numPending = 0;
        int numFailed = 0;

        for (Map.Entry<Class<? extends AbstractTransformer>, TransformerStatus> entry : AbstractMultiClassTransformer.TRANSFORMER_STATUSES.entrySet()) {
            Class<? extends AbstractTransformer> transformer = entry.getKey();
            TransformerStatus status = entry.getValue();

            switch (status) {
                case SUCCESSFUL:
                    numSuccessful ++;
                    break;

                case PENDING:
                    numPending ++;
                    break;

                case FAILED:
                    numFailed ++;
                    break;
            }
        }

        sender.sendMessage("Transformers: &2{0}&f successful, &e{1}&f pending, &4{2}&f failed", numSuccessful, numPending, numFailed);
        sender.sendMessage(" ");

        for (Map.Entry<Class<? extends AbstractTransformer>, TransformerStatus> entry : AbstractMultiClassTransformer.TRANSFORMER_STATUSES.entrySet()) {
            Class<? extends AbstractTransformer> transformer = entry.getKey();
            TransformerStatus status = entry.getValue();

            switch (status) {
                case SUCCESSFUL:
                    sender.sendMessage("&2{0}", transformer.getSimpleName());
                    break;

                case PENDING:
                    sender.sendMessage("&e{0}", transformer.getSimpleName());
                    break;

                case FAILED:
                    sender.sendMessage("&4{0}", transformer.getSimpleName());
                    break;
            }
        }
    }

    /**
     * Ensures LWC is loaded past the CoreMod phase and the LWC objects are correct for
     * the Mod stage.
     */
    public void ensureEngineLoaded() {
        if (SimpleEngine.getInstance() == null) {
            layer = new ForgeServerLayer();
            engine = (SimpleEngine) SimpleEngine.getOrCreateEngine(layer, new ForgeServerInfo(), new ForgeConsoleCommandSender());

            AbstractTransformer.init();
        } else {
            engine = SimpleEngine.getInstance();
            layer = (ForgeServerLayer) engine.getServerLayer();
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
