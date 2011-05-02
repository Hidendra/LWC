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
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.AccessRight;
import com.griefcraft.model.Action;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.scripting.Module.Result;
import com.griefcraft.scripting.ModuleLoader.Event;
import com.griefcraft.util.Colors;
import com.griefcraft.util.ConfigValues;
import com.griefcraft.util.StringUtils;

public class LWCBlockListener extends BlockListener {

	/**
	 * The plugin instance
	 */
	private LWCPlugin plugin;

	public LWCBlockListener(LWCPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		LWC lwc = plugin.getLWC();
		Block block = event.getBlock();
		
		if(block == null) {
			return;
		}

		Protection protection = lwc.findProtection(block);
		
		if(protection == null) {
			return;
		}
		
		Result result = lwc.getModuleLoader().dispatchEvent(Event.REDSTONE, protection, block, event.getOldCurrent());
		
		if(result == Result.CANCEL) {
			event.setNewCurrent(event.getOldCurrent());
		}
		
		/*boolean hasFlag = protection.hasFlag(Protection.Flag.REDSTONE);
		boolean denyRedstone = ConfigValues.DENY_REDSTONE.getBool();
		
		if(!hasFlag && denyRedstone) {
			event.setNewCurrent(event.getOldCurrent());
		} else if(hasFlag && !denyRedstone) {
			event.setNewCurrent(event.getOldCurrent());
		}*/
	}
	
	@Override
	public void onSignChange(SignChangeEvent event) {
		LWC lwc = plugin.getLWC();
		Block block = event.getBlock();
		Player player = event.getPlayer();
		
		if(block == null) {
			return;
		}
		
		Protection protection = lwc.findProtection(block);
		
		if(protection == null) {
			return;
		}
		
		boolean canAccess = lwc.canAccessProtection(player, protection);
		
		if(!canAccess) {
			event.setCancelled(true);
		}
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}

		if (ConfigValues.ALLOW_BLOCK_DESTRUCTION.getBool()) {
			return;
		}

		LWC lwc = plugin.getLWC();
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Protection protection = lwc.findProtection(block);
		
		if(protection == null) {
			return;
		}
		
		boolean canAccess = lwc.canAccessProtection(player, protection);
		boolean canAdmin = lwc.canAdminProtection(player, protection);

		Result result = lwc.getModuleLoader().dispatchEvent(Event.DESTROY_PROTECTION, player, protection, block, canAccess, canAdmin);

		if(result == Result.CANCEL) {
			event.setCancelled(true);
		}
		
		/*if (canAdmin) {
			protection.remove();
			player.sendMessage(Colors.Red + LWC.materialToString(protection.getBlockId()) + " unregistered.");
		}

		if (!hasAccess) {
			event.setCancelled(true);
		}*/
	}

	/**
	 * Redirect certain events (to more seperate the two distinct functions they are used for)
	 */
	@Override
	public void onBlockDamage(BlockDamageEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		blockTouched(event);
	}

	/**
	 * Used for auto registering placed protections
	 */
	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) {
			return;
		}

		LWC lwc = plugin.getLWC();
		Player player = event.getPlayer();
		Block block = event.getBlockPlaced();

		// check for an adjacent chest
		if (block.getType() == Material.CHEST) {
			if (lwc.findAdjacentDoubleChest(block) != null) {
				event.setCancelled(true);
				return;
			}
		}

		String autoRegisterType = ConfigValues.AUTO_REGISTER.getString();

		/*
		 * Check if it's enabled
		 */
		if (!autoRegisterType.equalsIgnoreCase("private") && !autoRegisterType.equalsIgnoreCase("public")) {
			return;
		}

		// default to public
		int type = ProtectionTypes.PUBLIC;

		if (autoRegisterType.equalsIgnoreCase("private")) {
			type = ProtectionTypes.PRIVATE;
		}

		/*
		 * If the block isn't protectable, don't let them
		 */
		if (!lwc.isProtectable(block)) {
			return;
		}

		/*
		 * If it's a chest, make sure they aren't trying to place it beside an already registered chest
		 */
		if (block.getType() == Material.CHEST) {
			BlockFace[] faces = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };

			for (BlockFace blockFace : faces) {
				Block face = block.getFace(blockFace);

				/*
				 * They're placing it beside a chest, check if it's protected
				 */
				if (face.getType() == Material.CHEST) {
					/*
					 * If it's protected, just return -- don't auto protect it
					 */
					if (lwc.getPhysicalDatabase().loadProtection(face.getWorld().getName(), face.getX(), face.getY(), face.getZ()) != null) {
						return;
					}
				}
			}
		}

		/*
		 * Enforce anything preventing us from creating a protection
		 */
		if (!lwc.isAdmin(player)) {
			if (lwc.enforceProtectionLimits(player) || lwc.enforceWorldGuard(player, block)) {
				return;
			}
		}

		/*
		 * All's good, protect the object!
		 */
		lwc.getPhysicalDatabase().registerProtection(block.getTypeId(), type, block.getWorld().getName(), player.getName(), "", block.getX(), block.getY(), block.getZ());

		/*
		 * Tell them
		 */
		lwc.sendLocale(player, "protection.onplace.create.finalize", "type", lwc.getLocale(autoRegisterType.toLowerCase()), "block", LWC.materialToString(block));
	}

	/**
	 * Called when a block is touched (left clicked). Used to complete registrations, etc etc!
	 * 
	 * @param event
	 */
	private void blockTouched(BlockDamageEvent event) {
		LWC lwc = plugin.getLWC();
		Player player = event.getPlayer();
		Block block = event.getBlock();

		Protection protection = lwc.findProtection(block);
		boolean hasNoOwner = protection == null;

		List<String> actions = lwc.getMemoryDatabase().getActions(player.getName());

		// boolean hasFreeRequest = actions.contains("free");
		// boolean requestInfo = actions.contains("info");
		// boolean createProtection = actions.contains("create");
		boolean modifyChest = actions.contains("modify");
		boolean dropTransferReg = actions.contains("dropTransferSelect");
		boolean showAccessList = actions.contains("owners");
		boolean forceOwner = actions.contains("forceowner");
		boolean changeFlag = actions.contains("flag");
		
		Result result;
		boolean canAccess = lwc.canAccessProtection(player, protection);
		boolean canAdmin = lwc.canAdminProtection(player, protection);
		
		if(protection != null) {
			result = lwc.getModuleLoader().dispatchEvent(Event.INTERACT_PROTECTION, player, protection, actions, canAccess, canAdmin);
		} else {
			result = lwc.getModuleLoader().dispatchEvent(Event.INTERACT_BLOCK, player, block, actions);
		}
		
		if(result == Result.CANCEL) {
			player.sendMessage("(MODULE)");
			return;
		}

		if (protection != null) {
			/*if (requestInfo) {
				String players = "";

				List<String> sessionUsers;

				if (protection.getType() == ProtectionTypes.PASSWORD) {
					sessionUsers = lwc.getMemoryDatabase().getSessionUsers(protection.getId());

					
					 * Players authed to the chest are still in the game-- let's colour them their prefix!:D
					 
					for (String plr : sessionUsers) {
						Player player_ = plugin.getServer().getPlayer(plr);

						if (player_ == null) {
							continue;
						}

						players += player_.getDisplayName() + ", ";
					}

					if (sessionUsers.size() > 0) {
						players = players.substring(0, players.length() - 4);
					}
				}

				boolean canAdmin = lwc.canAdminProtection(player, protection);
				boolean canAccess = lwc.canAccessProtection(player, protection);

				lwc.sendLocale(player, "protection.interact.info.finalize", "type", lwc.getLocale(protection.typeToString().toLowerCase()), "owner", protection.getOwner(), "access", lwc.getLocale((canAccess ? "yes" : "no")));
				if (protection.getType() == ProtectionTypes.PASSWORD && canAdmin) {
					lwc.sendLocale(player, "protection.interact.info.authedplayers", "players", players);
				}

				if (lwc.isAdmin(player)) {
					lwc.sendLocale(player, "protection.interact.info.raw", "raw", protection.toString());
				}

				if (lwc.notInPersistentMode(player.getName())) {
					lwc.getMemoryDatabase().unregisterAllActions(player.getName());
				}
				return;
			}*/

			/*else*/ if (dropTransferReg) {
				if (!canAccess) {
					lwc.sendLocale(player, "protection.interact.dropxfer.noaccess");
				} else {
					if (protection.getBlockId() != Material.CHEST.getId()) {
						lwc.sendLocale(player, "protection.interact.dropxfer.notchest");
						lwc.getMemoryDatabase().unregisterAllActions(player.getName());
						return;
					}

					lwc.getMemoryDatabase().registerMode(player.getName(), "dropTransfer", protection.getId() + "");
					lwc.getMemoryDatabase().registerMode(player.getName(), "+dropTransfer");
					lwc.sendLocale(player, "protection.interact.dropxfer.finalize");
				}
				
				lwc.getMemoryDatabase().unregisterAllActions(player.getName()); // ignore persist
				return;
			}

			/*else if (hasFreeRequest) {
				if (lwc.isAdmin(player) || protection.getOwner().equals(player.getName())) {
					lwc.sendLocale(player, "protection.interact.remove.finalize", "block", LWC.materialToString(protection.getBlockId()));
					protection.remove();
					if (lwc.notInPersistentMode(player.getName())) {
						lwc.getMemoryDatabase().unregisterAllActions(player.getName());
					}

					return;
				} else {
					lwc.sendLocale(player, "protection.interact.error.notowner", "block", LWC.materialToString(protection.getBlockId()));
					if (lwc.notInPersistentMode(player.getName())) {
						lwc.getMemoryDatabase().unregisterAllActions(player.getName());
					}

					return;
				}
			}*/

			else if (modifyChest) {
				if (lwc.canAdminProtection(player, protection)) {
					Action action = lwc.getMemoryDatabase().getAction("modify", player.getName());

					final String defaultEntities = action.getData();
					String[] entities = new String[0];

					if (defaultEntities.length() > 0) {
						entities = defaultEntities.split(" ");
					}

					if (lwc.notInPersistentMode(player.getName())) {
						lwc.getMemoryDatabase().unregisterAllActions(player.getName());
					}

					for (String rightsName : entities) {
						boolean remove = false;
						boolean isAdmin = false;
						int chestType = AccessRight.PLAYER;

						if (rightsName.startsWith("-")) {
							remove = true;
							rightsName = rightsName.substring(1);
						}

						if (rightsName.startsWith("@")) {
							isAdmin = true;
							rightsName = rightsName.substring(1);
						}

						if (rightsName.toLowerCase().startsWith("g:")) {
							chestType = AccessRight.GROUP;
							rightsName = rightsName.substring(2);
						}

						if (rightsName.toLowerCase().startsWith("l:")) {
							chestType = AccessRight.LIST;
							rightsName = rightsName.substring(2);
						}

						if (rightsName.toLowerCase().startsWith("list:")) {
							chestType = AccessRight.LIST;
							rightsName = rightsName.substring(5);
						}

						int chestID = protection.getId();
						String localeChild = AccessRight.typeToString(chestType).toLowerCase();

						if (!remove) {
							lwc.getPhysicalDatabase().unregisterProtectionRights(chestID, rightsName);
							lwc.getPhysicalDatabase().registerProtectionRights(chestID, rightsName, isAdmin ? 1 : 0, chestType);
							lwc.sendLocale(player, "protection.interact.rights.register." + localeChild, "name", rightsName, "isadmin", isAdmin ? "[" + Colors.Red + "ADMIN" + Colors.Gold + "]" : "");
						} else {
							lwc.getPhysicalDatabase().unregisterProtectionRights(chestID, rightsName);
							lwc.sendLocale(player, "protection.interact.rights.remove." + localeChild, "name", rightsName, "isadmin", isAdmin ? "[" + Colors.Red + "ADMIN" + Colors.Gold + "]" : "");
						}
						
						protection.update();
					}

					return;
				} else {
					lwc.sendLocale(player, "protection.interact.error.notowner", "block", LWC.materialToString(protection.getBlockId()));
					if (lwc.notInPersistentMode(player.getName())) {
						lwc.getMemoryDatabase().unregisterAllActions(player.getName());
					}

					return;
				}
			}

			else if (showAccessList) {
				Action action = lwc.getMemoryDatabase().getAction("owners", player.getName());
				int accessPage = Integer.parseInt(action.getData());

				/*
				 * Calculate range
				 */
				int start = (accessPage - 1) * AccessRight.RESULTS_PER_PAGE;
				int max = start + AccessRight.RESULTS_PER_PAGE;

				List<AccessRight> accessRights = lwc.getPhysicalDatabase().loadAccessRights(protection.getId(), start, max);
				int numRights = lwc.getPhysicalDatabase().countRightsForProtection(protection.getId());

				/*
				 * May have only been 2 rows left, or something. Get the real max
				 */
				int realMax = start + accessRights.size();

				player.sendMessage("");
				player.sendMessage(Colors.Green + "   LWC Protection");
				player.sendMessage(Colors.Blue + "Showing results " + Colors.LightBlue + start + Colors.Blue + "-" + Colors.LightBlue + realMax + Colors.Blue + ". Total: " + Colors.LightBlue + numRights);
				player.sendMessage("");
				player.sendMessage("");

				for (AccessRight accessRight : accessRights) {
					StringBuilder builder = new StringBuilder();
					builder.append(Colors.LightBlue);
					builder.append(accessRight.getName());
					builder.append(Colors.Blue);
					builder.append(" (");
					builder.append(AccessRight.typeToString(accessRight.getType()));
					builder.append(") ");

					if (accessRight.getRights() == 1) {
						builder.append(Colors.LightBlue);
						builder.append("(");
						builder.append(Colors.Red);
						builder.append("ADMIN");
						builder.append(Colors.LightBlue);
						builder.append(")");
					}

					player.sendMessage(builder.toString());
				}

				return;
			}

			else if (forceOwner) {
				Action action = lwc.getMemoryDatabase().getAction("forceowner", player.getName());
				String newOwner = action.getData();

				protection.setOwner(newOwner);
				protection.saveNow();
				
				lwc.sendLocale(player, "protection.interact.forceowner.finalize", "player", newOwner);
				
				if (lwc.notInPersistentMode(player.getName())) {
					lwc.getMemoryDatabase().unregisterAllActions(player.getName());
				}

				return;
			}
			
			else if (changeFlag) {
				Action action = lwc.getMemoryDatabase().getAction("flag", player.getName());
				String data = action.getData();
				
				if(!lwc.canAdminProtection(player, protection)) {
					lwc.sendLocale(player, "protection.accessdenied");
					return;
				}
				
				boolean shouldAdd = data.substring(0, 1).equals("+");
				String flagName = data.substring(1);
				
				Protection.Flag flag = null;
				
				for(Protection.Flag tmp : Protection.Flag.values()) {
					if(tmp.toString().equalsIgnoreCase(flagName)) {
						flag = tmp;
						break;
					}
				}
				
				if(flag == null) {
					lwc.sendLocale(player, "protection.internalerror", "id", "flg");
					return;
				}
				
				if(shouldAdd) {
					protection.addFlag(flag);
					lwc.sendLocale(player, "protection.interact.flag.add", "flag", StringUtils.capitalizeFirstLetter(flagName));
				} else {
					protection.removeFlag(flag);
					lwc.sendLocale(player, "protection.interact.flag.remove", "flag", StringUtils.capitalizeFirstLetter(flagName));
				}
				
				protection.saveNow();

				if (lwc.notInPersistentMode(player.getName())) {
					lwc.getMemoryDatabase().unregisterAllActions(player.getName());
				}
				
				return;
			}
		}

		if (dropTransferReg) {
			lwc.sendLocale(player, "protection.interact.dropxfer.notprotected");
			lwc.getMemoryDatabase().unregisterAllActions(player.getName());
			return;
		}

		if (/*requestInfo || hasFreeRequest ||*/ showAccessList) {
			lwc.sendLocale(player, "protection.interact.error.notregistered", "block", LWC.materialToString(block));
			if (lwc.notInPersistentMode(player.getName())) {
				lwc.getMemoryDatabase().unregisterAllActions(player.getName());
			}
			return;
		}

		if ((/*createProtection ||*/ modifyChest) && !hasNoOwner) {
			if (!lwc.canAdminProtection(player, protection)) {
				lwc.sendLocale(player, "protection.interact.error.notowner", "block", LWC.materialToString(protection.getBlockId()));
			} else {
				lwc.sendLocale(player, "protection.interact.error.alreadyregistered", "block", LWC.materialToString(protection.getBlockId()));
			}

			if (lwc.notInPersistentMode(player.getName())) {
				lwc.getMemoryDatabase().unregisterAllActions(player.getName());
			}

			return;
		}

		/*if (hasNoOwner && createProtection) {
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

			
			 * Make sure it's protectable Even though it would work fine (ie using block UNDER the sign instead), I would rather all protections being tagged on the real block itself instead of fucking myself in the future.
			 
			if (!lwc.isProtectable(block)) {
				return;
			}

			
			 * Enforce anything preventing us from creating a protection
			 
			if (!lwc.isAdmin(player)) {
				if (lwc.enforceProtectionLimits(player) || lwc.enforceWorldGuard(player, block)) {
					if (lwc.notInPersistentMode(player.getName())) {
						lwc.getMemoryDatabase().unregisterAllActions(player.getName());
					}

					return;
				}
			}

			if (type.equals("public")) {
				lwc.getPhysicalDatabase().registerProtection(block.getTypeId(), ProtectionTypes.PUBLIC, block.getWorld().getName(), player.getName(), "", block.getX(), block.getY(), block.getZ());
				lwc.sendLocale(player, "protection.interact.create.finalize"); 
			} else if (type.equals("password")) {
				String password = data.substring("password ".length());
				password = lwc.encrypt(password);

				lwc.getPhysicalDatabase().registerProtection(block.getTypeId(), ProtectionTypes.PASSWORD, block.getWorld().getName(), player.getName(), password, block.getX(), block.getY(), block.getZ());
				lwc.getMemoryDatabase().registerPlayer(player.getName(), lwc.getPhysicalDatabase().loadProtection(block.getWorld().getName(), block.getX(), block.getY(), block.getZ()).getId());
				lwc.sendLocale(player, "protection.interact.create.finalize");
				lwc.sendLocale(player, "protection.interact.create.password");

			} else if (type.equals("private")) {
				String defaultEntities = action.getData();
				String[] entities = new String[0];

				if (defaultEntities.length() > "private ".length()) {
					defaultEntities = defaultEntities.substring("private ".length());
					entities = defaultEntities.split(" ");
				}

				lwc.getPhysicalDatabase().registerProtection(block.getTypeId(), ProtectionTypes.PRIVATE, block.getWorld().getName(), player.getName(), "", block.getX(), block.getY(), block.getZ());
				lwc.sendLocale(player, "protection.interact.create.finalize");

				for (String rightsName : entities) {
					boolean isAdmin = false;
					int chestType = AccessRight.PLAYER;

					if (rightsName.startsWith("@")) {
						isAdmin = true;
						rightsName = rightsName.substring(1);
					}

					if (rightsName.toLowerCase().startsWith("g:")) {
						chestType = AccessRight.GROUP;
						rightsName = rightsName.substring(2);
					}

					if (rightsName.toLowerCase().startsWith("l:")) {
						chestType = AccessRight.LIST;
						rightsName = rightsName.substring(2);
					}

					if (rightsName.toLowerCase().startsWith("list:")) {
						chestType = AccessRight.LIST;
						rightsName = rightsName.substring(5);
					}

					String localeChild = AccessRight.typeToString(chestType).toLowerCase();
					
					// load the protection that was created
					Protection temp = lwc.getPhysicalDatabase().loadProtection(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
					lwc.getPhysicalDatabase().registerProtectionRights(temp.getId(), rightsName, isAdmin ? 1 : 0, chestType);
					lwc.sendLocale(player, "protection.interact.rights.register." + localeChild, "name", rightsName, "isadmin", isAdmin ? "[" + Colors.Red + "ADMIN" + Colors.Gold + "]" : "");
					
					temp.removeCache();
				}
			} else if (type.equals("trap")) {
				String trapType = chop[1].toLowerCase();
				String reason = "";
				int protectionType = ProtectionTypes.TRAP_KICK; // default to
																// kick

				if (chop.length > 3) {
					reason = StringUtils.join(chop, 2);
				}

				if (trapType.equals("ban")) {
					protectionType = ProtectionTypes.TRAP_BAN;
				}

				
				 * This.. is the definition of hackish. TODO: Let's rename "password" to "data", T_T
				 
				lwc.getPhysicalDatabase().registerProtection(block.getTypeId(), protectionType, block.getWorld().getName(), player.getName(), reason, block.getX(), block.getY(), block.getZ());
				lwc.sendLocale(player, "protection.interact.create.finalize");
			}

			if (lwc.notInPersistentMode(player.getName())) {
				lwc.getMemoryDatabase().unregisterAllActions(player.getName());
			}
		}*/
	}

}
