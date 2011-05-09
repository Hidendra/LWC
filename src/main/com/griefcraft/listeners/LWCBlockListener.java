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

		if (plugin.getLWC().getConfiguration().getBoolean("protections.blockDestruction", false)) {
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

		String autoRegisterType = plugin.getLWC().resolveProtectionConfiguration(block, "autoRegister");
		
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
			if (lwc.enforceProtectionLimits(player)) {
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
		// boolean dropTransferReg = actions.contains("dropTransferSelect");
		boolean showAccessList = actions.contains("owners");
		// boolean forceOwner = actions.contains("forceowner");
		// boolean changeFlag = actions.contains("flag");
		
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
			if (modifyChest) {
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
		}

		if (showAccessList) {
			lwc.sendLocale(player, "protection.interact.error.notregistered", "block", LWC.materialToString(block));
			if (lwc.notInPersistentMode(player.getName())) {
				lwc.getMemoryDatabase().unregisterAllActions(player.getName());
			}
			return;
		}

		if ((modifyChest) && !hasNoOwner) {
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
	}

}
