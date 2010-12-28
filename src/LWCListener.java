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

import static com.griefcraft.util.ConfigValues.ALLOW_FURNACE_PROTECTION;

import java.util.List;

import com.griefcraft.model.Action;
import com.griefcraft.model.Entity;
import com.griefcraft.model.EntityTypes;
import com.griefcraft.model.RightTypes;
import com.griefcraft.sql.MemDB;
import com.griefcraft.sql.PhysDB;
import com.griefcraft.util.Performance;

public class LWCListener extends PluginListener {

	/**
	 * Parent class
	 */
	private final LWC lwc;

	/**
	 * Tnt blast radius
	 */
	private static final int BLAST_RADIUS = 4;

	/**
	 * Debug mode TODO: hardly does anything, remove or fix that (after other TODOs are resolved)
	 */
	public boolean debugMode = false;

	/**
	 * Physical database object
	 */
	private PhysDB physicalDatabase;

	/**
	 * Memory database object
	 */
	private MemDB memoryDatabase;

	public LWCListener(LWC lwc) {
		this.lwc = lwc;
		physicalDatabase = lwc.getPhysicalDatabase();
		memoryDatabase = lwc.getMemoryDatabase();
	}

	@Override
	public boolean onBlockBreak(Player player, Block block) {
		if (!isProtectable(block)) {
			return false;
		}

		List<ComplexBlock> entitySet = lwc.getEntitySet(block.getX(), block.getY(), block.getZ());
		boolean hasAccess = true;
		boolean canAdmin = true;
		Entity entity = null;

		for (final ComplexBlock chest : entitySet) {
			if (chest == null) {
				continue;
			}

			entity = physicalDatabase.loadProtectedEntity(chest.getX(), chest.getY(), chest.getZ());

			if (entity == null) {
				continue;
			}

			hasAccess = lwc.canAccessChest(player, entity);
			canAdmin = lwc.canAdminChest(player, entity);
		}

		if (hasAccess && entity != null) {
			if (canAdmin) {
				physicalDatabase.unregisterProtectedEntity(entity.getX(), entity.getY(), entity.getZ());
				physicalDatabase.unregisterProtectionRights(entity.getID());
				player.sendMessage(Colors.Red + "Chest unregistered.");
			}
		}

		return !canAdmin;
	}

	/**
	 * TODO: rewrite
	 */
	@Override
	public boolean onBlockDestroy(Player player, Block block) {
		if (!isProtectable(block)) {
			return false;
		}

		List<ComplexBlock> entitySet = lwc.getEntitySet(block.getX(), block.getY(), block.getZ());
		boolean hasAccess = true;
		Entity entity = null;
		boolean hasNoOwner = true;

		for (final ComplexBlock chest : entitySet) {
			if (chest == null) {
				continue;
			}

			entity = physicalDatabase.loadProtectedEntity(chest.getX(), chest.getY(), chest.getZ());

			if (entity == null) {
				continue;
			}

			hasAccess = lwc.canAccessChest(player, entity);
			hasNoOwner = false;
			break;
		}

		if (block.getStatus() != 0) {
			return !hasAccess;
		}

		final List<String> actions = memoryDatabase.getActions(player.getName());

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

				if (entity.getType() == EntityTypes.PASSWORD) {
					sessionUsers = memoryDatabase.getSessionUsers(entity.getID());

					/*
					 * Players authed to the chest are still in the game-- let's colour them their prefix!:D
					 */
					for (final String plr : sessionUsers) {
						final Player player_ = etc.getServer().getPlayer(plr);

						if (player_ == null) {
							continue;
						}

						players += player_.getColor() + plr + Colors.White + ", ";
					}

					if (sessionUsers.size() > 0) {
						players = players.substring(0, players.length() - 4);
					}
				}

				String type = " ";

				switch (entity.getType()) {
				case EntityTypes.PUBLIC:
					type = "Public";
					break;
				case EntityTypes.PASSWORD:
					type = "Password";
					break;
				case EntityTypes.PRIVATE:
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

				if (entity.getType() == EntityTypes.PASSWORD && canAdmin) {
					player.sendMessage(Colors.Green + "Authed players: " + players);
				}

				if (canAdmin) {
					player.sendMessage(Colors.Green + "Location: " + Colors.Gold + "{" + entity.getX() + ", " + entity.getY() + ", " + entity.getZ() + "}");
					player.sendMessage(Colors.Green + "Date created: " + Colors.Gold + entity.getDate());
				}

				if (lwc.notInPersistentMode(player.getName())) {
					memoryDatabase.unregisterAllActions(player.getName());
				}
				return false;
			} else if (dropTransferReg) {
				final boolean canAccess = lwc.canAccessChest(player, entity);
				if (!canAccess) {
					player.sendMessage(Colors.Red + "You cannot use a chest that you cannot access as a drop transfer target.");
					player.sendMessage(Colors.Red + "If this is a passworded chest, please unlock it before retrying.");
					player.sendMessage(Colors.Red + "Use \"/lwc droptransfer select\" to try again.");
				} else {
					for (ComplexBlock __entity : entitySet) {
						if (!(__entity instanceof Chest) && !(__entity instanceof DoubleChest)) {
							player.sendMessage(Colors.Red + "You need to select a chest as the Drop Transfer target!");
							memoryDatabase.unregisterAllActions(player.getName());
							return false;
						}
					}

					memoryDatabase.registerMode(player.getName(), "dropTransfer", "f" + entity.getID());
					player.sendMessage(Colors.Green + "Successfully registered chest as drop transfer target.");
				}
				memoryDatabase.unregisterAllActions(player.getName()); // ignore
				// persist
				return false;
			} else if (hasFreeRequest) {
				if (lwc.isAdmin(player) || entity.getOwner().equals(player.getName())) {
					player.sendMessage(Colors.LightGreen + "Removed lock on the chest succesfully!");
					physicalDatabase.unregisterProtectedEntity(entity.getX(), entity.getY(), entity.getZ());
					physicalDatabase.unregisterProtectionRights(entity.getID());
					if (lwc.notInPersistentMode(player.getName())) {
						memoryDatabase.unregisterAllActions(player.getName());
					}
					return false;
				} else {
					player.sendMessage(Colors.Red + "You do not own that chest!");
					if (lwc.notInPersistentMode(player.getName())) {
						memoryDatabase.unregisterAllActions(player.getName());
					}
					return true;
				}
			} else if (modifyChest) {
				if (lwc.canAdminChest(player, entity)) {
					final Action action = memoryDatabase.getAction("modify", player.getName());

					final String defaultEntities = action.getData();
					String[] entities = new String[0];

					if (defaultEntities.length() > 0) {
						entities = defaultEntities.split(" ");
					}

					if (lwc.notInPersistentMode(player.getName())) {
						memoryDatabase.unregisterAllActions(player.getName());
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

						final int chestID = physicalDatabase.loadProtectedEntity(block.getX(), block.getY(), block.getZ()).getID();

						if (!remove) {
							physicalDatabase.unregisterProtectionRights(chestID, userEntity);
							physicalDatabase.registerProtectionRights(chestID, userEntity, isAdmin ? 1 : 0, chestType);
							player.sendMessage(Colors.LightGreen + "Registered rights for " + Colors.Gold + userEntity + Colors.Green + " " + (isAdmin ? "[" + Colors.Red + "ADMIN" + Colors.Gold + "]" : "") + " [" + (chestType == RightTypes.PLAYER ? "Player" : "Group") + "]");
						} else {
							physicalDatabase.unregisterProtectionRights(chestID, userEntity);
							player.sendMessage(Colors.LightGreen + "Removed rights for " + Colors.Gold + userEntity + Colors.Green + " [" + (chestType == RightTypes.PLAYER ? "Player" : "Group") + "]");
						}
					}

					return false;
				} else {
					player.sendMessage(Colors.Red + "You do not own that chest!");
					if (lwc.notInPersistentMode(player.getName())) {
						memoryDatabase.unregisterAllActions(player.getName());
					}
					return true;
				}
			}
		}

		if (dropTransferReg) {
			player.sendMessage(Colors.Red + "Cannot select unregistered chest as drop transfer target.");
			player.sendMessage(Colors.Red + "Use \"/lwc droptransfer select\" to try again.");
			memoryDatabase.unregisterAllActions(player.getName()); // ignore
			// persist

			return false;
		}

		if (requestInfo || hasFreeRequest) {
			player.sendMessage(Colors.Red + "Chest is unregistered");
			if (lwc.notInPersistentMode(player.getName())) {
				memoryDatabase.unregisterAllActions(player.getName());
			}
			return false;
		}

		if ((createChest || modifyChest) && !hasNoOwner) {
			if (!lwc.canAdminChest(player, entity)) {
				player.sendMessage(Colors.Red + "You do not own that chest!");
			} else {
				player.sendMessage(Colors.Red + "You have already registered that chest!");
			}
			if (lwc.notInPersistentMode(player.getName())) {
				memoryDatabase.unregisterAllActions(player.getName());
			}
			return true;
		}

		if (hasNoOwner && createChest) {
			final Action action = memoryDatabase.getAction("create", player.getName());

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
					memoryDatabase.unregisterAllActions(player.getName());
				}
				return false;
			}

			if (type.equals("public")) {
				physicalDatabase.registerProtectedEntity(EntityTypes.PUBLIC, player.getName(), "", block.getX(), block.getY(), block.getZ());
				player.sendMessage(Colors.Green + "Registered chest lock.");
			} else if (type.equals("password")) {
				String password = action.getData().substring("password ".length());
				password = lwc.encrypt(password);

				physicalDatabase.registerProtectedEntity(EntityTypes.PASSWORD, player.getName(), password, block.getX(), block.getY(), block.getZ());
				memoryDatabase.registerPlayer(player.getName(), physicalDatabase.loadProtectedEntity(block.getX(), block.getY(), block.getZ()).getID());
				player.sendMessage(Colors.Green + "Registered chest lock.");

				for (final ComplexBlock c : entitySet) {
					if (c != null) {
						c.update();
					}
				}

			} else if (type.equals("private")) {
				String defaultEntities = action.getData();
				String[] entities = new String[0];

				if (defaultEntities.length() > "private ".length()) {
					defaultEntities = defaultEntities.substring("private ".length());
					entities = defaultEntities.split(" ");
				}

				physicalDatabase.registerProtectedEntity(EntityTypes.PRIVATE, player.getName(), "", block.getX(), block.getY(), block.getZ());

				player.sendMessage(Colors.Green + "Registered chest lock.");

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

					physicalDatabase.registerProtectionRights(physicalDatabase.loadProtectedEntity(block.getX(), block.getY(), block.getZ()).getID(), userEntity, isAdmin ? 1 : 0, chestType);
					player.sendMessage(Colors.LightGreen + "Registered rights for " + Colors.Gold + userEntity + ": " + (isAdmin ? "[" + Colors.Red + "ADMIN" + Colors.Gold + "]" : "") + " [" + (chestType == RightTypes.PLAYER ? "Player" : "Group") + "]");
				}
			}

			if (lwc.notInPersistentMode(player.getName())) {
				memoryDatabase.unregisterAllActions(player.getName());
			}
		}

		return !hasAccess;
	}

	// true = revert
	@Override
	public void onBlockRightClicked(Player player, Block blockClicked, Item item) {
		if (!isProtectable(blockClicked)) {
			return;
		}

		if (lwc.isAdmin(player) && !debugMode) {
			return;
		}

		List<ComplexBlock> entitySet = lwc.getEntitySet(blockClicked.getX(), blockClicked.getY(), blockClicked.getZ());
		boolean hasAccess = true;

		_main: for (final ComplexBlock chest : entitySet) {
			if (chest == null) {
				continue;
			}

			final Entity entity = physicalDatabase.loadProtectedEntity(chest.getX(), chest.getY(), chest.getZ());

			if (entity == null) {
				continue;
			}

			hasAccess = lwc.canAccessChest(player, entity);

			switch (entity.getType()) {
			case EntityTypes.PUBLIC:
				hasAccess = true;
				break _main;

			case EntityTypes.PASSWORD:
				if (!hasAccess) {
					memoryDatabase.registerUnlock(player.getName(), entity.getID());
					player.sendMessage(Colors.Red + "This chest is locked.");
					player.sendMessage(Colors.Red + "Type " + Colors.Gold + "/lwc unlock <password>" + Colors.Red + " to unlock it");
				}

				break _main;

			case EntityTypes.PRIVATE:
				if (!hasAccess) {
					player.sendMessage(Colors.Red + "This chest is locked with a magical spell.");
				}

				break _main;
			}
		}

		for (final ComplexBlock chest : entitySet) {
			if (chest != null) {
				chest.update();
			}
		}

		return;
	}

	@Override
	public boolean onCommand(Player player, String[] split) {
		final String command = split[0].substring(1);
		String subCommand = "";

		if (split.length > 1) {
			for (int i = 1; i < split.length; i++) {
				subCommand += split[i] + " ";
			}
		}

		/**
		 * ALIASES
		 */
		if (command.equalsIgnoreCase("cpublic")) {
			return onCommand(player, "/lwc create public".split(" "));
		} else if (command.equalsIgnoreCase("cprivate")) {
			return onCommand(player, "/lwc create private".split(" "));
		} else if (command.equalsIgnoreCase("cinfo")) {
			return onCommand(player, "/lwc info".split(" "));
		} else if (command.equalsIgnoreCase("cpassword")) {
			String password = subCommand;

			return onCommand(player, ("/lwc create password " + password).split(" "));
		} else if (command.equalsIgnoreCase("dropxfer")) {
			return onCommand(player, ("/lwc droptransfer " + subCommand).split(" "));
		}

		if (!player.canUseCommand(split[0])) {
			return false;
		}

		subCommand = subCommand.trim();

		/**
		 * TODO: better command handler, perhaps?
		 */
		if (command.equalsIgnoreCase("lwc")) {
			if (split.length < 2) {
				lwc.sendFullHelp(player);
				return true;
			}

			final String action = split[1].toLowerCase();
			String subActions = "";

			if (split.length > 2) {
				for (int i = 2; i < split.length; i++) {
					subActions += split[i] + " ";
				}
			}

			subActions = subActions.trim();

			if (action.equals("create")) {
				if (split.length < 3) {
					player.sendMessage(Colors.Green + "LWC Protection");
					player.sendMessage("");
					player.sendMessage(Colors.LightGreen + "/lwc create public - Create a public protection");
					player.sendMessage(Colors.LightGreen + "/lwc create password <Password> - Create a password protected entity");
					player.sendMessage(Colors.LightGreen + "/lwc create private (Groups/Users) - Create a private protection");
					player.sendMessage("");
					player.sendMessage(Colors.Green + "Chest Types");
					player.sendMessage(Colors.Gold + "public " + Colors.LightGreen + "- Anyone can view the chest, but not destroy it");
					player.sendMessage("");
					player.sendMessage(Colors.Gold + "password " + Colors.LightGreen + "- Anyone (including the owner) must enter the");
					player.sendMessage(Colors.LightGreen + "chest password before being allowed in the chest");
					player.sendMessage("");
					player.sendMessage(Colors.Gold + "private " + Colors.LightGreen + "- Only users assigned to the chest can view it");
					player.sendMessage("");
					player.sendMessage(Colors.Green + "Defining Groups / Players");
					player.sendMessage(Colors.LightGreen + "Group : \"" + Colors.Gold + "g:GroupName" + Colors.LightGreen + "\"");
					player.sendMessage(Colors.LightGreen + "Player : \"" + Colors.Gold + "PlayerName" + Colors.LightGreen + "\"");
					player.sendMessage("");
					player.sendMessage(Colors.Blue + "Player names + groups are space seperated.");
					return true;
				}

				final String subAction = split[2];

				if (lwc.enforceChestLimits(player)) {
					return true;
				}

				if (subAction.equals("public")) {
					memoryDatabase.unregisterAllActions(player.getName());
					memoryDatabase.registerAction("create", player.getName(), "public");
					player.sendMessage(Colors.LightGreen + "Chest type: PUBLIC");
					player.sendMessage(Colors.Green + "Left click your entity to complete the locking process");
				} else if (subAction.equals("password")) {
					if (split.length < 4) {
						player.sendMessage(Colors.Red + "Usage: " + Colors.Gold + "/lwc create password <Password>");
						return true;
					}

					final String password = subActions.substring("password ".length()); // Neatness
					// !
					final String hiddenPass = lwc.transform(password, '*');

					memoryDatabase.unregisterAllActions(player.getName());
					memoryDatabase.registerAction("create", player.getName(), subActions);
					player.sendMessage(Colors.LightGreen + "Accepted password: " + Colors.Yellow + hiddenPass);
					player.sendMessage(Colors.Green + "Left click your entity to complete the locking process");
				} else if (subAction.equals("private")) {
					memoryDatabase.unregisterAllActions(player.getName());
					memoryDatabase.registerAction("create", player.getName(), subActions);
					player.sendMessage(Colors.LightGreen + "Chest type: PRIVATE");
					player.sendMessage(Colors.Green + "Left click your entity to complete the locking process");
				}
			} else if (action.equals("modify")) {
				if (split.length < 3) {
					player.sendMessage("");
					player.sendMessage(Colors.Green + "LWC Protection");
					player.sendMessage("");
					player.sendMessage(Colors.LightGreen + "/lwc modify (Groups/Users)");
					player.sendMessage("");
					player.sendMessage(Colors.Green + "Defining Groups / Players");
					player.sendMessage(Colors.LightGreen + "Group : \"" + Colors.Gold + "g:GroupName" + Colors.LightGreen + "\"");
					player.sendMessage(Colors.LightGreen + "Player : \"" + Colors.Gold + "PlayerName" + Colors.LightGreen + "\"");
					player.sendMessage(Colors.LightGreen + "Remove access : \"" + Colors.Gold + "-g:GroupName | -PlayerName" + Colors.LightGreen + "\"");
					player.sendMessage(Colors.LightGreen + "Chest admin : \"" + Colors.Gold + "@g:GroupName | @PlayerName" + Colors.LightGreen + "\"");
					player.sendMessage("");
					player.sendMessage(Colors.Green + "Chest admins (@) can Add/Remove users from a Private chest");
					player.sendMessage(Colors.Green + "They CANNOT remove the chest or prevent the owner from");
					player.sendMessage(Colors.Green + "accessing it");
					player.sendMessage(Colors.Blue + "Player names + groups are space seperated.");
					return true;
				}

				memoryDatabase.unregisterAllActions(player.getName());
				memoryDatabase.registerAction("modify", player.getName(), subActions);
				player.sendMessage(Colors.Green + "Left click your entity to finish modifying the chest");
			} else if (action.equals("free")) {
				if (split.length < 3) {
					player.sendMessage("");
					player.sendMessage(Colors.Green + "LWC Protection");
					player.sendMessage("");
					player.sendMessage(Colors.LightGreen + "/lwc free entity - Remove a protected entity");
					player.sendMessage(Colors.LightGreen + "/lwc free modes - Remove any temporary modes you may have on you");
					player.sendMessage("");
					player.sendMessage(Colors.Green + "To remove a protection, you must be an LWC Admin or");
					player.sendMessage(Colors.Green + "the owner of the entity");
					return true;
				}

				/**
				 * TODO: remove /lwc free chest in a later build
				 */
				if (subActions.toLowerCase().startsWith("chest") || subActions.toLowerCase().startsWith("entity")) {
					if (subActions.toLowerCase().startsWith("chest")) {
						player.sendMessage(Colors.Red + "(Please use " + Colors.Gold + "/lwc free entity" + Colors.Red + " instead)");
					}

					if (memoryDatabase.hasPendingChest(player.getName())) {
						player.sendMessage(Colors.Red + "You already have a pending action.");
						return true;
					}

					memoryDatabase.unregisterAllActions(player.getName());
					memoryDatabase.registerAction("free", player.getName(), 0);
					player.sendMessage(Colors.LightGreen + "Left click your entity to free the lock");
				} else if (subActions.toLowerCase().startsWith("modes")) {
					memoryDatabase.unregisterAllModes(player.getName());
					memoryDatabase.unregisterAllActions(player.getName());
					player.sendMessage(Colors.Green + "Cleared modes.");
				}

			} else if (action.equals("unlock")) {

				if (split.length < 3) {
					player.sendMessage(Colors.Red + "Usage: " + Colors.Gold + "/lwc unlock <Password>");
					return true;
				}

				final String password = lwc.encrypt(subActions);

				if (!memoryDatabase.hasPendingUnlock(player.getName())) {
					player.sendMessage(Colors.Red + "No chest selected. (Open a locked chest)");
					return true;
				} else {
					final int chestID = memoryDatabase.getUnlockID(player.getName());

					if (chestID == -1) {
						player.sendMessage(Colors.Red + "[lwc] Internal error.");
						return true;
					}

					final Entity chest = physicalDatabase.loadProtectedEntity(chestID);

					if (chest.getPassword().equals(password)) {
						player.sendMessage(Colors.Green + "Password accepted.");
						memoryDatabase.unregisterUnlock(player.getName());
						memoryDatabase.registerPlayer(player.getName(), chestID);

						for (final ComplexBlock entity : lwc.getEntitySet(chest.getX(), chest.getY(), chest.getZ())) {
							if (entity != null) {
								entity.update();
							}
						}
					} else {
						player.sendMessage(Colors.Red + "Invalid password.");
					}

				}
			} else if (action.equals("info")) {
				memoryDatabase.unregisterAllActions(player.getName());
				memoryDatabase.registerAction("info", player.getName(), 0);
				player.sendMessage(Colors.LightGreen + "Left click a block to see information about it");
			} else if (action.equals("persist")) {
				String mode = "persist";

				if (!lwc.isAdmin(player) && lwc.isModeBlacklisted(mode)) {
					player.sendMessage(Colors.Red + "That mode is currently disabled");
					return true;
				}

				memoryDatabase.registerMode(player.getName(), mode);
				player.sendMessage(Colors.Green + "Mode activated.");
				player.sendMessage(Colors.Green + "Type " + Colors.Gold + "/lwc free modes" + Colors.Green + " to undo (or logout)");
			} else if (action.equals("droptransfer")) {
				String mode = "dropTransfer";

				if (!lwc.isAdmin(player) && lwc.isModeBlacklisted(mode)) {
					player.sendMessage(Colors.Red + "That mode is currently disabled");
					return true;
				}

				if (split.length < 3) {
					player.sendMessage(Colors.Green + "LWC Drop Transfer");
					player.sendMessage("");
					player.sendMessage(Colors.LightGreen + "/lwc droptransfer select - Select a chest to drop transfer to");
					player.sendMessage(Colors.LightGreen + "/lwc droptransfer on - Turn on drop transferring");
					player.sendMessage(Colors.LightGreen + "/lwc droptransfer off - Turn off drop transferring");
					player.sendMessage(Colors.LightGreen + "/lwc droptransfer status - Turn off drop transferring");
					return true;
				}

				final String subAction = split[2].toLowerCase();
				final String playerName = player.getName();

				if (subAction.equals("select")) {
					if (lwc.playerIsDropTransferring(playerName)) {
						player.sendMessage(Colors.Red + "Please turn off drop transfer before reselecting a chest.");
						return true;
					}

					memoryDatabase.unregisterMode(playerName, mode);
					memoryDatabase.registerAction("dropTransferSelect", playerName, "");
					player.sendMessage(Colors.Green + "Please left-click a registered chest to set as your transfer target.");
				} else if (subAction.equals("on")) {
					int target = lwc.getPlayerDropTransferTarget(playerName);
					if (target == -1) {
						player.sendMessage(Colors.Red + "Please register a chest before turning drop transfer on.");
						return true;
					}

					memoryDatabase.unregisterMode(playerName, "dropTransfer");
					memoryDatabase.registerMode(playerName, "dropTransfer", "t" + target);
					player.sendMessage(Colors.Green + "Drop transfer is now on.");
					player.sendMessage(Colors.Green + "Any items dropped will be transferred to your chest.");
				} else if (subAction.equals("off")) {
					int target = lwc.getPlayerDropTransferTarget(playerName);
					if (target == -1) {
						player.sendMessage(Colors.Red + "Please register a chest before turning drop transfer off.");
						return true;
					}

					memoryDatabase.unregisterMode(playerName, "dropTransfer");
					memoryDatabase.registerMode(playerName, "dropTransfer", "f" + target);
					player.sendMessage(Colors.Green + "Drop transfer is now off.");
				} else if (subAction.equals("status")) {
					if (lwc.getPlayerDropTransferTarget(playerName) == -1) {
						player.sendMessage(Colors.Green + "You have not registered a drop transfer target.");
						return true;
					} else {
						if (lwc.playerIsDropTransferring(playerName)) {
							player.sendMessage(Colors.Green + "Drop transfer is currently active.");
						} else {
							player.sendMessage(Colors.Green + "Drop transfer is currently inactive.");
						}
					}
				}
				return true;
			} else if (action.equals("admin") && lwc.isAdmin(player)) {

				if (split.length < 3) {
					player.sendMessage("");
					player.sendMessage(Colors.Green + "LWC Admin Help");
					player.sendMessage("");
					player.sendMessage(Colors.LightGreen + "/lwc admin limits - Create protection limits");
					player.sendMessage(Colors.Green + "Example: " + Colors.Gold + "/lwc admin limits 1 g:default Notch");
					player.sendMessage(Colors.Green + "will give a limit of 1 chest to Notch and players in the default group");
					player.sendMessage("");
					player.sendMessage(Colors.LightGreen + "/lwc admin clear chests - " + Colors.Red + "REMOVES" + Colors.LightGreen + " all protected chests");
					player.sendMessage(Colors.LightGreen + "/lwc admin clear limits - " + Colors.Red + "REMOVES" + Colors.LightGreen + " all protection limits");
					return true;
				}

				final String subAction = split[2];

				if (subAction.equalsIgnoreCase("report")) {
					Performance.setChestCount(lwc.getPhysicalDatabase().entityCount());
					Performance.setPlayersOnline(etc.getServer().getPlayerList().size());

					for(String line : Performance.getGeneratedReport()) {
						player.sendMessage(Colors.Green + line);
					}

					Performance.clear();
				} else if (subAction.equalsIgnoreCase("clear")) {
					if (split.length < 3) {
						player.sendMessage(Colors.Red + "Usage: " + Colors.Gold + "/lwc admin clear <chests|limits|modes>");
						return true;
					}

					final String command_ = split[3].toLowerCase();

					if (command_.equals("chests")) {
						physicalDatabase.unregisterProtectionEntities();
						player.sendMessage(Colors.Green + "Done.");
					} else if (command_.equals("limits")) {
						physicalDatabase.unregisterProtectionLimits();
						player.sendMessage(Colors.Green + "Done.");
					}
				} else if (subAction.equalsIgnoreCase("limits")) {
					if (split.length < 4) {
						player.sendMessage(Colors.Red + "Usage: " + Colors.Gold + "/lwc limits <amount> <groups/players>");
						player.sendMessage(Colors.Green + "Groups should be in the formation \"g:GroupName\"");
						player.sendMessage(Colors.Green + "IE: /lwc admin limits 1 Hidendra g:default SomeGuy");
						player.sendMessage(Colors.Green + "PS: Limit of -2 removes the limit");
						return true;
					}

					final int limit = Integer.parseInt(split[3]);

					for (int i = 4; i < split.length; i++) {
						String entity = split[i];
						final boolean isGroup = entity.startsWith("g:");

						if (isGroup) {
							entity = entity.substring(2);
						}

						if (limit != -2) {
							physicalDatabase.registerProtectionLimit(isGroup ? 0 : 1, limit, entity);
							player.sendMessage(Colors.Green + "Registered limit of " + Colors.Gold + limit + Colors.Green + " chests to the " + (isGroup ? "group" : "user") + " " + Colors.Gold + entity);
						} else {
							physicalDatabase.unregisterProtectionLimit(isGroup ? 0 : 1, entity);
							player.sendMessage(Colors.Green + "Unregistered limit for " + Colors.Gold + entity);
						}
					}

					return true;
				}

			} else if (action.equals("convert") && lwc.isAdmin(player)) {
				if (split.length < 3) {
					player.sendMessage("");
					player.sendMessage(Colors.Green + "LWC Conversion Help");
					player.sendMessage("");
					player.sendMessage(Colors.Green + "This command converts chests of other plugins to LWC.");
					player.sendMessage("");
					player.sendMessage(Colors.LightGreen + "/lwc convert <OtherPlugin>");
					player.sendMessage("");
					player.sendMessage("Currently supported: " + Colors.Gold + "chestprotect");
				}

				String subAction = split[2];

				if (subAction.equalsIgnoreCase("chestprotect")) {
					new CPConverter(player);
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public void onDisconnect(Player player) {
		memoryDatabase.unregisterPlayer(player.getName());
		memoryDatabase.unregisterUnlock(player.getName());
		memoryDatabase.unregisterChest(player.getName());
		memoryDatabase.unregisterAllActions(player.getName());
	}

	// block.getStatus() , 1 = tnt, 2 = creeper
	// true = don't explode
	@Override
	public boolean onExplode(Block block) {
		boolean chestInRange = physicalDatabase.loadProtectedEntities(block.getX(), block.getY(), block.getZ(), BLAST_RADIUS).size() > 0;

		if (chestInRange) {
			/*
			 * !
			 */
			return true;
		}

		return false;
	}

	/**
	 * TODO: fix
	 */
	@Override
	public boolean onItemDrop(Player player, Item item) {
		final String pn = player.getName();
		final int targetId = lwc.getPlayerDropTransferTarget(pn);

		if (targetId == -1 || !lwc.playerIsDropTransferring(pn)) {
			return false;
		}

		if (!physicalDatabase.doesChestExist(targetId)) {
			player.sendMessage(Colors.Red + "Your drop transfer target was unregistered and/or destroyed.");
			player.sendMessage(Colors.Red + "Please re-register a target chest. Drop transfer will be deactivated.");

			memoryDatabase.unregisterMode(pn, "dropTransfer");
			return false;
		}

		Entity entity = physicalDatabase.loadProtectedEntity(targetId);

		if (entity == null) {
			player.sendMessage(Colors.Red + "An unknown error occured. Drop transfer will be deactivated.");

			memoryDatabase.unregisterMode(pn, "dropTransfer");
			return false;
		}

		if (!lwc.canAccessChest(player, entity)) {
			player.sendMessage(Colors.Red + "You have lost access to your target chest.");
			player.sendMessage(Colors.Red + "Please re-register a target chest. Drop transfer will be deactivated.");

			memoryDatabase.unregisterMode(pn, "dropTransfer");
			return false;
		}

		List<ComplexBlock> chests = lwc.getEntitySet(entity.getX(), entity.getY(), entity.getZ());
		int remainingAmt = item.getAmount();

		for (ComplexBlock chest : chests) {
			Inventory inventory = (Inventory) chest;
			Item toStack;

			for (Item debug : inventory.getContents()) {
				System.out.println("" + debug.getItemId() + ":" + debug.getAmount() + " " + debug.getSlot());
			}

			while (((toStack = inventory.getItemFromId(item.getItemId(), 63)) != null || inventory.getEmptySlot() != -1) && remainingAmt > 0) {
				if (toStack != null) {
					int amtDelta = Math.min(64 - toStack.getAmount(), item.getAmount());
					inventory.setSlot(item.getItemId(), toStack.getAmount() + amtDelta, toStack.getSlot());
					remainingAmt -= amtDelta;
				} else {
					inventory.addItem(new Item(item.getItemId(), remainingAmt));
					remainingAmt = 0;
				}

			}

			chest.update();

			if (remainingAmt == 0) {
				break;
			}
		}

		if (remainingAmt > 0) {
			player.sendMessage(Colors.Red + "Your chest is full. Drop transfer will be deactivated.");
			player.sendMessage(Colors.Red + "Any remaining quantity that could not be stored will be returned.");
			memoryDatabase.unregisterMode(pn, "dropTransfer");
			memoryDatabase.registerMode(pn, "dropTransfer", "f" + targetId);
			player.getInventory().addItem(item);
			player.getInventory().update();
		}

		return true;
	}

	@Override
	public boolean onOpenInventory(Player player, Inventory inventory) {
		if (lwc.isAdmin(player) && !debugMode) {
			return false;
		}

		if(inventory instanceof Workbench) {
			return false;
		}

		ComplexBlock block = (ComplexBlock) inventory;

		if(!isProtectable(block.getBlock())) {
			return false;
		}

		List<ComplexBlock> entitySet = lwc.getEntitySet(block.getX(), block.getY(), block.getZ());
		boolean hasAccess = true;

		for (final ComplexBlock chest : entitySet) {
			if (chest == null) {
				continue;
			}

			final Entity entity = physicalDatabase.loadProtectedEntity(chest.getX(), chest.getY(), chest.getZ());

			if (entity == null) {
				continue;
			}

			hasAccess = lwc.canAccessChest(player, entity);
		}

		return !hasAccess;
	}

	/**
	 * Check a block to see if it is protectable
	 * 
	 * @param block
	 * @return
	 */
	private boolean isProtectable(Block block) {
		switch (block.getType()) {

		case 54: /* Chest */
			return true;

		case 61: /* Furnace */
		case 62: /* Lit furnace */
			if (ALLOW_FURNACE_PROTECTION.getBool()) {
				return true;
			}

			break;

		}

		return false;
	}

}
