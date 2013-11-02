package org.getlwc.forge.client.gui;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.getlwc.forge.LWC;
import org.getlwc.forge.asm.AbstractMultiClassTransformer;
import org.getlwc.forge.asm.CompilationType;
import org.lwjgl.opengl.GL11;

import javax.swing.Icon;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiScreenWrapper extends GuiScreen {

    /**
     * The screen we are wrapping
     */
    protected GuiScreen screen;

    /**
     * The fontRenderer field in GuiScreen
     */
    protected Field fontRendererField;

    public GuiScreenWrapper(GuiScreen screen) {
        this.screen = screen;
        init();
    }

    /**
     * Initialize the wrapper
     */
    protected void init() {
        try {
            fontRendererField = GuiScreen.class.getDeclaredField(AbstractMultiClassTransformer.getFieldName("GuiScreen", "fontRenderer", CompilationType.SRG));
            fontRendererField.setAccessible(true);

            fontRenderer = (FontRenderer) fontRendererField.get(screen);

            this.width = screen.width;
            this.height = screen.height;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawScreen(int x, int y, float par3) {
        super.drawScreen(x, y, par3);
        screen.drawScreen(x, y, par3);
    }

    @Override
    public void keyTyped(char chr, int par2) {
        super.keyTyped(chr, par2);
        LWC.instance.getEngine().getConsoleSender().sendMessage(String.format("keyTyped(%c, %d)", chr, par2));
    }

    @Override
    public void mouseClicked(int x, int y, int type) {
        super.mouseClicked(x, y, type);
        LWC.instance.getEngine().getConsoleSender().sendMessage(String.format("mouseClicked(%d, %d, %d)", x, y, type));
    }

    @Override
    public void mouseMovedOrUp(int x, int y, int type) {
        super.mouseMovedOrUp(x, y, type);
    }

    @Override
    public void mouseClickMove(int x, int y, int type, long par4) {
        super.mouseClickMove(x, y, type, par4);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
    }

    @Override
    public void setWorldAndResolution(Minecraft minecraft, int width, int height) {
        super.setWorldAndResolution(minecraft, width, height);
        screen.setWorldAndResolution(minecraft, width, height);
    }

    @Override
    public void initGui() {
        screen.initGui();
    }

    @Override
    public void handleInput() {
        super.handleInput();
    }

    @Override
    public void handleKeyboardInput() {
        super.handleKeyboardInput();
        screen.handleKeyboardInput();
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        screen.handleMouseInput();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        screen.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        screen.onGuiClosed();
    }

    @Override
    public void drawDefaultBackground() {
        super.drawDefaultBackground();
        screen.drawDefaultBackground();
    }

    @Override
    public void drawWorldBackground(int par1) {
        screen.drawWorldBackground(par1);
    }

    @Override
    public void drawBackground(int par1) {
        screen.drawBackground(par1);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return screen.doesGuiPauseGame();
    }

    @Override
    public void confirmClicked(boolean par1, int par2) {
        screen.confirmClicked(par1, par2);
    }

}
