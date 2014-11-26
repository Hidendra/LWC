package org.getlwc.forge.entity;

import org.getlwc.Location;
import org.getlwc.entity.Entity;
import org.getlwc.forge.world.ForgeWorld;

import java.util.UUID;

public class ForgeEntity extends Entity {

    /**
     * The entity handle
     */
    private net.minecraft.entity.Entity handle;

    public ForgeEntity(net.minecraft.entity.Entity handle) {
        this.handle = handle;
    }

    @Override
    public UUID getUUID() {
        return handle.getPersistentID();
    }

    @Override
    public String getName() {
        return handle.getCommandSenderName();
    }

    @Override
    public Location getLocation() {
        try {
            return new Location(new ForgeWorld(handle.worldObj), (int) handle.posX, (int) handle.posY, (int) handle.posZ);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
