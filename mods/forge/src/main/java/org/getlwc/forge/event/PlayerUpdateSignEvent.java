package org.getlwc.forge.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet130UpdateSign;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerUpdateSignEvent extends PlayerEvent {

    public final Packet130UpdateSign packet;

    public PlayerUpdateSignEvent(EntityPlayerMP player, Packet130UpdateSign packet) {
        super(player);
        this.packet = packet;
    }

}
