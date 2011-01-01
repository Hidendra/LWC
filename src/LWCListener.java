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
			
			if(!lwc.isAdmin(player) && lwc.isInCuboidSafeZone(player)) {
				player.sendMessage(Colors.Red + "You need to be in a Cuboid-protected safe zone to do that!");
				memoryDatabase.unregisterAllActions(player.getName());
				return false;
			}

			if (type.equals("public")) {
				physicalDatabase.registerProtectedEntity(EntityTypes.PUBLIC, player.getName(), "", block.getX(), block.getY(), block.getZ());
				player.sendMessage(Colors.Green + "Created public protection successfully");
			} else if (type.equals("password")) {
				String password = action.getData().substring("password ".length());
				password = lwc.encrypt(password);

				physicalDatabase.registerProtectedEntity(EntityTypes.PASSWORD, player.getName(), password, block.getX(), block.getY(), block.getZ());
				memoryDatabase.registerPlayer(player.getName(), physicalDatabase.loadProtectedEntity(block.getX(), block.getY(), block.getZ()).getID());
				player.sendMessage(Colors.Green + "Created password protection successfully");
				player.sendMessage(Colors.LightGreen + "For convenience, you don't have to enter your password until");
				player.sendMessage(Colors.LightGreen + "you next log in");

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

	@Override
	public boolean onCommand(Player player, String[] split) {
		final String command = split[0].substring(1);
		String subCommand = "";
		String[] args = split.length > 1 ? new String[split.length - 1] : new String[0];

		/* Calculate the arguments used internally */
		if (split.length > 1) {
			for (int i = 1; i < split.length; i++) {
				split[i] = split[i].trim();

				if (split[i].isEmpty()) {
					continue;
				}

				args[i - 1] = split[i];
				subCommand += split[i] + " ";
			}
		}
		
		if(command.equals("cpublic")) {
			return onCommand(player, "/lwc -c public".split(" "));
		}
		else if(command.equals("cpassword")) {
			return onCommand(player, ("/lwc -c password " + subCommand).split(" "));
		}
		else if(command.equals("cprivate")) {
			return onCommand(player, "/lwc -c private".split(" "));
		}
		else if(command.equals("cinfo")) {
			return onCommand(player, "/lwc -i".split(" "));
		}
		else if(command.equals("cunlock")) {
			return onCommand(player, "/lwc -u".split(" "));
		}

		if (!player.canUseCommand(split[0])) {
			return false;
		}

		if (!"lwc".equalsIgnoreCase(command)) {
			return false;
		}

		if (args.length == 0) {
			lwc.sendFullHelp(player);
			return true;
		}

		for (Command cmd : lwc.getCommands()) {
			if (!cmd.validate(lwc, player, args)) {
				continue;
			}

			cmd.execute(lwc, player, args);
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

		if (targetId == -1 || !lwc.isPlayerDropTransferring(pn)) {
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

		if (inventory instanceof Workbench) {
			return false;
		}

		ComplexBlock block = (ComplexBlock) inventory;

		if (!isProtectable(block.getBlock())) {
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
			
			switch (entity.getType()) {
			case EntityTypes.PASSWORD:
				if (!hasAccess) {
					memoryDatabase.unregisterUnlock(player.getName());
					memoryDatabase.registerUnlock(player.getName(), entity.getID());
					
					player.sendMessage(Colors.Red + "This chest is locked.");
					player.sendMessage(Colors.Red + "Type " + Colors.Gold + "/lwc -u <password>" + Colors.Red + " to unlock it");
				}

				break;

			case EntityTypes.PRIVATE:
				if (!hasAccess) {
					player.sendMessage(Colors.Red + "This chest is locked with a magical spell.");
				}

				break;
			}
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
