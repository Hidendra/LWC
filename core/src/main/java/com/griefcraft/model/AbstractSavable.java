package com.griefcraft.model;

import com.griefcraft.lwc.LWC;

public abstract class AbstractSavable {

    /**
     * If the savable has been removed or not
     */
    protected boolean removed = false;

    /**
     * If the savable has been modified or not
     */
    protected boolean modified = false;

    /**
     * Queue the protection to be saved
     */
    public void save() {
        if (removed || !modified) {
            return;
        }

        LWC.getInstance().getDatabaseThread().add(this);
    }

    /**
     * Force a protection update to the live database
     */
    public abstract void saveNow();

}
