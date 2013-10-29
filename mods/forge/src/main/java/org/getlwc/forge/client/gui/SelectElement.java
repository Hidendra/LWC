package org.getlwc.forge.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SelectElement {

    /**
     * The text on the element
     */
    private String text;

    /**
     * Any subtext below the element
     */
    private String subtext;

    public SelectElement(String text, String subtext) {
        this.text = text;
        this.subtext = subtext;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash *= 31 + text.hashCode();
        hash *= 31 + subtext.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SelectElement)) {
            return false;
        }

        SelectElement e = (SelectElement) o;
        return e.text.equals(e) && e.subtext.equals(subtext);
    }

    public SelectElement(String text) {
        this(text, "");
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSubtext() {
        return subtext;
    }

    public void setSubtext(String subtext) {
        this.subtext = subtext;
    }

}
