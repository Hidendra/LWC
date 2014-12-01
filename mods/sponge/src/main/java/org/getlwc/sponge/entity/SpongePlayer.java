package org.getlwc.sponge.entity;

import org.getlwc.ItemStack;
import org.getlwc.Location;
import org.getlwc.entity.SimplePlayer;
import org.getlwc.sponge.world.SpongeWorld;
import org.getlwc.util.Color;

import java.util.UUID;

public class SpongePlayer extends SimplePlayer {

    /**
     * native Sponge handle
     */
    private org.spongepowered.api.entity.player.Player handle;

    public SpongePlayer(org.spongepowered.api.entity.player.Player handle) {
        this.handle = handle;
    }

    @Override
    public UUID getUUID() {
        return handle.getUniqueId();
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public Location getLocation() {
        // todo remove unnecessary object creation
        return new Location(new SpongeWorld(handle.getWorld()), handle.getX(), handle.getY(), handle.getZ());
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
