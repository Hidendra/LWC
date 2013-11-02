package org.getlwc.forge.client.gui;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ContainerButton implements Widget {

    private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation("lwc", "textures/gui/button.png");

    /**
     * The texture on top of the background
     */
    private ResourceLocation texture;

    /**
     * The width of the button
     */
    private int width;

    /**
     * The height of the button
     */
    private int height;

    /**
     * The colour of the button
     */
    private int colour;

    public ContainerButton(ResourceLocation texture, int width, int height, int colour) {
        this.texture = texture;
        this.width = width;
        this.height = height;
        this.colour = colour;
    }

    /**
     * Draw the entire button including both the background and texture
     *
     * @param x
     * @param y
     */
    public void draw(int x, int y) {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glPushMatrix();

        drawBackground(x, y);
        drawTexture(x, y);

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    /**
     * Draw the background of the container
     *
     * @param x
     * @param y
     */
    public void drawBackground(int x, int y) {
        Minecraft minecraft = FMLClientHandler.instance().getClient();

        float red = (colour >> 16 & 255) / 255.0f;
        float green = (colour >> 8 & 255) / 255.0f;
        float blue = (colour & 255) / 255.0f;

        GL11.glColor4f(red, green, blue, 1.0f);
        minecraft.renderEngine.bindTexture(BUTTON_TEXTURE);
        drawTexturedQuadFit(x, y, width, height, 1);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0F);
    }

    /**
     * Draw the button's texture
     *
     * @param x
     * @param y
     */
    public void drawTexture(int x, int y) {
        Minecraft minecraft = FMLClientHandler.instance().getClient();

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0F);
        minecraft.renderEngine.bindTexture(texture);
        drawTexturedQuadFit(x + 2, y + 2, 26, 26, 2);
    }

    /**
     * Draws an image to fit inside the given quad no matter what size it is
     * Credits to: http://www.minecraftforge.net/forum/index.php/topic,11229.msg57594.html#msg57594
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param zLevel
     */
    private void drawTexturedQuadFit(double x, double y, double width, double height, double zLevel){
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x + 0, y + height, zLevel, 0,1);
        tessellator.addVertexWithUV(x + width, y + height, zLevel, 1, 1);
        tessellator.addVertexWithUV(x + width, y + 0, zLevel, 1, 0);
        tessellator.addVertexWithUV(x + 0, y + 0, zLevel, 0, 0);
        tessellator.draw();
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getColour() {
        return colour;
    }

    public void setColour(int colour) {
        this.colour = colour;
    }

}
