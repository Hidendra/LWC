package org.getlwc.forge.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.getlwc.forge.LWC;
import org.getlwc.forge.asm.AbstractMultiClassTransformer;
import org.getlwc.forge.asm.CompilationType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SideOnly(Side.CLIENT)
public class GuiScreenWrapper extends GuiScreen {

    /**
     * The screen we are wrapping
     */
    private GuiScreen screen;

    private Field fontRendererField;
    private Field xSizeField;
    private Field ySizeField;

    /**
     * The xSize of the container if it is a container
     */
    private int xSize;

    /**
     * The ySize of the container if it is a container
     */
    private int ySize;

    public GuiScreenWrapper(GuiScreen screen) {
        this.screen = screen;
        initMethods();
    }

    /**
     * Initialize all methods
     */
    private void initMethods() {
        try {
            fontRendererField = GuiScreen.class.getDeclaredField(AbstractMultiClassTransformer.getFieldName("GuiScreen", "fontRenderer", CompilationType.SRG));
            fontRendererField.setAccessible(true);
            xSizeField = GuiContainer.class.getDeclaredField(AbstractMultiClassTransformer.getFieldName("GuiContainer", "xSize", CompilationType.SRG));
            xSizeField.setAccessible(true);
            ySizeField = GuiContainer.class.getDeclaredField(AbstractMultiClassTransformer.getFieldName("GuiContainer", "ySize", CompilationType.SRG));
            ySizeField.setAccessible(true);

            fontRenderer = (FontRenderer) fontRendererField.get(screen);

            if (screen instanceof GuiContainer) {
                GuiContainer container = (GuiContainer) screen;
                xSize = xSizeField.getInt(container);
                ySize = ySizeField.getInt(container);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawScreen(int x, int y, float par3) {
        super.drawScreen(x, y, par3);
        screen.drawScreen(x, y, par3);

        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;

        drawString(fontRenderer, "xSize=" + xSize, left + 30, top + 30, 0);
        drawGradientRect(left - 100, top, 40, 8 + 150, 0xc0000000, 0xc0000000);
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
