package org.getlwc.forge.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerUpdateSignEvent extends PlayerEvent {

    public final C12PacketUpdateSign packet;

    public PlayerUpdateSignEvent(EntityPlayerMP player, C12PacketUpdateSign packet) {
        super(player);
        this.packet = packet;
    }

}
