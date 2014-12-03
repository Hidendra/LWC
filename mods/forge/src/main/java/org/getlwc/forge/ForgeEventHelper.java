/*
 * Copyright (c) 2011-2013 Tyler Blair
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR,
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package org.getlwc.forge;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraftforge.common.MinecraftForge;
import org.getlwc.Block;
import org.getlwc.BlockFace;
import org.getlwc.EventHelper;
import org.getlwc.Location;
import org.getlwc.World;
import org.getlwc.entity.Player;
import org.getlwc.forge.entity.ForgeEntity;
import org.getlwc.forge.event.EntityExplodeEvent;
import org.getlwc.forge.event.PlayerBreakBlockEvent;
import org.getlwc.forge.event.PlayerPlaceBlockEvent;
import org.getlwc.forge.event.PlayerUpdateSignEvent;
import org.getlwc.lang.Locale;

import java.util.ArrayList;
import java.util.List;

public class ForgeEventHelper {

    /**
     * Called when a block is broken (harvested)
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @param entityPlayer
     */
    public static boolean onBlockHarvested(net.minecraft.world.World world, int x, int y, int z, EntityPlayer entityPlayer) {
        Event event = new PlayerBreakBlockEvent(world, x, y, z, entityPlayer);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled() || event.getResult() == Event.Result.DENY;
    }

    /**
     * Called when an explosion occurs in the world
     *
     * @param nativeWorld
     * @param explosionX
     * @param explosionY
     * @param explosionZ
     * @param explosionRadius
     * @param affectedLocations
     * @param entity
     * @return
     */
    public static boolean onExplosion(net.minecraft.world.World nativeWorld, double explosionX, double explosionY, double explosionZ, int explosionRadius, List<net.minecraft.world.ChunkPosition> affectedLocations, net.minecraft.entity.Entity entity) {
        List<Block> affectedBlocks = new ArrayList<Block>();
        World world = ForgeMod.instance.getWorld(nativeWorld.getWorldInfo().getWorldName());

        for (net.minecraft.world.ChunkPosition loc : affectedLocations) {
            Block block = world.getBlockAt(loc.chunkPosX, loc.chunkPosY, loc.chunkPosZ);

            // Check that it isn't air (we don't need to know about air internally)
            if (block.isOneOf("minecraft:air")) {
                continue;
            }

            affectedBlocks.add(block);
        }

        Event event = new EntityExplodeEvent(entity, (int) explosionX, (int) explosionY, (int) explosionZ, explosionRadius, affectedBlocks);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled() || event.getResult() == Event.Result.DENY;
    }

    /**
     * Called when a block is placed in the world
     *
     * @param itemStack
     * @param player
     * @param x
     * @param y
     * @param z
     * @param side
     * @param hitX
     * @param hitY
     * @param hitZ
     * @return
     */
    public static boolean onBlockPlace(net.minecraft.item.ItemStack itemStack, net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World nativeWorld, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        Event event = new PlayerPlaceBlockEvent(nativeWorld, x, y, z, itemStack, player);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled() || event.getResult() == Event.Result.DENY;
    }

    /**
     * Called when a sign is updated in the world
     *
     * @param player
     * @param packet
     * @return
     */
    public static boolean onUpdateSign(net.minecraft.entity.player.EntityPlayerMP player, C12PacketUpdateSign packet) {
        Event event = new PlayerUpdateSignEvent(player, packet);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled() || event.getResult() == Event.Result.DENY;
    }

    /**
     * Called when updateClientInfo(Packet204ClientInfo) is called in EntityPlayerMP
     *
     * @param handle
     * @param packet
     */
    public static void onUpdateClientInfo(EntityPlayerMP handle, C15PacketClientSettings packet) {
        ForgeMod mod = ForgeMod.instance;
        Player player = mod.wrapPlayer(handle);
        player.setLocale(new Locale(packet.func_149524_c())); // func_149524_c: getLanguage
        mod.getEngine().getConsoleSender().sendMessage("Player " + player.getName() + " loaded using locale: " + player.getLocale());
        EventHelper.onPlayerJoin(mod.wrapPlayer(handle));
    }

    /**
     * Called when a redstone event is triggered. Typically this is not for EVERY redstone events only a small subset
     * that LWC cares about
     *
     * @param handle
     * @param x
     * @param y
     * @param z
     * @param flag
     * @return
     */
    public static boolean onRedstoneChange(net.minecraft.world.World handle, int x, int y, int z, boolean flag) {
        ForgeMod.instance.getEngine().getConsoleSender().sendMessage(String.format("onRedstoneChange(%s, %d, %d, %d, %s)", handle.getWorldInfo().getWorldName(), x, y, z, Boolean.toString(flag)));


        return true;
    }

    /**
     * Called when a piston is extended
     *
     * @param handle
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static boolean onUpdatePistonState(net.minecraft.world.World handle, int x, int y, int z) {
        int data = handle.getBlockMetadata(x, y, z);
        int notchFace = BlockPistonBase.getPistonOrientation(data);

        if (data != 7) {
            boolean powered = isPistonIndirectlyPowered(handle, x, y, z, notchFace);

            BlockFace face = BlockFace.fromNotch(notchFace);
            World world = ForgeMod.instance.getWorld(handle.getWorldInfo().getWorldName());
            Block piston = world.getBlockAt(x, y, z);
            Location reaching = piston.getRelative(face).getLocation();

            if (powered && !BlockPistonBase.isExtended(data)) {
                return onPistonExtend(piston, reaching);
            } else if (!powered && BlockPistonBase.isExtended(data)) {
                return onPistonRetract(piston, reaching);
            }
        }

        return false;
    }

    /**
     * Called when an item is moved between inventories - i.e. hopper and dropper.
     * This is only called for the hopper that initiated the move
     *
     * @param handle
     * @param isPullingItems if the hopper is pulling items
     * @return
     */
    public static boolean onInventoryMoveItem(net.minecraft.tileentity.IHopper handle, boolean isPullingItems) {
        // todo get the block hopper is connected to using the face
        // isPullingItems = true
        //     hopper is pulling items from inventory above it
        // else
        //     hopper is attached to a block
        // i.e. TileEntityHopper.getOutputInventory() (private)
        World world = ForgeMod.instance.getWorld(handle.getWorldObj().getWorldInfo().getWorldName());
        Block hopper = world.getBlockAt((int) handle.getXPos(), (int) handle.getYPos(), (int) handle.getZPos());
        Location usingInventory;

        if (isPullingItems) {
            usingInventory = hopper.getRelative(BlockFace.UP).getLocation();
        } else {
            // int direction = BlockHopper.getDirectionFromMetadata(hopper.getData());
            // usingInventory = hopper.getRelative(Facing.offsetsXForSide[direction], Facing.offsetsYForSide[direction], Facing.offsetsZForSide[direction]).getLocation();
            usingInventory = hopper.getRelative(BlockFace.fromNotch(BlockHopper.getDirectionFromMetadata(hopper.getData()))).getLocation();
        }

        IInventory inventory = TileEntityHopper.func_145893_b(handle.getWorldObj(), usingInventory.getBlockX(), usingInventory.getBlockY(), usingInventory.getBlockZ()); // func_145893_b: getInventoryAtLocation

        boolean canProceed;

        if (isPullingItems) {
            // if they are pushing items then we shouldn't do anything if the source inven is empty
            canProceed = inventory != null && !isInventoryEmpty(inventory);
        } else {
            canProceed = inventory != null && !isInventoryEmpty(handle);
        }

        if (canProceed) {
            return EventHelper.onInventoryMoveItem(usingInventory);
        } else {
            return false;
        }
    }

    /**
     * Called when an entity tries to break a door
     *
     * @param entity
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static boolean onEntityBreakDoor(EntityLiving entity, int x, int y, int z) {
        World world = ForgeMod.instance.getWorld(entity.worldObj.getWorldInfo().getWorldName());
        return EventHelper.onBlockBreak(new ForgeEntity(entity), world.getBlockAt(x, y, z));
    }

    /**
     * Checks if an inventory is empty
     *
     * @param inventory
     * @return
     */
    private static boolean isInventoryEmpty(IInventory inventory) {
        if (inventory == null) {
            return true;
        }

        for (int index = 0; index < inventory.getSizeInventory(); index ++) {
            if (inventory.getStackInSlot(index) != null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Called when a piston is extended
     *
     * @param piston
     * @param extending
     * @return
     */
    private static boolean onPistonExtend(Block piston, Location extending) {
        return EventHelper.onPistonExtend(piston, extending);
    }

    /**
     * Called when a piston is retracted
     *
     * @param piston
     * @param retracting
     * @return
     */
    private static boolean onPistonRetract(Block piston, Location retracting) {
        return EventHelper.onPistonRetract(piston, retracting);
    }

    /**
     * Checks the block to that side to see if it is indirectly powered.
     *
     * @param handle
     * @param x
     * @param y
     * @param z
     * @param face
     * @return
     */
    private static boolean isPistonIndirectlyPowered(net.minecraft.world.World handle, int x, int y, int z, int face) {
        return face != 0 && handle.getIndirectPowerOutput(x, y - 1, z, 0) || (face != 1 && handle.getIndirectPowerOutput(x, y + 1, z, 1) || (face != 2 && handle.getIndirectPowerOutput(x, y, z - 1, 2) || (face != 3 && handle.getIndirectPowerOutput(x, y, z + 1, 3) || (face != 5 && handle.getIndirectPowerOutput(x + 1, y, z, 5) || (face != 4 && handle.getIndirectPowerOutput(x - 1, y, z, 4) || (handle.getIndirectPowerOutput(x, y, z, 0) || (handle.getIndirectPowerOutput(x, y + 2, z, 1) || (handle.getIndirectPowerOutput(x, y + 1, z - 1, 2) || (handle.getIndirectPowerOutput(x, y + 1, z + 1, 3) || (handle.getIndirectPowerOutput(x - 1, y + 1, z, 4) || handle.getIndirectPowerOutput(x + 1, y + 1, z, 5)))))))))));
    }

}
