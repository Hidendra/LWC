/**
 * This file is part of LWC (https://github.com/Hidendra/LWC)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.griefcraft.bukkit;

import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

public class MockPlayer implements Player {

	private String playerName;
	private String displayName;
	private boolean op;
	private World world;

	/**
	 * The message history for the player
	 */
	private Deque<String> messageHistory = new LinkedList<String>();
	
	public MockPlayer(String playerName) {
		this.playerName = playerName;
		this.displayName = playerName;
	}
	
	/**
	 * @return the last message received
	 */
	public String pollLastMessage() {
		return messageHistory.pollLast();
	}

	public void sendMessage(String arg0) {
		messageHistory.addLast(arg0);
	}

	public boolean isOnline() {
		return true;
	}

	public String getName() {
		return playerName;
	}
	
	public void setOp(boolean op) {
		this.op = op;
	}

	public boolean isOp() {
		return op;
	}

	public Server getServer() {
		return Bukkit.getServer();
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String arg0) {
		this.displayName = arg0;
	}

	public World getWorld() {
		return world;
	}
	
	public void setWorld(World world) {
		this.world = world;
	}

	public Location getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public InetSocketAddress getAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	public void sendRawMessage(String arg0) {
		// TODO Auto-generated method stub

	}

	public boolean isDead() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean performCommand(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void kickPlayer(String arg0) {
		// TODO Auto-generated method stub

	}

	public PlayerInventory getInventory() {
		// TODO Auto-generated method stub
		return null;
	}

	public ItemStack getItemInHand() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSleepTicks() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isSleeping() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setItemInHand(ItemStack arg0) {
		// TODO Auto-generated method stub

	}

	public void damage(int arg0) {
		// TODO Auto-generated method stub

	}

	public void damage(int arg0, Entity arg1) {
		// TODO Auto-generated method stub

	}

	public double getEyeHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getEyeHeight(boolean arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Location getEyeLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getHealth() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getLastDamage() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<Block> getLastTwoTargetBlocks(HashSet<Byte> arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Block> getLineOfSight(HashSet<Byte> arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getMaximumAir() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaximumNoDamageTicks() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNoDamageTicks() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getRemainingAir() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Block getTargetBlock(HashSet<Byte> arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vehicle getVehicle() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isInsideVehicle() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean leaveVehicle() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setHealth(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setLastDamage(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setMaximumAir(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setMaximumNoDamageTicks(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setNoDamageTicks(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setRemainingAir(int arg0) {
		// TODO Auto-generated method stub

	}

	public Arrow shootArrow() {
		// TODO Auto-generated method stub
		return null;
	}

	public Egg throwEgg() {
		// TODO Auto-generated method stub
		return null;
	}

	public Snowball throwSnowball() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean eject() {
		// TODO Auto-generated method stub
		return false;
	}

	public int getEntityId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getFallDistance() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getFireTicks() {
		// TODO Auto-generated method stub
		return 0;
	}

	public EntityDamageEvent getLastDamageCause() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getMaxFireTicks() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<Entity> getNearbyEntities(double arg0, double arg1, double arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public Entity getPassenger() {
		// TODO Auto-generated method stub
		return null;
	}

	public UUID getUniqueId() {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector getVelocity() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public void remove() {
		// TODO Auto-generated method stub

	}

	public void setFallDistance(float arg0) {
		// TODO Auto-generated method stub

	}

	public void setFireTicks(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setLastDamageCause(EntityDamageEvent arg0) {
		// TODO Auto-generated method stub

	}

	public boolean setPassenger(Entity arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setVelocity(Vector arg0) {
		// TODO Auto-generated method stub

	}

	public boolean teleport(Location arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean teleport(Entity arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void teleportTo(Location arg0) {
		// TODO Auto-generated method stub

	}

	public void teleportTo(Entity arg0) {
		// TODO Auto-generated method stub

	}

	public void awardAchievement(Achievement arg0) {
		// TODO Auto-generated method stub

	}

	public void chat(String arg0) {
		// TODO Auto-generated method stub

	}

	public Location getCompassTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	public void incrementStatistic(Statistic arg0) {
		// TODO Auto-generated method stub

	}

	public void incrementStatistic(Statistic arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	public void incrementStatistic(Statistic arg0, Material arg1) {
		// TODO Auto-generated method stub

	}

	public void incrementStatistic(Statistic arg0, Material arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	public boolean isSleepingIgnored() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSneaking() {
		// TODO Auto-generated method stub
		return false;
	}

	public void loadData() {
		// TODO Auto-generated method stub

	}

	public void playEffect(Location arg0, Effect arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	public void playNote(Location arg0, byte arg1, byte arg2) {
		// TODO Auto-generated method stub

	}

	public void saveData() {
		// TODO Auto-generated method stub

	}

	public void sendBlockChange(Location arg0, Material arg1, byte arg2) {
		// TODO Auto-generated method stub

	}

	public void sendBlockChange(Location arg0, int arg1, byte arg2) {
		// TODO Auto-generated method stub

	}

	public boolean sendChunkChange(Location arg0, int arg1, int arg2, int arg3, byte[] arg4) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setCompassTarget(Location arg0) {
		// TODO Auto-generated method stub

	}

	public void setSleepingIgnored(boolean arg0) {
		// TODO Auto-generated method stub

	}

	public void setSneaking(boolean arg0) {
		// TODO Auto-generated method stub

	}

	public void updateInventory() {
		// TODO Auto-generated method stub

	}

}
