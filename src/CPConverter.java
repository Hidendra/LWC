import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import com.griefcraft.model.ChestTypes;
import com.griefcraft.model.RightTypes;
import com.griefcraft.sql.PhysicalDatabase;

/**
 * Convert Chest Protect chests to LWC
 */
public class CPConverter implements Runnable {

	/**
	 * File where Chest Protect saves chests
	 */
	private String[] CHESTS_FILES = new String[] { "../lockedChests.txt", "lockedChests.txt" };

	/**
	 * How many chests were converted
	 */
	private int converted = 0;

	/**
	 * The player that issued the command ingame (if any)
	 */
	private Player player;

	public CPConverter() {
		new Thread(this).start();
	}

	public CPConverter(Player player) {
		this();
		this.player = player;
	}

	public void run() {
		try {
			log("LWC Conversion tool for Chest Protect chests");
			log("");
			log("Initializing sqlite");

			boolean connected = PhysicalDatabase.getInstance().connect();

			if (!connected) {
				throw new ConnectException("Failed to connect to the sqlite database");
			}

			PhysicalDatabase.getInstance().load();

			log("Done.");
			log("Starting conversion of Chest Protect chests");
			log("");

			convertChests();

			log("Done.");
			log("");
			log("Converted >" + converted + "< Chest Protect chests to LWC");
			log("LWC database now holds " + PhysicalDatabase.getInstance().chestCount() + " protected chests!");
		} catch (Exception e) {
			e.printStackTrace();
		}
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
				type = ChestTypes.PUBLIC;
			} else if (type > 1) {
				if (type == 3) {
					rightsType = RightTypes.GROUP;
				} else if (type == 4) {
					rightsType = RightTypes.PLAYER;
				}

				type = ChestTypes.PRIVATE;
			}

			if (split.length > 5) {
				users = split[5].trim();
			}

			log(String.format("Registering chest to %s at location {%d,%d,%d}", owner, x, y, z));

			/*
			 * Register the chest
			 */
			PhysicalDatabase.getInstance().registerChest(type, owner, "", x, y, z);

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
			int chestID = PhysicalDatabase.getInstance().loadChest(x, y, z).getID();

			/**
			 * Now register the extra users
			 */
			String[] extra = users.split(";");

			for (String entity : extra) {
				PhysicalDatabase.getInstance().registerRights(chestID, entity, 0, rightsType);
				log(String.format("  -> Registering rights to %s on chest %d", entity, chestID));
			}
		}
	}

	public void log(String str) {
		System.out.println(str);

		if (player != null) {
			player.sendMessage(str);
		}
	}

	public static void main(String[] args) throws Exception {
		new CPConverter();
	}

}
