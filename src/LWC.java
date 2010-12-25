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

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import com.griefcraft.model.ChestTypes;
import com.griefcraft.model.RightTypes;
import com.griefcraft.sql.MemoryDatabase;
import com.griefcraft.sql.PhysicalDatabase;

public class LWC extends Plugin {

	/**
	 * The version
	 */
	public static final double VERSION = 1.37;

	/**
	 * The PluginListener
	 */
	private LWCListener listener;

	/**
	 * Check if a player can access a chest
	 * 
	 * @param player
	 *            the player to check
	 * @param Chest
	 *            the chest to check
	 * @return if the player can access the chest
	 */
	public boolean canAccessChest(Player player, com.griefcraft.model.Chest chest) {
		if (chest == null) {
			return true;
		}

		if (isAdmin(player)) {
			return true;
		}

		if (isMod(player)) {
			Player chestOwner = etc.getDataSource().getPlayer(chest.getOwner());

			if (chestOwner == null) {
				return true;
			}

			if (!isAdmin(chestOwner)) {
				return true;
			}
		}

		switch (chest.getType()) {
		case ChestTypes.PUBLIC:
			return true;

		case ChestTypes.PASSWORD:
			return MemoryDatabase.getInstance().hasAccess(player.getName(), chest);

		case ChestTypes.PRIVATE:
			final PhysicalDatabase instance = PhysicalDatabase.getInstance();
			return player.getName().equalsIgnoreCase(chest.getOwner()) || instance.getPrivateAccess(RightTypes.PLAYER, chest.getID(), player.getName()) != -1 || instance.getPrivateAccess(RightTypes.GROUP, chest.getID(), player.getGroups()) != -1;

		default:
			return false;
		}
	}

	/**
	 * Check if a player can access a chest
	 * 
	 * @param player
	 *            the player to check
	 * @param x
	 *            x coordinate of the chest
	 * @param y
	 *            y coordinate of the chest
	 * @param z
	 *            z coordinate of the chest
	 * @return if the player can access the chest
	 */
	public boolean canAccessChest(Player player, int x, int y, int z) {
		return canAccessChest(player, PhysicalDatabase.getInstance().loadChest(x, y, z));
	}

	/**
	 * Check if a player can administrate a chest
	 * 
	 * @param player
	 *            the player to check
	 * @param Chest
	 *            the chest to check
	 * @return if the player can administrate the chest
	 */
	public boolean canAdminChest(Player player, com.griefcraft.model.Chest chest) {
		if (chest == null) {
			return true;
		}

		if (isAdmin(player)) {
			return true;
		}

		switch (chest.getType()) {
		case ChestTypes.PUBLIC:
			return player.getName().equalsIgnoreCase(chest.getOwner());

		case ChestTypes.PASSWORD:
			return player.getName().equalsIgnoreCase(chest.getOwner()) && MemoryDatabase.getInstance().hasAccess(player.getName(), chest);

		case ChestTypes.PRIVATE:
			final PhysicalDatabase instance = PhysicalDatabase.getInstance();
			return player.getName().equalsIgnoreCase(chest.getOwner()) || instance.getPrivateAccess(RightTypes.PLAYER, chest.getID(), player.getName()) == 1 || instance.getPrivateAccess(RightTypes.GROUP, chest.getID(), player.getGroups()) == 1;

		default:
			return false;
		}
	}

	@Override
	public void disable() {

	}

	@Override
	public void enable() {
		log("Physical db location: " + PhysicalDatabase.getInstance().getDatabasePath());

		log("Opening sqlite databases (1 Physical & 1 Memory)");

		final boolean connected = PhysicalDatabase.getInstance().connect() && MemoryDatabase.getInstance().connect();

		if (!connected) {
			log("Failed to open sqlite databases");
		}

		PhysicalDatabase.getInstance().load();
		MemoryDatabase.getInstance().load();

		log("Chest count: " + PhysicalDatabase.getInstance().chestCount());
		log("Limit count: " + PhysicalDatabase.getInstance().limitCount());
	}

	public String encrypt(String plaintext) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA");
			md.update(plaintext.getBytes("UTF-8"));

			final byte[] raw = md.digest(); // step 4
			return byteArray2Hex(raw);
		} catch (final Exception e) {

		}

		return "";
	}

	/**
	 * Check for chest limits on a given player and return true if they are limited
	 * 
	 * @param player
	 *            the player to check
	 * @return true if they are limited
	 */
	public boolean enforceChestLimits(Player player) {
		final int userLimit = PhysicalDatabase.getInstance().getUserLimit(player.getName());

		/*
		 * Sort of redundant, but use the least amount of queries we can!
		 */
		if (userLimit != -1) {
			final int chests = PhysicalDatabase.getInstance().getChestCount(player.getName());

			if (chests >= userLimit) {
				player.sendMessage(Colors.Red + "You have exceeded the amount of chests you can lock!");
				return true;
			}
		} else {
			final int groupLimit = PhysicalDatabase.getInstance().getGroupLimit(player.getGroups().length > 0 ? player.getGroups()[0] : "default");

			if (groupLimit != -1) {
				final int chests = PhysicalDatabase.getInstance().getChestCount(player.getName());

				if (chests >= groupLimit) {
					player.sendMessage(Colors.Red + "You have exceeded the amount of chests you can lock!");
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Useful for getting double chests
	 * 
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 * @param z
	 *            the z coordinate
	 * @return the Chest[] array of chests
	 */
	public List<Chest> getChestSet(int x, int y, int z) {
		List<Chest> chests = new ArrayList<Chest>(2);

		for (int xD = -1; xD <= 1; xD++) {
			for (int zD = -1; zD <= 1; zD++) {
				final ComplexBlock block = etc.getServer().getComplexBlock(x + xD, y, z + zD);

				if (block == null || !(block instanceof Chest)) {
					continue;
				}

				final Chest chest = (Chest) block;

				if (chest != null) {
					chests.add(chest);
				}
			}
		}

		return chests;
	}

	public int getPlayerDropTransferTarget(String player) {
		String rawTarget = MemoryDatabase.getInstance().getModeData(player, "dropTransfer");
		try {
			int ret = Integer.parseInt(rawTarget.substring(1));
			return ret;
		} catch (final Throwable t) {
		}

		return -1;
	}

	@Override
	public void initialize() {
		log("Registering hooks");

		listener = new LWCListener(this);

		registerHook(PluginLoader.Hook.DISCONNECT);
		registerHook(PluginLoader.Hook.COMMAND);
		registerHook(PluginLoader.Hook.BLOCK_RIGHTCLICKED);
		registerHook(PluginLoader.Hook.BLOCK_BROKEN);
		registerHook(PluginLoader.Hook.BLOCK_DESTROYED);
		registerHook(PluginLoader.Hook.OPEN_INVENTORY);
		//registerHook(PluginLoader.Hook.COMPLEX_BLOCK_SEND);
		registerHook(PluginLoader.Hook.EXPLODE);
		registerHook(PluginLoader.Hook.ITEM_DROP);
	}

	/**
	 * Check if a player can do admin functions on LWC
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player is an LWC admin
	 */
	public boolean isAdmin(Player player) {
		return player.canUseCommand("/lwcadmin");
	}

	/**
	 * Check if a player can do mod functions on LWC
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player is an LWC mod
	 */
	public boolean isMod(Player player) {
		return player.canUseCommand("/lwcmod");
	}

	public void log(String str) {
		System.out.println("[LWC] [v" + VERSION + "] " + str);
	}

	/**
	 * Return if the player is in persistent mode
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player is NOT in persistent mode
	 */
	public boolean notInPersistentMode(String player) {
		return !MemoryDatabase.getInstance().hasMode(player, "persist");
	}

	public boolean playerIsDropTransferring(String player) {
		return MemoryDatabase.getInstance().hasMode(player, "dropTransfer") && MemoryDatabase.getInstance().getModeData(player, "dropTransfer").startsWith("t");
	}

	/**
	 * Send the full help to a player
	 * 
	 * @param player
	 *            the player to send to
	 */
	public void sendFullHelp(Player player) {
		player.sendMessage("");
		player.sendMessage(Colors.Green + "Welcome to LWC, a Protection mod");
		player.sendMessage("");

		player.sendMessage(Colors.Green + " Commands:");

		player.sendMessage(Colors.LightGreen + "/lwc create - View detailed info on chest types");
		player.sendMessage(Colors.LightGreen + "/lwc create public - Create a public chest");
		player.sendMessage(Colors.LightGreen + "/lwc create password - Create a password protected chest");
		player.sendMessage(Colors.LightGreen + "/lwc create private - Create a private chest");

		player.sendMessage("");

		player.sendMessage(Colors.LightGreen + "/lwc modify - Modify a protected chest");

		player.sendMessage("");

		player.sendMessage(Colors.LightGreen + "/lwc free chest - Remove a protected chest");
		player.sendMessage(Colors.LightGreen + "/lwc free modes - Remove temporary modes on you");

		player.sendMessage("");

		player.sendMessage(Colors.LightGreen + "/lwc persist - Allow use of 1 command multiple times");

		player.sendMessage(Colors.LightGreen + "/lwc unlock - Unlock a password protected chest");

		player.sendMessage("");

		player.sendMessage(Colors.LightGreen + "/lwc info - View information on a protected chest");

		player.sendMessage("");

		player.sendMessage(Colors.LightGreen + "/lwc droptransfer - View Drop Transfer help");

		player.sendMessage("");

		player.sendMessage(Colors.Red + "/lwc admin - (LWC ADMIN) Admin functions");
	}

	public void sendPendingRequest(Player player) {
		player.sendMessage(Colors.Red + "You already have a pending chest request.");
		player.sendMessage(Colors.Red + "To remove it, type /lwc free pending");
	}

	/**
	 * Transform a string into one char
	 * 
	 * @param str
	 *            The string to transform
	 * @param chr
	 *            The char to transform all chars to (ie '*')
	 * @return the transformed string
	 */
	public String transform(String str, char chr) {
		final char[] charArray = str.toCharArray();

		for (int i = 0; i < charArray.length; i++) {
			charArray[i] = chr;
		}

		return new String(charArray);
	}

	/**
	 * Convert a byte array to hex
	 * 
	 * @param hash
	 *            the hash to convert
	 * @return the converted hash
	 */
	private String byteArray2Hex(byte[] hash) {
		final Formatter formatter = new Formatter();
		for (final byte b : hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	/**
	 * Register a hook with default priority
	 * 
	 * @param hook
	 *            the hook to register
	 */
	private void registerHook(PluginLoader.Hook hook) {
		registerHook(hook, PluginListener.Priority.MEDIUM);
	}

	/**
	 * Register a hook
	 * 
	 * @param hook
	 *            the hook to register
	 * @priority the priority to use
	 */
	private void registerHook(PluginLoader.Hook hook, PluginListener.Priority priority) {
		log("LWCListener -> " + hook.toString());

		etc.getLoader().addListener(hook, listener, this, priority);
	}

}
