package org.getlwc.forge.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import org.getlwc.forge.asm.AbstractMultiClassTransformer;
import org.getlwc.forge.asm.CompilationType;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class GuiContainerWrapper extends GuiScreenWrapper {

    /**
     * The xSize of the container
     */
    private int xSize;

    /**
     * The ySize of the container
     */
    private int ySize;

    /**
     * The left of the container if we have a container
     */
    private int leftX;

    /**
     * The top of the container if we have a container
     */
    private int topY;

    /**
     * List of all of the widgets on the container
     */
    private List<Widget> widgets;

    public GuiContainerWrapper(GuiContainer screen) {
        super(screen);
    }

    /**
     * Initialize all methods
     */
    protected void init() {
        super.init();

        try {
            Field xSizeField = GuiContainer.class.getDeclaredField(AbstractMultiClassTransformer.getFieldName("GuiContainer", "xSize", CompilationType.SRG));
            xSizeField.setAccessible(true);
            Field ySizeField = GuiContainer.class.getDeclaredField(AbstractMultiClassTransformer.getFieldName("GuiContainer", "ySize", CompilationType.SRG));
            ySizeField.setAccessible(true);

            GuiContainer container = (GuiContainer) screen;
            xSize = xSizeField.getInt(container);
            ySize = ySizeField.getInt(container);

            leftX = (width - xSize) / 2;
            topY = (height - ySize) / 2;

            widgets = new ArrayList<Widget>();
            widgets.add(new ContainerButton(new ResourceLocation("lwc", "textures/gui/lock.png"), 30, 30, 0xfc717b));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawScreen(int x, int y, float par3) {
        super.drawScreen(x, y, par3);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glPushMatrix();

        drawString(fontRenderer, String.format("xSize=%d, ySize=%d, leftX=%d, topY=%d", xSize, ySize, leftX, topY), leftX + 30, topY + 30, 0xfc717b);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        for (Widget widget : widgets) {
            int widgetX = leftX - widget.getWidth();
            int widgetY = topY + 2;

            widget.draw(widgetX, widgetY);
        }


        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

}
