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

package com.griefcraft.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.griefcraft.logging.Logger;
import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.sql.Database;

public class Updater {

	/**
	 * Internal config
	 */
	private HashMap<String, String> config = new HashMap<String, String>();

	private double latestInternalVersion = 0.00;

	/**
	 * The latest LWC version
	 */
	private double latestPluginVersion = 0.00;

	/**
	 * The logging object for this class
	 */
	private Logger logger = Logger.getLogger(getClass().getSimpleName());

	/**
	 * List of files to download
	 */
	private List<UpdaterFile> needsUpdating = new ArrayList<UpdaterFile>();

	/**
	 * The folder where libraries are stored
	 */
	public final static String DEST_LIBRARY_FOLDER = "plugins/LWC/";

	/**
	 * File used for the distribution
	 */
	public final static String DIST_FILE = "lwc/release/LWC.jar";

	/**
	 * URL to the base update site
	 */
	public final static String UPDATE_SITE = "http://griefcraft.com/bukkit/";

	/**
	 * File used to obtain the latest version
	 */
	public final static String VERSION_FILE = "lwc/VERSION";

	public Updater() {
		/*
		 * Default config values
		 */
		config.put("sqlite", "1.00");

		/*
		 * Parse the internal config
		 */
		parseInternalConfig();
	}

	/**
	 * Check for dependencies
	 * 
	 * @return true if LWC should be reloaded
	 */
	public void check() {
		String[] paths = new String[] { DEST_LIBRARY_FOLDER + "lib/" + Database.DefaultType.getDriver(), getFullNativeLibraryPath() };

		for (String path : paths) {
			File file = new File(path);

			if (file != null && !file.exists() && !file.isDirectory()) {
				UpdaterFile updaterFile = new UpdaterFile(UPDATE_SITE + "shared/" + path.replaceAll(DEST_LIBRARY_FOLDER, ""));
				updaterFile.setLocalLocation(path);

				if (!needsUpdating.contains(updaterFile)) {
					needsUpdating.add(updaterFile);
				}
			}
		}

		if (ConfigValues.AUTO_UPDATE.getBool()) {
			if (latestPluginVersion > LWCInfo.VERSION) {
				logger.log("Update detected for LWC");
				logger.log("Latest version: " + latestPluginVersion);
			}
		}
	}

	/**
	 * Check to see if the distribution is outdated
	 * 
	 * @return
	 */
	public boolean checkDist() {
		check();

		if (latestPluginVersion > LWCInfo.VERSION) {
			UpdaterFile updaterFile = new UpdaterFile(UPDATE_SITE + DIST_FILE);
			updaterFile.setLocalLocation("plugins/LWC.jar");

			needsUpdating.add(updaterFile);

			try {
				update();
				logger.log("Updated successful");
				return true;
			} catch (Exception e) {
				logger.log("Update failed: " + e.getMessage());
				e.printStackTrace();
			}
		}

		return false;
	}

	/**
	 * @return the current sqlite version
	 */
	public double getCurrentSQLiteVersion() {
		return Double.parseDouble(config.get("sqlite"));
	}

	/**
	 * @return the full path to the native library for sqlite
	 */
	public String getFullNativeLibraryPath() {
		return getOSSpecificFolder() + getOSSpecificFileName();
	}

	/**
	 * @return the latest internal version
	 */
	public double getLatestInternalVersion() {
		return latestInternalVersion;
	}

	/**
	 * @return the latest plugin version
	 */
	public double getLatestPluginVersion() {
		return latestPluginVersion;
	}

	/**
	 * @return the os/arch specific file name for sqlite's native library
	 */
	public String getOSSpecificFileName() {
		String osname = System.getProperty("os.name").toLowerCase();

		if (osname.contains("windows")) {
			return "sqlitejdbc.dll";
		} else if (osname.contains("mac")) {
			return "libsqlitejdbc.jnilib";
		} else { /* We assume linux/unix */
			return "libsqlitejdbc.so";
		}
	}

	/**
	 * @return the os/arch specific folder location for SQLite's native library
	 */
	public String getOSSpecificFolder() {
		String osname = System.getProperty("os.name").toLowerCase();
		String arch = System.getProperty("os.arch").toLowerCase();

		if (osname.contains("windows")) {
			return DEST_LIBRARY_FOLDER + "lib/native/Windows/" + arch + "/";
		} else if (osname.contains("mac")) {
			return DEST_LIBRARY_FOLDER + "lib/native/Mac/" + arch + "/";
		} else { /* We assume linux/unix */
			return DEST_LIBRARY_FOLDER + "lib/native/Linux/" + arch + "/";
		}
	}

	/**
	 * Load the latest versions
	 * 
	 * @param background
	 *            if true, will be run in the background
	 */
	public void loadVersions(boolean background) {
		class Background_Check_Thread implements Runnable {
			@Override
			public void run() {
				try {
					URL url = new URL(UPDATE_SITE + VERSION_FILE);

					InputStream inputStream = url.openStream();
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

					// load up them versions!
					// expected: PLUGINVERSION\nINTERNALVERSION
					latestPluginVersion = Double.parseDouble(bufferedReader.readLine());
					latestInternalVersion = Double.parseDouble(bufferedReader.readLine());

					bufferedReader.close();
				} catch (Exception e) {
				}

				try {
					if (ConfigValues.AUTO_UPDATE.getBool()) {
						checkDist();
					} else {
						check();
					}

					update();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		Background_Check_Thread worker = new Background_Check_Thread();

		if (background) {
			new Thread(worker).start();
		} else {
			worker.run();
		}
	}

	/**
	 * Create the internal updater config file
	 */
	public void saveInternal() {
		try {
			File file = getInternalFile();

			if (file.exists()) {
				file.delete();
			}

			file.createNewFile();

			BufferedWriter writer = new BufferedWriter(new FileWriter(file));

			writer.write("# LWC Internal Config\n");
			writer.write("###############################\n");
			writer.write("### DO NOT MODIFY THIS FILE ###\n");
			writer.write("### THIS DOES NOT CHANGE    ###\n");
			writer.write("### LWC'S VISIBLE BEHAVIOUR ###\n");
			writer.write("###############################\n\n");
			writer.write("###############################\n");
			writer.write("###        THANK YOU!       ###\n");
			writer.write("###############################\n\n");

			for (String key : config.keySet()) {
				String value = config.get(key);

				writer.write(key + ":" + value + "\n");
			}

			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Ensure we have all of the required files (if not, download them)
	 */
	public void update() throws Exception {
		if (needsUpdating.size() == 0) {
			return;
		}

		/*
		 * Make the folder hierarchy if needed
		 */
		File folder = new File(getOSSpecificFolder());
		folder.mkdirs();
		folder = new File(DEST_LIBRARY_FOLDER + "lib/");
		folder.mkdirs();

		logger.log("Need to download " + needsUpdating.size() + " file(s)");

		Iterator<UpdaterFile> iterator = needsUpdating.iterator();

		while (iterator.hasNext()) {
			UpdaterFile item = iterator.next();

			String fileName = item.getRemoteLocation();
			fileName = fileName.substring(fileName.lastIndexOf('/') + 1);

			logger.log(" - Downloading file: " + fileName);

			URL url = new URL(item.getRemoteLocation());
			File file = new File(item.getLocalLocation());

			if (file.exists()) {
				file.delete();
			}

			InputStream inputStream = url.openStream();
			OutputStream outputStream = new FileOutputStream(file);

			saveTo(inputStream, outputStream);

			inputStream.close();
			outputStream.close();

			logger.log("  + Download complete");
			iterator.remove();
		}

		/*
		 * In the event we updated binaries, we should force an ini save!
		 */
		saveInternal();
	}

	/**
	 * Check the internal LWC version
	 */
	private void checkInternal() {
		if (latestInternalVersion > getCurrentSQLiteVersion()) {
			requireBinaryUpdate();
			logger.log("Binary update required");
			config.put("sqlite", latestInternalVersion + "");
		}
	}

	/**
	 * @return the internal config file
	 */
	private File getInternalFile() {
		return new File(DEST_LIBRARY_FOLDER + "internal.ini");
	}

	/**
	 * Parse the internal config file
	 */
	private void parseInternalConfig() {
		try {
			File file = getInternalFile();

			if (!file.exists()) {
				saveInternal();
				return;
			}

			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;

			while ((line = reader.readLine()) != null) {
				if (line.trim().startsWith("#")) {
					continue;
				}

				if (!line.contains(":")) {
					continue;
				}

				/*
				 * Split the array
				 */
				String[] arr = line.split(":");

				if (arr.length < 2) {
					continue;
				}

				/*
				 * Get the key/value
				 */
				String key = arr[0];
				String value = StringUtils.join(arr, 1, ":");
				value = value.substring(0, value.length() - 1);

				/*
				 * Set the config value
				 */
				config.put(key, value);
			}

			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Force update of binaries
	 */
	private void requireBinaryUpdate() {
		String[] paths = new String[] { DEST_LIBRARY_FOLDER + "lib/" + Database.DefaultType.getDriver(), getFullNativeLibraryPath() };

		for (String path : paths) {
			UpdaterFile updaterFile = new UpdaterFile(UPDATE_SITE + "shared/" + path.replaceAll(DEST_LIBRARY_FOLDER, ""));
			updaterFile.setLocalLocation(path);

			if (!needsUpdating.contains(updaterFile)) {
				needsUpdating.add(updaterFile);
			}
		}
	}

	/**
	 * Write an input stream to an output stream
	 * 
	 * @param inputStream
	 * @param outputStream
	 */
	private void saveTo(InputStream inputStream, OutputStream outputStream) throws IOException {
		byte[] buffer = new byte[1024];
		int len = 0;

		while ((len = inputStream.read(buffer)) > 0) {
			outputStream.write(buffer, 0, len);
		}
	}

}
