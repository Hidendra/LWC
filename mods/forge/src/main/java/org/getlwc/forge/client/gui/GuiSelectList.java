package org.getlwc.forge.client.gui;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiSelectList extends GuiSlot {

    /**
     * The elements to be shown on the list
     */
    private List<SelectElement> elements = new ArrayList<SelectElement>();

    /**
     * The selected element. Only one may be selected at a time
     */
    private SelectElement selectedElement = null;

    public GuiSelectList(int width, int height, int top, int bottom, int slotHeight) {
        super(FMLClientHandler.instance().getClient(), width, height, top, bottom, slotHeight);
    }

    /**
     * Size of the current list
     *
     * @return
     */
    @Override
    protected int getSize() {
        return elements.size();
    }

    /**
     * Called when an element in the list is clicked
     *
     * @param slot
     * @param doubleClicked
     */
    @Override
    protected void elementClicked(int slot, boolean doubleClicked) {
        if (slot < 0 || slot >= elements.size()) {
            return;
        }

        selectedElement = elements.get(slot);
    }

    /**
     * Check if a slot is selected or not
     *
     * @param slot
     * @return
     */
    @Override
    protected boolean isSelected(int slot) {
        if (slot < 0 || slot >= elements.size()) {
            return false;
        }

        return selectedElement != null && elements.get(slot) == selectedElement;
    }

    /**
     * Draw the background
     */
    @Override
    protected void drawBackground() {
        //
    }

    /**
     * Draw a slot
     *
     * @param slot
     * @param x
     * @param y
     * @param height
     * @param tessellator
     */
    @Override
    protected void drawSlot(int slot, int x, int y, int height, Tessellator tessellator) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
