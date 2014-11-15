package org.getlwc.sponge.entity;

import org.getlwc.ItemStack;
import org.getlwc.Location;
import org.getlwc.entity.SimplePlayer;
import org.getlwc.util.Color;

import java.util.UUID;

public class SpongePlayer extends SimplePlayer {

    /**
     * native Sponge handle
     */
    private org.spongepowered.api.entity.Player handle;

    public SpongePlayer(org.spongepowered.api.entity.Player handle) {
        this.handle = handle;
    }

    @Override
    public UUID getUUID() {
        // TODO
        throw new UnsupportedOperationException("getUUID() is not yet supported");
    }

    @Override
    public String getName() {
        // TODO
        throw new UnsupportedOperationException("getName() is not yet supported");
    }

    @Override
    public Location getLocation() {
        // TODO Entity does not even expose the world it's in yet
        return new Location(null, handle.getX(), handle.getY(), handle.getZ());
    }

    @Override
    public ItemStack getItemInHand() {
        // TODO
        throw new UnsupportedOperationException("getItemInHand() is not yet supported");
    }

    @Override
    public void sendMessage(String message) {
        for (String line : message.split("\n")) {
            handle.sendMessage(Color.replaceColors(line));
        }
    }

}
