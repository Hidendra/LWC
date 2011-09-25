/*
 * Copyright 2011 Tyler Blair. All rights reserved.
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

import org.bukkit.BlockChangeDelegate;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class MockWorld implements World {

    public boolean createExplosion(Location arg0, float arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean createExplosion(double arg0, double arg1, double arg2, float arg3) {
        // TODO Auto-generated method stub
        return false;
    }

    public Item dropItem(Location arg0, ItemStack arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Item dropItemNaturally(Location arg0, ItemStack arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean generateTree(Location arg0, TreeType arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean generateTree(Location arg0, TreeType arg1, BlockChangeDelegate arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    public Block getBlockAt(Location arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Block getBlockAt(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getBlockTypeIdAt(Location arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getBlockTypeIdAt(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Chunk getChunkAt(Location arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Chunk getChunkAt(Block arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Chunk getChunkAt(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Entity> getEntities() {
        // TODO Auto-generated method stub
        return null;
    }

    public Environment getEnvironment() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getFullTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    public ChunkGenerator getGenerator() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getHighestBlockYAt(Location arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getHighestBlockYAt(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getId() {
        // TODO Auto-generated method stub
        return 0;
    }

    public List<LivingEntity> getLivingEntities() {
        // TODO Auto-generated method stub
        return null;
    }

    public Chunk[] getLoadedChunks() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean getPVP() {
        // TODO Auto-generated method stub
        return false;
    }

    public List<Player> getPlayers() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<BlockPopulator> getPopulators() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getSeed() {
        // TODO Auto-generated method stub
        return 0;
    }

    public Location getSpawnLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getThunderDuration() {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getWeatherDuration() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean hasStorm() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isChunkLoaded(Chunk arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isChunkLoaded(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isThundering() {
        // TODO Auto-generated method stub
        return false;
    }

    public void loadChunk(Chunk arg0) {
        // TODO Auto-generated method stub

    }

    public void loadChunk(int arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    public boolean loadChunk(int arg0, int arg1, boolean arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    public void playEffect(Location arg0, Effect arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    public void playEffect(Location arg0, Effect arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub

    }

    public boolean refreshChunk(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean regenerateChunk(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public void save() {
        // TODO Auto-generated method stub

    }

    public void setFullTime(long arg0) {
        // TODO Auto-generated method stub

    }

    public void setPVP(boolean arg0) {
        // TODO Auto-generated method stub

    }

    public boolean setSpawnLocation(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    public void setStorm(boolean arg0) {
        // TODO Auto-generated method stub

    }

    public void setThunderDuration(int arg0) {
        // TODO Auto-generated method stub

    }

    public void setThundering(boolean arg0) {
        // TODO Auto-generated method stub

    }

    public void setTime(long arg0) {
        // TODO Auto-generated method stub

    }

    public void setWeatherDuration(int arg0) {
        // TODO Auto-generated method stub

    }

    public Arrow spawnArrow(Location arg0, Vector arg1, float arg2, float arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    public LivingEntity spawnCreature(Location arg0, CreatureType arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public LightningStrike strikeLightning(Location arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public LightningStrike strikeLightningEffect(Location arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean unloadChunk(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean unloadChunk(int arg0, int arg1, boolean arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean unloadChunk(int arg0, int arg1, boolean arg2, boolean arg3) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean unloadChunkRequest(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean unloadChunkRequest(int arg0, int arg1, boolean arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean createExplosion(Location arg0, float arg1, boolean arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean createExplosion(double arg0, double arg1, double arg2, float arg3, boolean arg4) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean getAllowAnimals() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean getAllowMonsters() {
        // TODO Auto-generated method stub
        return false;
    }

    public ChunkSnapshot getEmptyChunkSnapshot(int arg0, int arg1, boolean arg2, boolean arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setSpawnFlags(boolean arg0, boolean arg1) {
        // TODO Auto-generated method stub

    }

    public <T extends Entity> T spawn(Location arg0, Class<T> arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    public Biome getBiome(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Block getHighestBlockAt(Location arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Block getHighestBlockAt(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public double getHumidity(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getTemperature(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return 0;
    }

    public UUID getUID() {
        // TODO Auto-generated method stub
        return null;
    }

}
