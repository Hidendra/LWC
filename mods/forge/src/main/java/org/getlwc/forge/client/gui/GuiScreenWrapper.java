package org.getlwc.forge.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.getlwc.forge.asm.AbstractMultiClassTransformer;
import org.getlwc.forge.asm.CompilationType;

import java.lang.reflect.Method;

@SideOnly(Side.CLIENT)
public class GuiScreenWrapper extends GuiScreen {

    /**
     * The screen we are wrapping
     */
    private GuiScreen screen;

    private Method methodKeyTyped;
    private Method methodMouseClicked;
    private Method methodMouseMovedOrUp;
    private Method methodMouseClickMove;
    private Method methodActionPerformed;

    public GuiScreenWrapper(GuiScreen screen) {
        this.screen = screen;
        initMethods();
    }

    /**
     * Initialize all methods
     */
    private void initMethods() {
        try {
            Class<?> clazz = GuiScreen.class;

            methodKeyTyped = findMethod(clazz, "GuiScreen", "keyTyped", char.class, int.class);
            methodMouseClicked = findMethod(clazz, "GuiScreen", "mouseClicked", int.class, int.class, int.class);
            methodMouseMovedOrUp = findMethod(clazz, "GuiScreen", "mouseMovedOrUp", int.class, int.class, int.class);
            methodMouseClickMove = findMethod(clazz, "GuiScreen", "mouseClickMove", int.class, int.class, int.class, long.class);
            methodActionPerformed = findMethod(clazz, "GuiScreen", "actionPerformed", GuiButton.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Find a method inside of a class
     *
     * @param clazz
     * @param className
     * @param methodName
     * @param parameterTypes
     * @return
     */
    private Method findMethod(Class<?> clazz, String className, String methodName, Class<?>... parameterTypes) {
        Method method = null;

        // try srg name first
        try {
            method = clazz.getDeclaredMethod(AbstractMultiClassTransformer.getMethodName(className, methodName, CompilationType.SRG), parameterTypes);
        } catch (Exception e) {
            // not found; try obf
            try {
                method = clazz.getDeclaredMethod(AbstractMultiClassTransformer.getMethodName(className, methodName, CompilationType.OBFUSCATED), parameterTypes);
            } catch (Exception ex) {
                // not found; try unobfuscated last
                try {
                    method = clazz.getDeclaredMethod(AbstractMultiClassTransformer.getMethodName(className, methodName, CompilationType.UNOBFUSCATED), parameterTypes);
                } catch (Exception exc) {
                    throw new UnsupportedOperationException("Could not resolve method: " + className + "/" + methodName);
                }
            }
        }

        return method;
    }

    @Override
    public void drawScreen(int x, int y, float par3) {
        screen.drawScreen(x, y, par3);
    }

    @Override
    public void keyTyped(char chr, int par2) {
        try {
            methodKeyTyped.invoke(screen, chr, par2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(int x, int y, int type) {
        try {
            methodMouseClicked.invoke(screen, x, y, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseMovedOrUp(int x, int y, int type) {
        try {
            methodMouseMovedOrUp.invoke(screen, x, y, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseClickMove(int x, int y, int type, long par4) {
        try {
            methodMouseClickMove.invoke(screen, x, y, type, par4);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        try {
            methodActionPerformed.invoke(screen, button);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setWorldAndResolution(Minecraft minecraft, int par2, int par3) {
        screen.setWorldAndResolution(minecraft, par2, par3);
    }

    @Override
    public void initGui() {
        screen.initGui();
    }

    @Override
    public void handleInput() {
        screen.handleInput();
    }

    @Override
    public void handleMouseInput() {
        screen.handleMouseInput();
    }

    @Override
    public void handleKeyboardInput() {
        screen.handleKeyboardInput();
    }

    @Override
    public void updateScreen() {
        screen.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        screen.onGuiClosed();
    }

    @Override
    public void drawDefaultBackground() {
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
