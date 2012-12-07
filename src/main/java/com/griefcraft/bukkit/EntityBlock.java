/*
 * Copyright (c) 2011, 2012, Tyler Blair
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
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
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

package com.griefcraft.bukkit;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.List;

public class EntityBlock implements Block {

    /**
     * The entity block id
     */
    public static final int ENTITY_BLOCK_ID = 5000;

    /**
     * The position offset to use in the database
     */
    public static final int POSITION_OFFSET = 50000;

    /**
     * The entity this protection is for
     */
    private Entity entity;

    public EntityBlock(Entity entity) {
        this.entity = entity;
    }

    /**
     * The name of these entity block. Used to represent the "material" for configuration, mainly.
     * @return
     */
    public String getName() {
        if (entity instanceof Painting) {
            return "painting";
        } else if (entity instanceof ItemFrame) {
            return "item_frame";
        } else if (entity instanceof StorageMinecart) {
            return "storage_minecart";
        }

        return "entity";
    }

    public int getX() {
        return POSITION_OFFSET + entity.getUniqueId().hashCode();
    }

    public int getY() {
        return POSITION_OFFSET + entity.getUniqueId().hashCode();
    }

    public int getZ() {
        return POSITION_OFFSET + entity.getUniqueId().hashCode();
    }

    public int getTypeId() {
        return 5000;
    }

    public World getWorld() {
        return entity.getWorld();
    }

    public Entity getEntity() {
        return entity;
    }

    public Location getLocation() {
        return entity.getLocation();
    }

    public Chunk getChunk() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setData(byte b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setData(byte b, boolean b2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setType(Material material) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean setTypeId(int i) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean setTypeId(int i, boolean b) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean setTypeIdAndData(int i, byte b, boolean b2) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public BlockFace getFace(Block block) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public BlockState getState() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Biome getBiome() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setBiome(Biome biome) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isBlockPowered() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isBlockIndirectlyPowered() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isBlockFacePowered(BlockFace blockFace) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isBlockFaceIndirectlyPowered(BlockFace blockFace) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getBlockPower(BlockFace blockFace) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getBlockPower() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isEmpty() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isLiquid() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getTemperature() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getHumidity() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PistonMoveReaction getPistonMoveReaction() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean breakNaturally() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean breakNaturally(ItemStack itemStack) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection<ItemStack> getDrops() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection<ItemStack> getDrops(ItemStack itemStack) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public byte getData() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Block getRelative(int i, int i2, int i3) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Block getRelative(BlockFace blockFace) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Block getRelative(BlockFace blockFace, int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Material getType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public byte getLightLevel() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public byte getLightFromSky() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public byte getLightFromBlocks() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setMetadata(String s, MetadataValue metadataValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<MetadataValue> getMetadata(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean hasMetadata(String s) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeMetadata(String s, Plugin plugin) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
