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

package com.griefcraft.listeners;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockDamageLevel;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockInteractEvent;
import org.bukkit.event.block.BlockListener;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Action;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.model.RightTypes;
import com.griefcraft.util.Colors;

public class LWCBlockListener extends BlockListener {

	/**
	 * The plugin instance
	 */
	private LWCPlugin plugin;
	
	public LWCBlockListener(LWCPlugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Prevent player from interacting with a protected block
	 */
	@Override
	public void onBlockInteract(BlockInteractEvent event) {
		if(!(event.getEntity() instanceof Player)) {
			return;
		}
		
		Player player = (Player) event.getEntity();
		LWC lwc = plugin.getLWC();
		
		Block block = event.getBlock();

		if (!lwc.isProtectable(block)) {
			return;
		}

		List<Block> entitySet = lwc.getEntitySet(block.getWorld(), block.getX(), block.getY(), block.getZ());
		boolean hasAccess = true;

		for (final Block _block : entitySet) {
			if (_block == null) {
				continue;
			}

			final Protection protection = lwc.getPhysicalDatabase().loadProtectedEntity(_block.getX(), _block.getY(), _block.getZ());

			if (protection == null) {
				continue;
			}

			hasAccess = lwc.canAccessChest(player, protection);
			
			switch (protection.getType()) {
			case ProtectionTypes.PASSWORD:
				if (!hasAccess) {
					lwc.getMemoryDatabase().unregisterUnlock(player.getName());
					lwc.getMemoryDatabase().registerUnlock(player.getName(), protection.getID());
					
					player.sendMessage(Colors.Red + "This chest is locked.");
					player.sendMessage(Colors.Red + "Type " + Colors.Gold + "/lwc -u <password>" + Colors.Red + " to unlock it");
				}

				break;

			case ProtectionTypes.PRIVATE:
				if (!hasAccess) {
					player.sendMessage(Colors.Red + "This chest is locked with a magical spell.");
				}

				break;
			}
		}

		if(!hasAccess) {
			event.setCancelled(true);
		}
	}
	
	/**
	 * Redirect certain events (to more seperate the two distinct functions this is used for)
	 */
	@Override
	public void onBlockDamage(BlockDamageEvent event) {
		BlockDamageLevel level = event.getDamageLevel();
		Block block = event.getBlock();
		
		if(!plugin.getLWC().isProtectable(block)) {
			return;
		}
		
		if(level == BlockDamageLevel.BROKEN) {
			blockBroken(event);
		}
		
		else if(level == BlockDamageLevel.STARTED) {
			blockTouched(event);
		}

	}
	
	/**
	 * Called when a block is touched (left clicked). 
	 * Used to complete registrations, etc etc!
	 * 
	 * @param event
	 */
	private void blockTouched(BlockDamageEvent event) {
		LWC lwc = plugin.getLWC();
		Player player = event.getPlayer();
		Block block = event.getBlock();
		
		List<Block> entitySet = lwc.getEntitySet(block.getWorld(), block.getX(), block.getY(), block.getZ());
		boolean hasAccess = true;
		Protection entity = null;
		boolean hasNoOwner = true;

		for (final Block chest : entitySet) {
			if (chest == null) {
				continue;
			}

			entity = lwc.getPhysicalDatabase().loadProtectedEntity(chest.getX(), chest.getY(), chest.getZ());

			if (entity == null) {
				continue;
			}

			hasAccess = lwc.canAccessChest(player, entity);
			hasNoOwner = false;

			break;
		}

		List<String> actions = lwc.getMemoryDatabase().getActions(player.getName());

		final boolean hasFreeRequest = actions.contains("free");
		final boolean requestInfo = actions.contains("info");
		final boolean createChest = actions.contains("create");
		final boolean modifyChest = actions.contains("modify");
		final boolean dropTransferReg = actions.contains("dropTransferSelect");

		if (entity != null) {
			hasNoOwner = false;

			if (requestInfo) {
				String players = "";

				List<String> sessionUsers;

				if (entity.getType() == ProtectionTypes.PASSWORD) {
					sessionUsers = lwc.getMemoryDatabase().getSessionUsers(entity.getID());

					/*
					 * Players authed to the chest are still in the game-- let's colour them their prefix!:D
					 */
					for (final String plr : sessionUsers) {
						final Player player_ = plugin.getServer().getPlayer(plr);

						if (player_ == null) {
							continue;
						}

						// players += player_.getColor() + plr + Colors.White + ", ";
						players += player_.getDisplayName() + ", ";
					}

					if (sessionUsers.size() > 0) {
						players = players.substring(0, players.length() - 4);
					}
				}

				String type = " ";

				switch (entity.getType()) {
				case ProtectionTypes.PUBLIC:
					type = "Public";
					break;
				case ProtectionTypes.PASSWORD:
					type = "Password";
					break;
				case ProtectionTypes.PRIVATE:
					type = "Private";
					break;
				}

				boolean canAdmin = lwc.canAdminChest(player, entity);
				// boolean canAccess = parent.canAccessChest(player,
				// entity);

				if (canAdmin) {
					player.sendMessage(Colors.Green + "ID: " + Colors.Gold + entity.getID());
				}

				player.sendMessage(Colors.Green + "Type: " + Colors.Gold + type);
				player.sendMessage(Colors.Green + "Owner: " + Colors.Gold + entity.getOwner());

				if (entity.getType() == ProtectionTypes.PASSWORD && canAdmin) {
					player.sendMessage(Colors.Green + "Authed players: " + players);
				}

				if (canAdmin) {
					player.sendMessage(Colors.Green + "Location: " + Colors.Gold + "{" + entity.getX() + ", " + entity.getY() + ", " + entity.getZ() + "}");
					player.sendMessage(Colors.Green + "Date created: " + Colors.Gold + entity.getDate());
				}

				if (lwc.notInPersistentMode(player.getName())) {
					lwc.getMemoryDatabase().unregisterAllActions(player.getName());
				}
				return;
			} else if (dropTransferReg) {
				final boolean canAccess = lwc.canAccessChest(player, entity);
				
				if (!canAccess) {
					player.sendMessage(Colors.Red + "You cannot use a chest that you cannot access as a drop transfer target.");
					player.sendMessage(Colors.Red + "If this is a passworded chest, please unlock it before retrying.");
					player.sendMessage(Colors.Red + "Use \"/lwc droptransfer select\" to try again.");
				} else {
					for (Block __entity : entitySet) {
						if (__entity.getType() != Material.CHEST) {
							player.sendMessage(Colors.Red + "You need to select a chest as the Drop Transfer target!");
							lwc.getMemoryDatabase().unregisterAllActions(player.getName());
							return;
						}
					}

					lwc.getMemoryDatabase().registerMode(player.getName(), "dropTransfer", "f" + entity.getID());
					player.sendMessage(Colors.Green + "Successfully registered chest as drop transfer target.");
				}
				lwc.getMemoryDatabase().unregisterAllActions(player.getName()); // ignore
				// persist
				return;
			} else if (hasFreeRequest) {
				if (lwc.isAdmin(player) || entity.getOwner().equals(player.getName())) {
					player.sendMessage(Colors.LightGreen + "Removed lock on the chest successfully!");
					lwc.getPhysicalDatabase().unregisterProtectedEntity(entity.getX(), entity.getY(), entity.getZ());
					lwc.getPhysicalDatabase().unregisterProtectionRights(entity.getID());
					if (lwc.notInPersistentMode(player.getName())) {
						lwc.getMemoryDatabase().unregisterAllActions(player.getName());
					}
					return;
				} else {
					player.sendMessage(Colors.Red + "You do not own that chest!");
					if (lwc.notInPersistentMode(player.getName())) {
						lwc.getMemoryDatabase().unregisterAllActions(player.getName());
					}
					
					event.setCancelled(true);
					return;
				}
			} else if (modifyChest) {
				if (lwc.canAdminChest(player, entity)) {
					final Action action = lwc.getMemoryDatabase().getAction("modify", player.getName());

					final String defaultEntities = action.getData();
					String[] entities = new String[0];

					if (defaultEntities.length() > 0) {
						entities = defaultEntities.split(" ");
					}

					if (lwc.notInPersistentMode(player.getName())) {
						lwc.getMemoryDatabase().unregisterAllActions(player.getName());
					}

					for (String userEntity : entities) {
						boolean remove = false;
						boolean isAdmin = false;
						int chestType = RightTypes.PLAYER;

						if (userEntity.startsWith("-")) {
							remove = true;
							userEntity = userEntity.substring(1);
						}

						if (userEntity.startsWith("@")) {
							isAdmin = true;
							userEntity = userEntity.substring(1);
						}

						if (userEntity.toLowerCase().startsWith("g:")) {
							chestType = RightTypes.GROUP;
							userEntity = userEntity.substring(2);
						}

						final int chestID = lwc.getPhysicalDatabase().loadProtectedEntity(block.getX(), block.getY(), block.getZ()).getID();

						if (!remove) {
							lwc.getPhysicalDatabase().unregisterProtectionRights(chestID, userEntity);
							lwc.getPhysicalDatabase().registerProtectionRights(chestID, userEntity, isAdmin ? 1 : 0, chestType);
							player.sendMessage(Colors.LightGreen + "Registered rights for " + Colors.Gold + userEntity + Colors.Green + " " + (isAdmin ? "[" + Colors.Red + "ADMIN" + Colors.Gold + "]" : "") + " [" + (chestType == RightTypes.PLAYER ? "Player" : "Group") + "]");
						} else {
							lwc.getPhysicalDatabase().unregisterProtectionRights(chestID, userEntity);
							player.sendMessage(Colors.LightGreen + "Removed rights for " + Colors.Gold + userEntity + Colors.Green + " [" + (chestType == RightTypes.PLAYER ? "Player" : "Group") + "]");
						}
					}

					return;
				} else {
					player.sendMessage(Colors.Red + "You do not own that chest!");
					if (lwc.notInPersistentMode(player.getName())) {
						lwc.getMemoryDatabase().unregisterAllActions(player.getName());
					}
					
					event.setCancelled(true);
					return;
				}
			}
		}

		if (dropTransferReg) {
			player.sendMessage(Colors.Red + "Cannot select unregistered chest as drop transfer target.");
			player.sendMessage(Colors.Red + "Use \"/lwc droptransfer select\" to try again.");
			lwc.getMemoryDatabase().unregisterAllActions(player.getName()); // ignore
			// persist

			return;
		}

		if (requestInfo || hasFreeRequest) {
			player.sendMessage(Colors.Red + "Chest is unregistered");
			if (lwc.notInPersistentMode(player.getName())) {
				lwc.getMemoryDatabase().unregisterAllActions(player.getName());
			}
			return;
		}

		if ((createChest || modifyChest) && !hasNoOwner) {
			if (!lwc.canAdminChest(player, entity)) {
				player.sendMessage(Colors.Red + "You do not own that chest!");
			} else {
				player.sendMessage(Colors.Red + "You have already registered that chest!");
			}
			
			if (lwc.notInPersistentMode(player.getName())) {
				lwc.getMemoryDatabase().unregisterAllActions(player.getName());
			}
			
			event.setCancelled(true);
			return;
		}

		if (hasNoOwner && createChest) {
			final Action action = lwc.getMemoryDatabase().getAction("create", player.getName());

			final String data = action.getData();
			final String[] chop = data.split(" ");
			final String type = chop[0].toLowerCase();
			String subset = "";

			if (chop.length > 1) {
				for (int i = 1; i < chop.length; i++) {
					subset += chop[i] + " ";
				}
			}

			if (lwc.enforceChestLimits(player)) {
				if (lwc.notInPersistentMode(player.getName())) {
					lwc.getMemoryDatabase().unregisterAllActions(player.getName());
				}
				return;
			}

			if (type.equals("public")) {
				lwc.getPhysicalDatabase().registerProtectedEntity(ProtectionTypes.PUBLIC, player.getName(), "", block.getX(), block.getY(), block.getZ());
				player.sendMessage(Colors.Green + "Created public protection successfully");
			} else if (type.equals("password")) {
				String password = action.getData().substring("password ".length());
				password = lwc.encrypt(password);

				lwc.getPhysicalDatabase().registerProtectedEntity(ProtectionTypes.PASSWORD, player.getName(), password, block.getX(), block.getY(), block.getZ());
				lwc.getMemoryDatabase().registerPlayer(player.getName(), lwc.getPhysicalDatabase().loadProtectedEntity(block.getX(), block.getY(), block.getZ()).getID());
				player.sendMessage(Colors.Green + "Created password protection successfully");
				player.sendMessage(Colors.LightGreen + "For convenience, you don't have to enter your password until");
				player.sendMessage(Colors.LightGreen + "you next log in");

			} else if (type.equals("private")) {
				String defaultEntities = action.getData();
				String[] entities = new String[0];

				if (defaultEntities.length() > "private ".length()) {
					defaultEntities = defaultEntities.substring("private ".length());
					entities = defaultEntities.split(" ");
				}

				lwc.getPhysicalDatabase().registerProtectedEntity(ProtectionTypes.PRIVATE, player.getName(), "", block.getX(), block.getY(), block.getZ());

				player.sendMessage(Colors.Green + "Created private protection successfully");

				for (String userEntity : entities) {
					boolean isAdmin = false;
					int chestType = RightTypes.PLAYER;

					if (userEntity.startsWith("@")) {
						isAdmin = true;
						userEntity = userEntity.substring(1);
					}

					if (userEntity.toLowerCase().startsWith("g:")) {
						chestType = RightTypes.GROUP;
						userEntity = userEntity.substring(2);
					}

					lwc.getPhysicalDatabase().registerProtectionRights(lwc.getPhysicalDatabase().loadProtectedEntity(block.getX(), block.getY(), block.getZ()).getID(), userEntity, isAdmin ? 1 : 0, chestType);
					player.sendMessage(Colors.LightGreen + "Registered rights for " + Colors.Gold + userEntity + ": " + (isAdmin ? "[" + Colors.Red + "ADMIN" + Colors.Gold + "]" : "") + " [" + (chestType == RightTypes.PLAYER ? "Player" : "Group") + "]");
				}
			}

			if (lwc.notInPersistentMode(player.getName())) {
				lwc.getMemoryDatabase().unregisterAllActions(player.getName());
			}
		}

		if(!hasAccess) {
			event.setCancelled(true);
		}
	}
	
	/**
	 * Prevent a player from destroying a protected block
	 * We can safely -assume- that onBlockDamage already checked if the block
	 * being called can be protected
	 * 
	 * @param event
	 */
	private void blockBroken(BlockDamageEvent event) {
		LWC lwc = plugin.getLWC();
		Player player = event.getPlayer();
		Block block = event.getBlock();
		
		List<Block> entitySet = lwc.getEntitySet(block.getWorld(), block.getX(), block.getY(), block.getZ());
		boolean hasAccess = true; 
		boolean canAdmin = true;
		Protection protection = null;

		for (Block _block : entitySet) {
			if (_block == null) {
				continue;
			}

			protection = lwc.getPhysicalDatabase().loadProtectedEntity(_block.getX(), _block.getY(), _block.getZ());

			if (protection == null) {
				continue;
			}

			hasAccess = lwc.canAccessChest(player, protection);
			canAdmin = lwc.canAdminChest(player, protection);
		}

		if (hasAccess && protection != null) {
			if (canAdmin) {
				lwc.getPhysicalDatabase().unregisterProtectedEntity(protection.getX(), protection.getY(), protection.getZ());
				lwc.getPhysicalDatabase().unregisterProtectionRights(protection.getID());
				player.sendMessage(Colors.Red + "Chest unregistered.");
			}
		}

		if(!canAdmin) {
			event.setCancelled(true);
		}
	}
	
}
