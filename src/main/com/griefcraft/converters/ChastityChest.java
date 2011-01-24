package com.griefcraft.converters;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;

import org.bukkit.entity.Player;

import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.sql.PhysDB;
import com.griefcraft.util.Config;

/**
 * Convert Chastity chests to LWC
 */
public class ChastityChest implements Runnable {

	public static void main(String[] args) throws Exception {
		new ChastityChest();
	}

	/**
	 * File where potential chest dbs are
	 */
	private String[] CHESTS_FILES = new String[] { "ChastityChest.chests", "ChastityChest/ChastityChest.chests" };

	/**
	 * How many chests were converted
	 */
	private int converted = 0;

	/**
	 * The player that issued the command ingame (if any)
	 */
	private Player player;

	/**
	 * Physical database object
	 */
	private PhysDB physicalDatabase;

	public ChastityChest() {
		new Thread(this).start();
		physicalDatabase = new PhysDB();
	}

	public ChastityChest(Player player) {
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
			throw new FileNotFoundException("No Chastity Chest database found");
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

			String[] split = line.split("=");

			if (split.length < 2) {
				continue;
			}

			int[] coords = splitCoordinates(split[0]);
			
			int x = coords[0];
			int y = coords[1];
			int z = coords[2];
			String owner = split[1];

			int type = ProtectionTypes.PRIVATE;

			log(String.format("Registering chest to %s at location {%d,%d,%d}", owner, x, y, z));

			/*
			 * Register the chest
			 */
			physicalDatabase.registerProtectedEntity(type, owner, "", x, y, z);

			converted++;
		}
	}
	
	/**
	 * WHY OH WHY
	 * 
	 * @param str
	 * @return
	 */
	private int[] splitCoordinates(String str) {
		int[] coords = new int[3];
		String[] split = str.split("-");
		int index = 0;
		
		boolean _neg = false;
		
		for(String string : split) {
			if(string.isEmpty()) {
				_neg = true;
			}
			else {
				coords[index] = Integer.parseInt((_neg ? "-" : "") + string);
				_neg = false;
				index ++;
			}
		}
		
		return coords;
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
			Config.init();
			
			log("LWC Conversion tool for Chastity Chest chests");
			log("");
			log("Initializing sqlite");

			boolean connected = physicalDatabase.connect();

			if (!connected) {
				throw new ConnectException("Failed to connect to the sqlite database");
			}

			physicalDatabase.load();

			log("Done.");
			log("Starting conversion of Chest Protect chests");
			log("");

			convertChests();

			log("Done.");
			log("");
			log("Converted >" + converted + "< Chest Protect chests to LWC");
			log("LWC database now holds " + physicalDatabase.getProtectionCount() + " protected chests!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
