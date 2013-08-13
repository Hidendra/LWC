package org.getlwc.forge.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet130UpdateSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerUpdateSignEvent extends PlayerEvent {

    public final Packet130UpdateSign packet;

    public final TileEntitySign sign;

    public PlayerUpdateSignEvent(EntityPlayerMP player, Packet130UpdateSign packet, TileEntitySign sign) {
        super(player);
        this.packet = packet;
        this.sign = sign;
    }

}
