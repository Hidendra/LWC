package org.getlwc.granite.entity;

import org.getlwc.ItemStack;
import org.getlwc.Location;
import org.getlwc.entity.SimplePlayer;
import org.getlwc.granite.util.GraniteUtils;
import org.getlwc.granite.world.GraniteWorld;
import org.getlwc.util.Color;

import java.util.UUID;

public class GranitePlayer extends SimplePlayer {

    private org.granitemc.granite.api.entity.player.Player handle;

    public GranitePlayer(org.granitemc.granite.api.entity.player.Player handle) {
        this.handle = handle;
    }

    @Override
    public ItemStack getItemInHand() {
        return GraniteUtils.castItemStack(handle.getHeldItem());
    }

    @Override
    public void sendMessage(String message) {
        for (String line : message.split("\n")) {
            handle.sendMessage(Color.replaceColors(line));
        }
    }

    @Override
    public UUID getUUID() {
        // TODO getUUID() returns null
        return handle.getUniqueID();
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public Location getLocation() {
        org.granitemc.granite.api.utils.Location locationHandle = handle.getLocation();

        // TODO avoid recreating GraniteWorld everytime
        return new Location(new GraniteWorld(locationHandle.getWorld()), locationHandle.getX(), locationHandle.getY(), locationHandle.getZ());
    }

    /**
     * Returns the handle for this player
     *
     * @return
     */
    public org.granitemc.granite.api.entity.player.Player getHandle() {
        return handle;
    }

}
