package org.getlwc.forge.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S33PacketUpdateSign;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerUpdateSignEvent extends PlayerEvent {

    public final S33PacketUpdateSign packet;

    public PlayerUpdateSignEvent(EntityPlayerMP player, S33PacketUpdateSign packet) {
        super(player);
        this.packet = packet;
    }

}
