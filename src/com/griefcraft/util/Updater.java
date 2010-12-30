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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import com.griefcraft.logging.Logger;

public class Updater {

	/**
	 * The logging object for this class
	 */
	private Logger logger = Logger.getLogger(getClass().getSimpleName());

	/**
	 * URL to the base update site
	 */
	private final static String UPDATE_SITE = "https://github.com/Hidendra/LWC/raw/master/";

	/**
	 * List of files to download
	 */
	private List<String> needsUpdating = new ArrayList<String>();

	/**
	 * Check the folders for files that need to be downloaded/updated
	 */
	public void check() {
		String[] paths = new String[] { "lib/sqlite.jar", "lib/" + getOSSpecificFileName() };

		for (String path : paths) {
			File file = new File(path);

			if (file != null && !file.exists() && !file.isDirectory()) {
				needsUpdating.add(path);
			}
		}

	}

	/**
	 * Get the OS specific sqlite file name (arch specific, too, for linux)
	 * 
	 * @return
	 */
	public String getOSSpecificFileName() {
		String osname = System.getProperty("os.name").toLowerCase();
		String arch = System.getProperty("os.arch");

		if (osname.contains("windows")) {
			osname = "win";
			arch = "x86";
		} else if (osname.contains("mac")) {
			osname = "mac";
			arch = "universal";
		} else if (osname.contains("nix")) {
			osname = "linux";
		} else if (osname.equals("sunos")) {
			osname = "linux";
		}

		if (arch.startsWith("i") && arch.endsWith("86")) {
			arch = "x86";
		}

		return osname + "-" + arch + ".lib";
	}

	/**
	 * Ensure we have all of the required files (if not, download them)
	 */
	public void update() throws Exception {
		if (needsUpdating.size() == 0) {
			return;
		}

		File folder = new File("lib");

		if (folder.exists() && !folder.isDirectory()) {
			throw new Exception("Folder \"lib\" cannot be created ! It is a file!");
		} else if (!folder.exists()) {
			logger.info("Creating folder : lib");
			folder.mkdir();
		}

		/*
		 * Enable SSL (github is 100% SSL)
		 */
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");

		logger.info("Need to download " + needsUpdating.size() + " object(s)");

		for (String item : needsUpdating) {
			logger.info(" - Downloading file : " + item);

			URL url = new URL(UPDATE_SITE + item);
			File file = new File(item);

			if (file.exists()) {
				file.delete();
			}

			InputStream inputStream = url.openStream();
			OutputStream outputStream = new FileOutputStream(file);

			byte[] buffer = new byte[1024];
			int len = 0;

			while ((len = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, len);
			}

			inputStream.close();
			outputStream.close();

			logger.info("  + Download complete");
		}
	}

}
