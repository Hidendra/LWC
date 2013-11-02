package org.getlwc.forge.client;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.getlwc.forge.LWC;
import org.getlwc.forge.client.gui.GuiContainerWrapper;
import org.getlwc.forge.client.gui.GuiScreenWrapper;

import java.util.EnumSet;

@SideOnly(Side.CLIENT)
public class ClientTickHandler implements ITickHandler {

    public void tickStart(EnumSet<TickType> tickTypes, Object... objects) {
        Minecraft minecraft = FMLClientHandler.instance().getClient();

        if (minecraft.currentScreen instanceof GuiContainer) {
            LWC.instance.getEngine().getConsoleSender().sendMessage(String.format("Wrapping screen: %s", minecraft.currentScreen.getClass().getCanonicalName()));

            GuiScreenWrapper wrapper = new GuiContainerWrapper((GuiContainer) minecraft.currentScreen);
            minecraft.displayGuiScreen(wrapper);
        }
    }

    public void tickEnd(EnumSet<TickType> tickTypes, Object... objects) {
        //
    }

    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.CLIENT);
    }

    public String getLabel() {
        return "lwc-client-gui";
    }
}
