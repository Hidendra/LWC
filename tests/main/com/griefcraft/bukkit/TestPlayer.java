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

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.ServerConfigurationManager;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.griefcraft.tests.TestSuite;

public class TestPlayer extends CraftPlayer implements Player {

	/**
	 * The test suite the created this player
	 */
	private TestSuite testSuite;
	
    public TestPlayer(CraftServer server, EntityPlayer entity) {
		super(server, entity);
	}
    
    public void setTestSuite(TestSuite testSuite) {
    	this.testSuite = testSuite;
    }
    
    public TestSuite getTestSuite() {
    	return testSuite;
    }

	public boolean isOp() {
        return server.getHandle().h(getName());
    }

    public boolean isPlayer() {
        return true;
    }

    public boolean isOnline() {
        for (Object obj: server.getHandle().b) {
            EntityPlayer player = (EntityPlayer) obj;
            if (player.name.equalsIgnoreCase(getName())) {
                return true;
            }
        }
        return false;
    }

    public double getEyeHeight() {
        return getEyeHeight(false);
    }

    public double getEyeHeight(boolean ignoreSneaking) {
        if (ignoreSneaking) {
            return 1.62D;
        } else {
            if (isSneaking()) {
                return 1.42D;
            } else {
                return 1.62D;
            }
        }
    }

    public void sendRawMessage(String message) {
        // FIXME
    }

    public void sendMessage(String message) {
        // FIXME
    }

    public void kickPlayer(String message) {
        // FIXME
    }

    public void chat(String msg) {
        // FIXME
    	System.out.println(getName() + ": " + msg);
    }

    public String getDisplayName() {
        return getHandle().name;
    }

    public void setDisplayName(final String name) {
        getHandle().name = name;
    }

    @Override
    public String toString() {
        return "TestPlayer{" + "name=" + getName() + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CraftHumanEntity other = (CraftHumanEntity) obj;
        if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
        return hash;
    }

    public boolean performCommand(String command) {
        return server.dispatchCommand(this, command);
    }

    @Override
    public boolean teleport(Location location) {
        WorldServer oldWorld = ((CraftWorld)getWorld()).getHandle();
        WorldServer newWorld = ((CraftWorld)location.getWorld()).getHandle();
        ServerConfigurationManager manager = server.getHandle();
        EntityPlayer entity = getHandle();
        boolean teleportSuccess;

        if (oldWorld != newWorld) {

            EntityPlayer newEntity = new EntityPlayer(manager.c, newWorld, entity.name, new ItemInWorldManager(newWorld));

            newEntity.id = entity.id;
            newEntity.a = entity.a;
            newEntity.health = entity.health;
            newEntity.fireTicks = entity.fireTicks;
            newEntity.inventory = entity.inventory;
            newEntity.inventory.d = newEntity;
            newEntity.activeContainer = entity.activeContainer;
            newEntity.defaultContainer = entity.defaultContainer;
            newEntity.locX = location.getX();
            newEntity.locY = location.getY();
            newEntity.locZ = location.getZ();
            newEntity.displayName = entity.displayName;
            newEntity.compassTarget = entity.compassTarget;
            newWorld.u.c((int) location.getBlockX() >> 4, (int) location.getBlockZ() >> 4);

            teleportSuccess = newEntity.a.teleport(location);

            if (teleportSuccess) {
                manager.c.k.a(entity);
                manager.c.k.b(entity);
                oldWorld.manager.b(entity);
                manager.b.remove(entity);
                oldWorld.e(entity);

                newWorld.manager.a(newEntity);
                newWorld.a(newEntity);
                manager.b.add(newEntity);

                entity.a.e = newEntity;
                this.entity = newEntity;

                setCompassTarget(getCompassTarget());
            }

            return teleportSuccess;
        } else {
            return entity.a.teleport(location);
        }
    }

    public void setSneaking(boolean sneak) {
        getHandle().e(sneak);
    }

    public boolean isSneaking() {
        return getHandle().Z();
    }

    public void loadData() {
        server.getHandle().n.b(getHandle());
    }

    public void saveData() {
        server.getHandle().n.a(getHandle());
    }

    public void updateInventory() {
        getHandle().m();
    }

	@Override
	public InetSocketAddress getAddress() {
		return null;
	}

	@Override
	public Location getCompassTarget() {
		return getWorld().getSpawnLocation();
	}

	@Override
	public void setCompassTarget(Location arg0) {
		
	}

}
