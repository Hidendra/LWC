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
import org.bukkit.block.BlockFace;
import org.bukkit.block.ContainerBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockInteractEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Action;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.model.AccessRight;
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

	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		if (!LWCInfo.DEVELOPMENT) {
			return; // wip
		}

		LWC lwc = plugin.getLWC();
		Block poweredBlock = event.getBlock();

		System.out.println(poweredBlock.getType().toString() + ": " + event.getOldCurrent() + "->" + event.getNewCurrent());

		if (!lwc.isProtectable(poweredBlock)) {
			return;
		}

		List<Block> blocks = lwc.getProtectionSet(poweredBlock.getWorld(), poweredBlock.getX(), poweredBlock.getY(), poweredBlock.getZ());

		for (Block block : blocks) {
			Protection protection = lwc.getPhysicalDatabase().loadProtectedEntity(block.getX(), block.getY(), block.getZ());

			if (protection != null) {
				event.setNewCurrent(event.getOldCurrent());
				break;
			}
		}
	}

	/**
	 * Prevent player from interacting with a protected block
	 */
	@Override
	public void onBlockInteract(BlockInteractEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getEntity();
		LWC lwc = plugin.getLWC();

		Block block = event.getBlock();

		/*
		 * Prevent players with lwc.blockinventories from opening inventories
		 */
		if (lwc.getGroupHandler() != null && !lwc.getGroupHandler().getPermissionHandler().has(player, "lwc.protect") && lwc.getGroupHandler().getPermissionHandler().has(player, "lwc.blockinventory") && !lwc.isAdmin(player) && !lwc.isMod(player)) {
			if (block.getState() instanceof ContainerBlock) {
				player.sendMessage(Colors.Red + "The server admin is blocking you from opening that.");
				event.setCancelled(true);
				return;
			}
		}

		boolean access = lwc.enforceAccess(player, block);

		if (!access) {
			event.setCancelled(true);
		}
	}

	/**
	 * Redirect certain events (to more seperate the two distinct functions they are used for)
	 */
	public void onBlockDamage(BlockDamageEvent event) {
		if (event.isCancelled()) {
			return;
		}

		BlockDamageLevel level = event.getDamageLevel();

		if (level == BlockDamageLevel.STARTED) {
			blockTouched(event);
		}

	}

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}

		LWC lwc = plugin.getLWC();
		Player player = event.getPlayer();
		Block block = event.getBlock();

		List<Block> protectionSet = lwc.getProtectionSet(block.getWorld(), block.getX(), block.getY(), block.getZ());
		boolean hasAccess = true;
		boolean canAdmin = true;
		Protection protection = null;

		for (Block _block : protectionSet) {
			if (_block == null) {
				continue;
			}

			protection = lwc.getPhysicalDatabase().loadProtectedEntity(_block.getX(), _block.getY(), _block.getZ());

			if (protection == null) {
				continue;
			}

			hasAccess = lwc.canAccessProtection(player, protection);
			canAdmin = lwc.canAdminProtection(player, protection);
		}

		if (protection != null) {
			if (canAdmin) {
				lwc.getPhysicalDatabase().unregisterProtectedEntity(protection.getX(), protection.getY(), protection.getZ());
				lwc.getPhysicalDatabase().unregisterProtectionRights(protection.getId());
				player.sendMessage(Colors.Red + lwc.materialToString(protection.getBlockId()) + " unregistered.");
			} else {
				event.setCancelled(true);
			}
		}

		if (!hasAccess) {
			event.setCancelled(true);
		}
	}

	/**
	 * Used for auto registering placed protections
	 */
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) {
			return;
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
		 * Get info required for finishing the protection
		 */
		LWC lwc = plugin.getLWC();
		Player player = event.getPlayer();
		Block block = event.getBlockPlaced();

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
					if (lwc.getPhysicalDatabase().loadProtectedEntity(face.getX(), face.getY(), face.getZ()) != null) {
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
		lwc.getPhysicalDatabase().registerProtectedEntity(block.getTypeId(), type, player.getName(), "", block.getX(), block.getY(), block.getZ());

		/*
		 * Tell them
		 */
		player.sendMessage(Colors.Green + "Created " + StringUtils.capitalizeFirstLetter(autoRegisterType) + " " + lwc.materialToString(block) + " successfully");
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

		List<Block> protectionSet = lwc.getProtectionSet(block.getWorld(), block.getX(), block.getY(), block.getZ());
		Protection protection = null;
		boolean hasNoOwner = true;

		for (Block _block : protectionSet) {
			if (_block == null) {
				continue;
			}

			protection = lwc.getPhysicalDatabase().loadProtectedEntity(_block.getX(), _block.getY(), _block.getZ());

			if (protection == null) {
				continue;
			}

			hasNoOwner = false;
			break;
		}

		List<String> actions = lwc.getMemoryDatabase().getActions(player.getName());

		boolean hasFreeRequest = actions.contains("free");
		boolean requestInfo = actions.contains("info");
		boolean createProtection = actions.contains("create");
		boolean modifyChest = actions.contains("modify");
		boolean dropTransferReg = actions.contains("dropTransferSelect");
		boolean showAccessList = false;
		int accessPage = 1;

		/*
		 * Let's do some work before hand and seperate it from that big scary If statement
		 */
		for (String action : actions) {
			if (action.startsWith("owners:")) {
				showAccessList = true;
				accessPage = Integer.parseInt(action.split(":")[1]);
			}
		}

		if (protection != null) {
			hasNoOwner = false;

			if (requestInfo) {
				String players = "";

				List<String> sessionUsers;

				if (protection.getType() == ProtectionTypes.PASSWORD) {
					sessionUsers = lwc.getMemoryDatabase().getSessionUsers(protection.getId());

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

				switch (protection.getType()) {
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

				boolean canAdmin = lwc.canAdminProtection(player, protection);
				// boolean canAccess = parent.canAccessChest(player,
				// entity);

				if (canAdmin) {
					player.sendMessage(Colors.Green + "ID: " + Colors.Gold + protection.getId());
				}

				player.sendMessage(Colors.Green + "Type: " + Colors.Gold + type);
				player.sendMessage(Colors.Green + "Owner: " + Colors.Gold + protection.getOwner());

				if (protection.getType() == ProtectionTypes.PASSWORD && canAdmin) {
					player.sendMessage(Colors.Green + "Authed players: " + players);
				}

				if (canAdmin) {
					player.sendMessage(Colors.Green + "Location: " + Colors.Gold + "{" + protection.getX() + ", " + protection.getY() + ", " + protection.getZ() + "}");
					player.sendMessage(Colors.Green + "Date created: " + Colors.Gold + protection.getDate());
				}

				if (lwc.notInPersistentMode(player.getName())) {
					lwc.getMemoryDatabase().unregisterAllActions(player.getName());
				}
				return;
			}

			else if (dropTransferReg) {
				final boolean canAccess = lwc.canAccessProtection(player, protection);

				if (!canAccess) {
					player.sendMessage(Colors.Red + "You cannot use a chest that you cannot access as a drop transfer target.");
					player.sendMessage(Colors.Red + "If this is a passworded chest, please unlock it before retrying.");
					player.sendMessage(Colors.Red + "Use \"/lwc droptransfer select\" to try again.");
				} else {
					for (Block __entity : protectionSet) {
						if (__entity.getType() != Material.CHEST) {
							player.sendMessage(Colors.Red + "You need to select a chest as the Drop Transfer target!");
							lwc.getMemoryDatabase().unregisterAllActions(player.getName());
							return;
						}
					}

					lwc.getMemoryDatabase().registerMode(player.getName(), "dropTransfer", "f" + protection.getId());
					player.sendMessage(Colors.Green + "Successfully registered chest as drop transfer target.");
				}
				lwc.getMemoryDatabase().unregisterAllActions(player.getName()); // ignore
				// persist
				return;
			}

			else if (hasFreeRequest) {
				if (lwc.isAdmin(player) || protection.getOwner().equals(player.getName())) {
					player.sendMessage(Colors.Green + "Removed lock on the " + lwc.materialToString(protection.getBlockId()) + " successfully!");
					lwc.getPhysicalDatabase().unregisterProtectedEntity(protection.getX(), protection.getY(), protection.getZ());
					lwc.getPhysicalDatabase().unregisterProtectionRights(protection.getId());
					if (lwc.notInPersistentMode(player.getName())) {
						lwc.getMemoryDatabase().unregisterAllActions(player.getName());
					}

					return;
				} else {
					player.sendMessage(Colors.Red + "You do not own that " + lwc.materialToString(protection.getBlockId()) + "!");
					if (lwc.notInPersistentMode(player.getName())) {
						lwc.getMemoryDatabase().unregisterAllActions(player.getName());
					}

					return;
				}
			}

			else if (modifyChest) {
				if (lwc.canAdminProtection(player, protection)) {
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
						int chestType = AccessRight.PLAYER;

						if (userEntity.startsWith("-")) {
							remove = true;
							userEntity = userEntity.substring(1);
						}

						if (userEntity.startsWith("@")) {
							isAdmin = true;
							userEntity = userEntity.substring(1);
						}

						if (userEntity.toLowerCase().startsWith("g:")) {
							chestType = AccessRight.GROUP;
							userEntity = userEntity.substring(2);
						}

						int chestID = protection.getId();

						if (!remove) {
							lwc.getPhysicalDatabase().unregisterProtectionRights(chestID, userEntity);
							lwc.getPhysicalDatabase().registerProtectionRights(chestID, userEntity, isAdmin ? 1 : 0, chestType);
							player.sendMessage(Colors.Green + "Registered rights for " + Colors.Gold + userEntity + Colors.Green + " " + (isAdmin ? "[" + Colors.Red + "ADMIN" + Colors.Gold + "]" : "") + " [" + (chestType == AccessRight.PLAYER ? "Player" : "Group") + "]");
						} else {
							lwc.getPhysicalDatabase().unregisterProtectionRights(chestID, userEntity);
							player.sendMessage(Colors.Green + "Removed rights for " + Colors.Gold + userEntity + Colors.Green + " [" + (chestType == AccessRight.PLAYER ? "Player" : "Group") + "]");
						}
					}

					return;
				} else {
					player.sendMessage(Colors.Red + "You do not own that " + lwc.materialToString(protection.getBlockId()) + "!");
					if (lwc.notInPersistentMode(player.getName())) {
						lwc.getMemoryDatabase().unregisterAllActions(player.getName());
					}

					return;
				}
			}

			else if (showAccessList) {
				/*
				 * Calculate range
				 */
				int start = (accessPage - 1) * AccessRight.RESULTS_PER_PAGE;
				int max = start + AccessRight.RESULTS_PER_PAGE;

				List<AccessRight> accessRights = lwc.getPhysicalDatabase().getAccessRights(protection.getId(), start, max);
				int numRights = lwc.getPhysicalDatabase().countRights(protection.getId());

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
					builder.append(accessRight.getEntity());
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

		if (dropTransferReg) {
			player.sendMessage(Colors.Red + "Cannot select unregistered chest as drop transfer target.");
			player.sendMessage(Colors.Red + "Use \"/lwc droptransfer select\" to try again.");
			lwc.getMemoryDatabase().unregisterAllActions(player.getName()); // ignore
			// persist

			return;
		}

		if (requestInfo || hasFreeRequest || showAccessList) {
			player.sendMessage(Colors.Red + "That " + lwc.materialToString(block) + " is not registered!");
			if (lwc.notInPersistentMode(player.getName())) {
				lwc.getMemoryDatabase().unregisterAllActions(player.getName());
			}
			return;
		}

		if ((createProtection || modifyChest) && !hasNoOwner) {
			if (!lwc.canAdminProtection(player, protection)) {
				player.sendMessage(Colors.Red + "You do not own that " + lwc.materialToString(protection.getBlockId()) + "!");
			} else {
				player.sendMessage(Colors.Red + "You have already registered that " + lwc.materialToString(protection.getBlockId()) + "!");
			}

			if (lwc.notInPersistentMode(player.getName())) {
				lwc.getMemoryDatabase().unregisterAllActions(player.getName());
			}

			return;
		}

		if (hasNoOwner && createProtection) {
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

			/*
			 * Make sure it's protectable Even though it would work fine (ie using block UNDER the sign instead), I would rather all protections being tagged on the real block
			 * itself instead of fucking myself in the future.
			 */
			if (!lwc.isProtectable(block)) {
				return;
			}

			/*
			 * Enforce anything preventing us from creating a protection
			 */
			if (!lwc.isAdmin(player)) {
				if (lwc.enforceProtectionLimits(player) || lwc.enforceWorldGuard(player, block)) {
					if (lwc.notInPersistentMode(player.getName())) {
						lwc.getMemoryDatabase().unregisterAllActions(player.getName());
					}

					return;
				}
			}

			if (type.equals("public")) {
				lwc.getPhysicalDatabase().registerProtectedEntity(block.getTypeId(), ProtectionTypes.PUBLIC, player.getName(), "", block.getX(), block.getY(), block.getZ());
				player.sendMessage(Colors.Green + "Created Public " + lwc.materialToString(block) + " successfully");
			} else if (type.equals("password")) {
				String password = data.substring("password ".length());
				password = lwc.encrypt(password);

				lwc.getPhysicalDatabase().registerProtectedEntity(block.getTypeId(), ProtectionTypes.PASSWORD, player.getName(), password, block.getX(), block.getY(), block.getZ());
				lwc.getMemoryDatabase().registerPlayer(player.getName(), lwc.getPhysicalDatabase().loadProtectedEntity(block.getX(), block.getY(), block.getZ()).getId());
				player.sendMessage(Colors.Green + "Created password protected " + lwc.materialToString(block) + " successfully");
				player.sendMessage(Colors.Blue + "For convenience, you don't have to enter your password until");
				player.sendMessage(Colors.Blue + "you next log in");

			} else if (type.equals("private")) {
				String defaultEntities = action.getData();
				String[] entities = new String[0];

				if (defaultEntities.length() > "private ".length()) {
					defaultEntities = defaultEntities.substring("private ".length());
					entities = defaultEntities.split(" ");
				}

				lwc.getPhysicalDatabase().registerProtectedEntity(block.getTypeId(), ProtectionTypes.PRIVATE, player.getName(), "", block.getX(), block.getY(), block.getZ());

				player.sendMessage(Colors.Green + "Created Private " + lwc.materialToString(block) + " successfully");

				for (String userEntity : entities) {
					boolean isAdmin = false;
					int chestType = AccessRight.PLAYER;

					if (userEntity.startsWith("@")) {
						isAdmin = true;
						userEntity = userEntity.substring(1);
					}

					if (userEntity.toLowerCase().startsWith("g:")) {
						chestType = AccessRight.GROUP;
						userEntity = userEntity.substring(2);
					}

					lwc.getPhysicalDatabase().registerProtectionRights(lwc.getPhysicalDatabase().loadProtectedEntity(block.getX(), block.getY(), block.getZ()).getId(), userEntity, isAdmin ? 1 : 0, chestType);
					player.sendMessage(Colors.Green + "Registered rights for " + Colors.Gold + userEntity + ": " + (isAdmin ? "[" + Colors.Red + "ADMIN" + Colors.Gold + "]" : "") + " [" + (chestType == AccessRight.PLAYER ? "Player" : "Group") + "]");
				}
			} else if (type.equals("trap")) {
				String trapType = chop[1].toLowerCase();
				String reason = "";
				int protectionType = ProtectionTypes.TRAP_KICK; // default to kick

				if (chop.length > 3) {
					reason = StringUtils.join(chop, 2);
				}

				if (trapType.equals("ban")) {
					protectionType = ProtectionTypes.TRAP_BAN;
				}

				player.sendMessage(Colors.Green + "Created " + StringUtils.capitalizeFirstLetter(trapType) + " trap on " + lwc.materialToString(protection.getBlockId()) + " successfully");
				player.sendMessage(Colors.Green + "Will use the reason: " + Colors.Blue + "\"" + Colors.Red + reason + Colors.Blue + "\"");

				/*
				 * This.. is the definition of hackish. Let's rename "password" to "data", T_T
				 */
				lwc.getPhysicalDatabase().registerProtectedEntity(block.getTypeId(), protectionType, player.getName(), reason, block.getX(), block.getY(), block.getZ());
			}

			if (lwc.notInPersistentMode(player.getName())) {
				lwc.getMemoryDatabase().unregisterAllActions(player.getName());
			}
		}
	}

}
