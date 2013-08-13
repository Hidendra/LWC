package org.getlwc.forge.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerPlaceBlockEvent extends PlayerEvent {

    public final net.minecraft.world.World world;

    public final int blockX;

    public final int blockY;

    public final int blockZ;

    public final net.minecraft.item.ItemStack stack;

    public final EntityPlayer player;

    public PlayerPlaceBlockEvent(net.minecraft.world.World world, int x, int y, int z, net.minecraft.item.ItemStack stack, EntityPlayer entityPlayer) {
        super(entityPlayer);
        this.world = world;
        this.blockX = x;
        this.blockY = y;
        this.blockZ = z;
        this.stack = stack;
        this.player = entityPlayer;
    }

}
