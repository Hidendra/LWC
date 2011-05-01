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

package com.griefcraft.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.bukkit.command.CommandSender;

import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.AccessRight;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.sql.PhysDB;
import com.griefcraft.util.Config;

/**
 * Convert Chest Protect chests to LWC
 */
public class ChestProtect implements Runnable {

	/**
	 * File where Chest Protect saves chests
	 */
	private String[] CHESTS_FILES = new String[] { "../lockedChests.txt", "lockedChests.txt" };

	/**
	 * How many chests were converted
	 */
	private int converted = 0;

	/**
	 * Physical database object
	 */
	private PhysDB physicalDatabase;

	/**
	 * The player that issued the command ingame (if any)
	 */
	private CommandSender player;

	public ChestProtect() {
		new Thread(this).start();
		physicalDatabase = new PhysDB();
	}

	public ChestProtect(CommandSender player) {
		this();
		this.player = player;
	}

	/**
	 * Load the Chest Protect chests
	 */
	public void convertChests() throws FileNotFoundException, IOException {
		File file = null;

		for (String path : CHESTS_FILES) {
			file = new File(path);

			if (file != null && file.exists()) {
				break;
			}
		}

		if (file == null || !file.exists()) {
			throw new FileNotFoundException("No Chest Protect chest database found");
		}

		String line;
		int lineNumber = 0;
		BufferedReader reader = new BufferedReader(new FileReader(file));

		while ((line = reader.readLine()) != null) {
			line = line.trim();

			lineNumber++;

			if (line.startsWith("#")) {
				// comment !
				continue;
			}

			String[] split = line.split(",");

			if (split.length < 5) {
				continue;
			}

			String owner = split[0];
			int x = Integer.parseInt(split[1]);
			int y = Integer.parseInt(split[2]);
			int z = Integer.parseInt(split[3]);
			int type = Integer.parseInt(split[4]);
			int rightsType = -1;
			String users = "";

			if (type == 1) {
				type = ProtectionTypes.PUBLIC;
			} else if (type > 1) {
				if (type == 3) {
					rightsType = AccessRight.GROUP;
				} else if (type == 4) {
					rightsType = AccessRight.PLAYER;
				}

				type = ProtectionTypes.PRIVATE;
			}

			if (split.length > 5) {
				users = split[5].trim();
			}

			log(String.format("Registering chest to %s at location {%d,%d,%d}", owner, x, y, z));

			/*
			 * Register the chest
			 */
			physicalDatabase.registerProtection(0, type, "", owner, "", x, y, z);

			converted++;

			/**
			 * If rightsType is still -1, we're done with this chest
			 */
			if (rightsType == -1) {
				continue;
			}

			/**
			 * The id of the chest we just registered
			 */
			int chestID = physicalDatabase.loadProtection("", x, y, z).getId();

			/**
			 * Now register the extra users
			 */
			String[] extra = users.split(";");

			for (String entity : extra) {
				physicalDatabase.registerProtectionRights(chestID, entity, 0, rightsType);
				log(String.format("  -> Registering rights to %s on chest %d", entity, chestID));
			}
		}
		
		reader.close();
	}

	public void log(String str) {
		System.out.println(str);

		if (player != null) {
			player.sendMessage(str);
		}
	}

	@Override
	public void run() {
		try {
			log("LWC Conversion tool for Chest Protect chests");
			log("");

			Config.init();

			LWCPlugin plugin = new LWCPlugin();
			plugin.loadDatabase();
			physicalDatabase = new PhysDB();
			physicalDatabase.connect();
			physicalDatabase.load();

			convertChests();

			log("Done.");
			log("");
			log("Converted >" + converted + "< Chest Protect chests to LWC");
			log("LWC database now holds " + physicalDatabase.getProtectionCount() + " protected chests!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		new ChestProtect();
	}

}
