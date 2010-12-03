import java.util.ArrayList;
import java.util.List;

import com.griefcraft.model.Action;
import com.griefcraft.model.ChestTypes;
import com.griefcraft.model.RightTypes;
import com.griefcraft.sql.MemoryDatabase;
import com.griefcraft.sql.PhysicalDatabase;

public class LWCListener extends PluginListener {

	/**
	 * Parent class
	 */
	private final LWC parent;
	
	/**
	 * Tnt blast radius
	 */
	private static final int BLAST_RADIUS = 4;

	/**
	 * Debug mode
	 */
	public boolean debugMode = false;
	
	/**
	 * If the server is actively using Magic Carpet, to detour around it
	 */
	private boolean magicCarpet = false;

	public LWCListener(LWC parent) {
		this.parent = parent;
	}
	
	// block.getStatus() , 1 = tnt, 2 = creeper
	// true = don't explode
	@Override
	public boolean onExplode(Block block) {
		boolean chestInRange = PhysicalDatabase.getInstance().loadChests(block.getX(), block.getY(), block.getZ(), BLAST_RADIUS).size() > 0;
		
		if(chestInRange) {
			/*
			 * !
			 */
			return true;
		}
		
		return false;
	}
	
	// true = revert
	@Override
	public void onBlockRightClicked(Player player, Block blockClicked, Item item) {
		if (blockClicked.getType() != 54) {
			return;
		}

		if (parent.isAdmin(player) && !debugMode) {
			return;
		}

		final Chest[] chestSet = parent.getChestSet(blockClicked.getX(),
				blockClicked.getY(), blockClicked.getZ());
		boolean hasAccess = true;

		for (final Chest chest : chestSet) {
			if (chest == null) {
				continue;
			}

			final com.griefcraft.model.Chest chest_ = PhysicalDatabase
			.getInstance().loadChest(chest.getX(), chest.getY(),
					chest.getZ());

			if (chest_ == null) {
				continue;
			}
			
			hasAccess = parent.canAccessChest(player, chest_);

			switch (chest_.getType()) {
			case ChestTypes.PUBLIC:
				hasAccess = true;
				break;

			case ChestTypes.PASSWORD:
				if (!hasAccess) {
					MemoryDatabase.getInstance().registerUnlock(
							player.getName(), chest_.getID());
					player.sendMessage(Colors.Red + "This chest is locked.");
					player.sendMessage(Colors.Red + "Type " + Colors.Gold
							+ "/lwc unlock <password>" + Colors.Red
							+ " to unlock it");
				}

				break;

			case ChestTypes.PRIVATE:
				if (!hasAccess) {
					player.sendMessage(Colors.Red
							+ "This chest is locked with a magical spell.");
				}

				break;
			}
		}

		for (final Chest chest : chestSet) {
			if (chest != null) {
				chest.update();
			}
		}

		return;
	}

	@Override
	public boolean onBlockBreak(Player player, Block block) {
		if (block.getType() != 54) {
			return false;
		}

		final Chest[] chestSet = parent.getChestSet(block.getX(), block.getY(),
				block.getZ());
		boolean hasAccess = true;
		com.griefcraft.model.Chest chest_ = null;

		for (final Chest chest : chestSet) {
			if (chest == null) {
				continue;
			}

			chest_ = PhysicalDatabase
			.getInstance().loadChest(chest.getX(), chest.getY(),
					chest.getZ());

			if (chest_ == null) {
				continue;
			}

			hasAccess = parent.canAccessChest(player, chest_);
		}

		if(hasAccess && chest_ != null) {
			if(parent.canAdminChest(player, chest_)) {
				PhysicalDatabase.getInstance().unregisterChest(chest_.getID());
				PhysicalDatabase.getInstance().unregisterAllRights(chest_.getID());
				player.sendMessage(Colors.Red + "Chest unregistered.");
			}
		}

		return false;
	}
	
	// true = revert
	@Override
	public boolean onBlockDestroy(Player player, Block block) {
		if (block.getType() != 54) {
			return false;
		}

		final Chest[] chestSet = parent.getChestSet(block.getX(), block.getY(),
				block.getZ());
		boolean hasAccess = true;
		com.griefcraft.model.Chest chest_ = null;

		for (final Chest chest : chestSet) {
			if (chest == null) {
				continue;
			}

			chest_ = PhysicalDatabase
			.getInstance().loadChest(chest.getX(), chest.getY(),
					chest.getZ());

			if (chest_ == null) {
				continue;
			}

			hasAccess = parent.canAccessChest(player, chest_);
		}
		
		if(block.getStatus() != 0) {
			return !hasAccess;
		}
		
		final List<String> actions = MemoryDatabase.getInstance().getActions(
				player.getName());

		final boolean hasFreeRequest = actions.contains("free");
		final boolean requestInfo = actions.contains("info");
		final boolean createChest = actions.contains("create");
		final boolean modifyChest = actions.contains("modify");
		
		boolean hasNoOwner = true;
		chest_ = null;

		for (final Chest chest : chestSet) {
			if (chest == null) {
				continue;
			}

			chest_ = PhysicalDatabase
			.getInstance().loadChest(chest.getX(), chest.getY(),
					chest.getZ());

			if (chest_ != null) {
				hasNoOwner = false;

				if (requestInfo) {
					String players = "";

					List<String> sessionUsers;

					if (chest_.getType() == ChestTypes.PASSWORD) {
						sessionUsers = MemoryDatabase.getInstance()
						.getSessionUsers(chest_.getID());

						/*
						 * Players authed to the chest are still in the
						 * game-- let's colour them their prefix!:D
						 */
						for (final String plr : sessionUsers) {
							final Player player_ = etc.getServer()
							.getPlayer(plr);

							if (player_ == null) {
								continue;
							}

							players += player_.getColor() + plr
							+ Colors.White + ", ";
						}

						if (sessionUsers.size() > 0) {
							players = players.substring(0,
									players.length() - 4);
						}
					}

					String type = " ";

					switch (chest_.getType()) {
					case ChestTypes.PUBLIC:
						type = "Public";
						break;
					case ChestTypes.PASSWORD:
						type = "Password";
						break;
					case ChestTypes.PRIVATE:
						type = "Private";
						break;
					}

					boolean canAdmin = parent.canAdminChest(player, chest_);
					// boolean canAccess = parent.canAccessChest(player, chest_);

					if(canAdmin) {
						player.sendMessage(Colors.Green + "ID: " + Colors.Gold
								+ chest_.getID());
					}

					player.sendMessage(Colors.Green + "Type: "
							+ Colors.Gold + type);
					player.sendMessage(Colors.Green + "Owner: "
							+ Colors.Gold + chest_.getOwner());

					if (chest_.getType() == ChestTypes.PASSWORD && canAdmin) {
						player.sendMessage(Colors.Green
								+ "Authed players: " + players);
					}

					if(canAdmin) {
						player.sendMessage(Colors.Green + "Location: "
								+ Colors.Gold + "{" + chest_.getX() + ", "
								+ chest_.getY() + ", " + chest_.getZ() + "}");
						player.sendMessage(Colors.Green + "Date created: "
								+ Colors.Gold + chest_.getDate());
					}

					if(parent.isInPersistentMode(player.getName())) {
						MemoryDatabase.getInstance().unregisterAllActions(
								player.getName());
					}
					return false;
				} else if (hasFreeRequest) {
					if (parent.isAdmin(player)
							|| chest_.getOwner().equals(player.getName())) {
						player.sendMessage(Colors.LightGreen
								+ "Removed lock on the chest succesfully!");
						PhysicalDatabase.getInstance().unregisterChest(
								chest_.getID());
						PhysicalDatabase.getInstance().unregisterAllRights(
								chest_.getID());
						if(parent.isInPersistentMode(player.getName())) {
							MemoryDatabase.getInstance().unregisterAllActions(
									player.getName());
						}
						return false;
					} else {
						player.sendMessage(Colors.Red
								+ "You do not own that chest!");
						if(parent.isInPersistentMode(player.getName())) {
							MemoryDatabase.getInstance().unregisterAllActions(
									player.getName());
						}
						return true;
					}
				} else if (modifyChest) {
					if (parent.canAdminChest(player, chest_)) {
						final Action action = MemoryDatabase.getInstance()
						.getAction("modify", player.getName());

						final String defaultEntities = action.getData();
						String[] entities = new String[0];

						if (defaultEntities.length() > 0) {
							entities = defaultEntities.split(" ");
						}

						if(parent.isInPersistentMode(player.getName())) {
							MemoryDatabase.getInstance().unregisterAllActions(
									player.getName());
						}

						for (String entity : entities) {
							boolean remove = false;
							boolean isAdmin = false;
							int chestType = RightTypes.PLAYER;

							if (entity.startsWith("-")) {
								remove = true;
								entity = entity.substring(1);
							}

							if (entity.startsWith("@")) {
								isAdmin = true;
								entity = entity.substring(1);
							}

							if (entity.toLowerCase().startsWith("g:")) {
								chestType = RightTypes.GROUP;
								entity = entity.substring(2);
							}

							final int chestID = PhysicalDatabase.getInstance().loadChest(block.getX(), block.getY(), block.getZ()).getID();

							if (!remove) {
								PhysicalDatabase.getInstance()
								.unregisterRights(chestID, entity);
								PhysicalDatabase.getInstance().registerRights(
										chestID, entity, isAdmin ? 1 : 0,
												chestType);
								player.sendMessage(Colors.LightGreen
										+ "Registered rights for "
										+ Colors.Gold
										+ entity + Colors.Green
										+ " "
										+ (isAdmin ? "[" + Colors.Red + "ADMIN"
												+ Colors.Gold + "]" : "")
												+ " ["
												+ (chestType == RightTypes.PLAYER ? "Player"
														: "Group") + "]");
							} else {
								PhysicalDatabase.getInstance()
								.unregisterRights(chestID, entity);
								player.sendMessage(Colors.LightGreen
										+ "Removed rights for "
										+ Colors.Gold
										+ entity + Colors.Green
										+ " ["
										+ (chestType == RightTypes.PLAYER ? "Player"
												: "Group") + "]");
							}
						}

						return false;
					} else {
						player.sendMessage(Colors.Red
								+ "You do not own that chest!");
						if(parent.isInPersistentMode(player.getName())) {
							MemoryDatabase.getInstance().unregisterAllActions(
									player.getName());
						}
						return true;
					}

				}

			}


			if (requestInfo || hasFreeRequest) {
				player.sendMessage(Colors.Red + "Chest is unregistered");
				if(parent.isInPersistentMode(player.getName())) {
					MemoryDatabase.getInstance().unregisterAllActions(player.getName());
				}
				return false;
			}

			if ((createChest || modifyChest) && !hasNoOwner) {
				if(!parent.canAdminChest(player, chest_)) {
					player.sendMessage(Colors.Red + "You do not own that chest!");
				} else {
					player.sendMessage(Colors.Red + "You have already registered that chest!");
				}
				if(parent.isInPersistentMode(player.getName())) {
					MemoryDatabase.getInstance().unregisterAllActions(player.getName());
				}
				return true;
			}

			if (hasNoOwner) {
				if (createChest) {
					
					final Action action = MemoryDatabase.getInstance().getAction(
							"create", player.getName());

					final String data = action.getData();
					final String[] chop = data.split(" ");
					final String type = chop[0].toLowerCase();
					String subset = "";

					if (chop.length > 1) {
						for (int i = 1; i < chop.length; i++) {
							subset += chop[i] + " ";
						}
					}

					if (parent.enforceChestLimits(player)) {
						if(parent.isInPersistentMode(player.getName())) {
							MemoryDatabase.getInstance().unregisterAllActions(player.getName());
						}
						return false;
					}

					if (type.equals("public")) {
						PhysicalDatabase.getInstance().registerChest(
								ChestTypes.PUBLIC, player.getName(), "",
								block.getX(), block.getY(), block.getZ());
						player.sendMessage(Colors.Green + "Registered chest lock.");
					} else if (type.equals("password")) {
						String password = action.getData().substring(
								"password ".length());
						password = parent.encrypt(password);

						PhysicalDatabase.getInstance().registerChest(
								ChestTypes.PASSWORD, player.getName(), password,
								block.getX(), block.getY(), block.getZ());
						MemoryDatabase.getInstance().registerPlayer(
								player.getName(),
								PhysicalDatabase
								.getInstance()
								.loadChest(block.getX(), block.getY(),
										block.getZ()).getID());
						player.sendMessage(Colors.Green + "Registered chest lock.");

						for (final Chest c : chestSet) {
							if (c != null) {
								c.update();
							}
						}

					} else if (type.equals("private")) {
						String defaultEntities = action.getData();
						String[] entities = new String[0];

						if (defaultEntities.length() > "private ".length()) {
							defaultEntities = defaultEntities.substring("private "
									.length());
							entities = defaultEntities.split(" ");
						}

						PhysicalDatabase.getInstance().registerChest(
								ChestTypes.PRIVATE, player.getName(), "",
								block.getX(), block.getY(), block.getZ());

						player.sendMessage(Colors.Green + "Registered chest lock.");

						for (String entity : entities) {
							boolean isAdmin = false;
							int chestType = RightTypes.PLAYER;

							if (entity.startsWith("@")) {
								isAdmin = true;
								entity = entity.substring(1);
							}

							if (entity.toLowerCase().startsWith("g:")) {
								chestType = RightTypes.GROUP;
								entity = entity.substring(2);
							}

							PhysicalDatabase.getInstance().registerRights(
									PhysicalDatabase
									.getInstance()
									.loadChest(block.getX(), block.getY(),
											block.getZ()).getID(), entity,
											isAdmin ? 1 : 0, chestType);
							player.sendMessage(Colors.LightGreen
									+ "Registered rights for "
									+ Colors.Gold
									+ entity
									+ ": "
									+ (isAdmin ? "[" + Colors.Red + "ADMIN"
											+ Colors.Gold + "]" : "")
											+ " ["
											+ (chestType == RightTypes.PLAYER ? "Player"
													: "Group") + "]");
						}

						if(parent.isInPersistentMode(player.getName())) {
							MemoryDatabase.getInstance().unregisterAllActions(player.getName());
						}
					}

					return false;
				}
			}
		}

		return !hasAccess;
	}
	
	@Override
	public void onPlayerMove(Player player, Location from, Location to) {
		if(!magicCarpet) {
			return;
		}

		etc.getServer().setBlock(etc.getServer().getBlockAt((int) from.x, (int) from.y, (int) from.z));
		etc.getServer().setBlock(etc.getServer().getBlockAt((int) to.x, (int) to.y, (int) to.z));
		
		/* List<com.griefcraft.model.Chest> chests = new ArrayList<com.griefcraft.model.Chest>();
		
		for(com.griefcraft.model.Chest chest_ : chests) {
			ComplexBlock chest = etc.getServer().getComplexBlock(chest_.getX(), chest_.getY(), chest_.getZ());
			
			if(chest == null) {
				continue;
			}
			
			chest.update();
		} */
		
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
		
		if(command.equalsIgnoreCase("mc") || command.equalsIgnoreCase("magiccarpet")) {
			player.sendMessage(Colors.Gold + "magic carpet detected");
			magicCarpet = true;
			return false;
		}

		/**
		 * ALIASES
		 */
		if(command.equalsIgnoreCase("cpublic")) {
			return onCommand(player, "/lwc create public".split(" "));
		} else if(command.equalsIgnoreCase("cprivate")) {
			return onCommand(player, "/lwc create private".split(" "));
		} else if(command.equalsIgnoreCase("cinfo")) {
			return onCommand(player, "/lwc info".split(" "));
		} else if(command.equalsIgnoreCase("cpassword")) {
			String password = subCommand;

			return onCommand(player, ("/lwc create password " + password).split(" "));
		}

		if (!player.canUseCommand(split[0])) {
			return false;
		}

		subCommand = subCommand.trim();

		if (command.equalsIgnoreCase("lwc")) {
			if (split.length < 2) {
				parent.sendFullHelp(player);
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
					player.sendMessage(Colors.LightGreen + "/lwc create public - Create a public chest");
					player.sendMessage(Colors.LightGreen + "/lwc create password <Password> - Create a password protected chest");
					player.sendMessage(Colors.LightGreen + "/lwc create private (Groups/Users) - Create a private chest");
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

				if (parent.enforceChestLimits(player)) {
					return true;
				}

				if (subAction.equals("public")) {
					MemoryDatabase.getInstance().unregisterAllActions(
							player.getName());
					MemoryDatabase.getInstance().registerAction("create",
							player.getName(), "public");
					player.sendMessage(Colors.LightGreen + "Chest type: PUBLIC");
					player.sendMessage(Colors.Green
							+ "Left click your chest to complete the locking process");
				} else if (subAction.equals("password")) {
					if (split.length < 4) {
						player.sendMessage(Colors.Red + "Usage: " + Colors.Gold
								+ "/lwc create password <Password>");
						return true;
					}

					final String password = subActions.substring("password "
							.length()); // Neatness !
					final String hiddenPass = parent.transform(password, '*');

					MemoryDatabase.getInstance().unregisterAllActions(
							player.getName());
					MemoryDatabase.getInstance().registerAction("create",
							player.getName(), subActions);
					player.sendMessage(Colors.LightGreen
							+ "Accepted password: " + Colors.Yellow
							+ hiddenPass);
					player.sendMessage(Colors.Green
							+ "Left click your chest to complete the locking process");
				} else if (subAction.equals("private")) {
					MemoryDatabase.getInstance().unregisterAllActions(
							player.getName());
					MemoryDatabase.getInstance().registerAction("create",
							player.getName(), subActions);
					player.sendMessage(Colors.LightGreen
							+ "Chest type: PRIVATE");
					player.sendMessage(Colors.Green
							+ "Left click your chest to complete the locking process");
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

				MemoryDatabase.getInstance().unregisterAllActions(
						player.getName());
				MemoryDatabase.getInstance().registerAction("modify",
						player.getName(), subActions);
				player.sendMessage(Colors.Green
						+ "Left click your chest to finish modifying the chest");
			} else if (action.equals("free")) {
				if(split.length < 3) {
					player.sendMessage("");
					player.sendMessage(Colors.Green + "LWC Protection");
					player.sendMessage("");
					player.sendMessage(Colors.LightGreen + "/lwc free chest - After issuing, left click a chest to remove it");
					player.sendMessage(Colors.LightGreen + "/lwc free modes - Remove any temporary modes you may have on you");
					player.sendMessage("");
					player.sendMessage(Colors.Green + "To remove a chest (i.e. free), you must be an LWC Admin or");
					player.sendMessage(Colors.Green + "the owner of the chest in question");
					return true;
				}

				if (subActions.toLowerCase().startsWith("chest")) {
					if (MemoryDatabase.getInstance().hasPendingChest(
							player.getName())) {
						player.sendMessage(Colors.Red
								+ "You already have a pending chest action.");
						return true;
					}

					MemoryDatabase.getInstance().unregisterAllActions(
							player.getName());
					MemoryDatabase.getInstance().registerAction("free",
							player.getName(), 0);
					player.sendMessage(Colors.LightGreen
							+ "Left click your chest to free the lock");
				} else if(subActions.toLowerCase().startsWith("modes")) {
					MemoryDatabase.getInstance().unregisterAllModes(player.getName());
					MemoryDatabase.getInstance().unregisterAllActions(player.getName());
					player.sendMessage(Colors.Green + "Cleared modes.");
				}

			} else if (action.equals("unlock")) {

				if (split.length < 3) {
					player.sendMessage(Colors.Red + "Usage: " + Colors.Gold
							+ "/lwc unlock <Password>");
					return true;
				}

				final String password = parent.encrypt(subActions);

				if (!MemoryDatabase.getInstance().hasPendingUnlock(
						player.getName())) {
					player.sendMessage(Colors.Red
							+ "No chest selected. (Open a locked chest)");
					return true;
				} else {
					final int chestID = MemoryDatabase.getInstance()
					.getUnlockID(player.getName());

					if (chestID == -1) {
						player.sendMessage(Colors.Red + "[lwc] Internal error.");
						return true;
					}

					final com.griefcraft.model.Chest chest = PhysicalDatabase
					.getInstance().loadChest(chestID);

					if (chest.getPassword().equals(password)) {
						player.sendMessage(Colors.Green + "Password accepted.");
						MemoryDatabase.getInstance().unregisterUnlock(
								player.getName());
						MemoryDatabase.getInstance().registerPlayer(
								player.getName(), chestID);

						for (final Chest chest_ : parent.getChestSet(
								chest.getX(), chest.getY(), chest.getZ())) {
							if (chest_ != null) {
								chest_.update();
							}
						}
					} else {
						player.sendMessage(Colors.Red + "Invalid password.");
					}

				}
			} else if (action.equals("info")) {
				MemoryDatabase.getInstance().unregisterAllActions(
						player.getName());
				MemoryDatabase.getInstance().registerAction("info",
						player.getName(), 0);
				player.sendMessage(Colors.LightGreen
						+ "Left click a chest to see information about it");
			} else if(action.equals("persist")) {
				MemoryDatabase.getInstance().registerMode(player.getName(), "persist");
				player.sendMessage(Colors.Green + "Mode activated.");
				player.sendMessage(Colors.Green + "Type " + Colors.Gold + "/lwc free modes" + Colors.Green + " to undo (or logout)");
			} else if (action.equals("admin") && parent.isAdmin(player)) {

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

				if (subAction.equalsIgnoreCase("clear")) {
					if (split.length < 3) {
						player.sendMessage(Colors.Red + "Usage: " + Colors.Gold
								+ "/lwc admin clear <chests|limits|modes>");
						return true;
					}

					final String command_ = split[3].toLowerCase();

					if (command_.equals("chests")) {
						PhysicalDatabase.getInstance().unregisterAllChests();
						player.sendMessage(Colors.Green + "Done.");
					} else if (command_.equals("limits")) {
						PhysicalDatabase.getInstance().unregisterAllLimits();
						player.sendMessage(Colors.Green + "Done.");
					}
				} else if (subAction.equalsIgnoreCase("limits")) {
					if (split.length < 4) {
						player.sendMessage(Colors.Red + "Usage: " + Colors.Gold
								+ "/lwc limits <amount> <groups/players>");
						player.sendMessage(Colors.Green
								+ "Groups should be in the formation \"g:GroupName\"");
						player.sendMessage(Colors.Green
								+ "IE: /lwc admin limits 1 Hidendra g:default SomeGuy");
						player.sendMessage(Colors.Green
								+ "PS: Limit of -2 removes the limit");
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
							PhysicalDatabase.getInstance().registerLimit(
									isGroup ? 0 : 1, limit, entity);
							player.sendMessage(Colors.Green
									+ "Registered limit of " + Colors.Gold
									+ limit + Colors.Green + " chests to the "
									+ (isGroup ? "group" : "user") + " "
									+ Colors.Gold + entity);
						} else {
							PhysicalDatabase.getInstance().unregisterLimit(
									isGroup ? 0 : 1, entity);
							player.sendMessage(Colors.Green
									+ "Unregistered limit for " + Colors.Gold
									+ entity);
						}
					}

					return true;
				}

			}

			return true;
		}

		return false;
	}

	// true = revert changes
	@Override
	public boolean onComplexBlockChange(Player player, ComplexBlock block) {
		if (!(block instanceof Chest)) {
			return false;
		}

		if (parent.isAdmin(player) && !debugMode) {
			return false;
		}

		final Chest[] chestSet = parent.getChestSet(block.getX(), block.getY(),
				block.getZ());
		boolean hasAccess = true;

		for (final Chest chest : chestSet) {
			if (chest == null) {
				continue;
			}

			final com.griefcraft.model.Chest chest_ = PhysicalDatabase
			.getInstance().loadChest(chest.getX(), chest.getY(),
					chest.getZ());

			if (chest_ == null) {
				continue;
			}

			hasAccess = parent.canAccessChest(player, chest_);
		}

		return !hasAccess;
	}

	@Override
	public void onDisconnect(Player player) {
		MemoryDatabase.getInstance().unregisterPlayer(player.getName());
		MemoryDatabase.getInstance().unregisterUnlock(player.getName());
		MemoryDatabase.getInstance().unregisterChest(player.getName());
		MemoryDatabase.getInstance().unregisterAllActions(player.getName());
	}

	// true = show chest as empty
	@Override
	public boolean onSendComplexBlock(Player player, ComplexBlock block) {
		return onComplexBlockChange(player, block);
	}

}
