package org.getlwc.forge.client.gui;

public interface Widget {

    /**
     * Draw the widget at the given location
     *
     * @param x
     * @param y
     */
    public void draw(int x, int y);

    /**
     * Get the width of the widget
     *
     * @return widget width
     */
    public int getWidth();

    /**
     * Get the height of the widget
     *
     * @return widget height
     */
    public int getHeight();

}
