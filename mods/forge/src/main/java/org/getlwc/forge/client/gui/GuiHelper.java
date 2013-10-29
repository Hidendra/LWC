package org.getlwc.forge.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import org.getlwc.forge.asm.AbstractSingleClassTransformer;
import org.getlwc.forge.asm.CompilationType;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;

public class GuiHelper {

    /**
     * The field fontRenderer exists at (GuiScreen)
     */
    private static Field fontRendererField = null;

    /**
     * The field xSize exists at (GuiContainer)
     */
    private static Field xSizeField = null;

    public static void drawGuiContainerForegroundLayer(GuiContainer container) {
        //
        FontRenderer fontRenderer = null;
        int xSize = -1;

        try {
            if (fontRendererField == null) {
                fontRendererField = GuiScreen.class.getDeclaredField(AbstractSingleClassTransformer.getFieldName("GuiScreen", "fontRenderer", CompilationType.SRG));
                fontRendererField.setAccessible(true);
            }

            if (xSizeField == null) {
                xSizeField = GuiContainer.class.getDeclaredField(AbstractSingleClassTransformer.getFieldName("GuiContainer", "xSize", CompilationType.SRG));
                xSizeField.setAccessible(true);
            }

            fontRenderer = (FontRenderer) fontRendererField.get(container);
            xSize = (Integer) xSizeField.get(container);
        } catch (Exception e) {
            //
            e.printStackTrace();
        }

        if (fontRenderer == null || xSize == -1) {
            return;
        }

        fontRenderer.drawString("xSize=" + xSize, 20, 20, 0 /* color? */);

        int baseX = xSize;
        int baseY = 8;
        drawGradientRect(xSize, 8, xSize + 96, 8 + 150, 0xc0000000, 0xc0000000);
        //
    }

    /**
     * Called when the mouse is clicked on a container
     *
     * @param x
     * @param y
     * @param flag
     */
    public static void mouseClicked(int x, int y, int flag) {
        //
        System.out.println(String.format("mouseClicked(%d, %d, %d)", x, y, flag));
    }

    /**
     * Draw a gradient rectangle
     *
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @param startColour
     * @param endColour
     */
    private static void drawGradientRect(int startX, int startY, int endX, int endY, int startColour, int endColour) {
        float f = (float) (startColour >> 24 & 255) / 255.0F;
        float f1 = (float) (startColour >> 16 & 255) / 255.0F;
        float f2 = (float) (startColour >> 8 & 255) / 255.0F;
        float f3 = (float) (startColour & 255) / 255.0F;
        float f4 = (float) (endColour >> 24 & 255) / 255.0F;
        float f5 = (float) (endColour >> 16 & 255) / 255.0F;
        float f6 = (float) (endColour >> 8 & 255) / 255.0F;
        float f7 = (float) (endColour & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(f1, f2, f3, f);
        int zLevel = 1;
        tessellator.addVertex((double) endX, (double) startY, (double) zLevel);
        tessellator.addVertex((double) startX, (double) startY, (double) zLevel);
        tessellator.setColorRGBA_F(f5, f6, f7, f4);
        tessellator.addVertex((double) startX, (double) endY, (double) zLevel);
        tessellator.addVertex((double) endX, (double) endY, (double) zLevel);
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

}
