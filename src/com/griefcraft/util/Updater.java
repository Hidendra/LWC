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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import com.griefcraft.LWCInfo;
import com.griefcraft.logging.Logger;

import static com.griefcraft.util.ConfigValues.AUTO_UPDATE;

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
	 * File used to obtain the latest version
	 */
	private final static String VERSION_FILE = "VERSION";
	
	/**
	 * File used for the distribution
	 */
	private final static String DIST_FILE = "dist/LWC.jar";

	/**
	 * List of files to download
	 */
	private List<UpdaterFile> needsUpdating = new ArrayList<UpdaterFile>();
	
	public Updater() {
		enableSSL();
	}

	/**
	 * Check for dependencies
	 * 
	 * @return true if LWC should be reloaded
	 */
	public void check() {
		String[] paths = new String[] { "lib/sqlite.jar", "lib/" + getOSSpecificFileName() };

		for (String path : paths) {
			File file = new File(path);

			if (file != null && !file.exists() && !file.isDirectory()) {
				UpdaterFile updaterFile = new UpdaterFile(UPDATE_SITE + path);
				updaterFile.setLocalLocation(path);
				
				needsUpdating.add(updaterFile);
			}
		}
		
		double latestVersion = getLatestVersion();
		
		if(latestVersion > LWCInfo.VERSION) {
			logger.info("Update detected for LWC");
			logger.info("Latest version: " + latestVersion);
		}
	}
	
	/**
	 * Check to see if the distribution is outdated
	 * 
	 * @return
	 */
	public boolean checkDist() {

		double latestVersion = getLatestVersion();
		
		if(latestVersion > LWCInfo.VERSION) {
			UpdaterFile updaterFile = new UpdaterFile(UPDATE_SITE + DIST_FILE);
			updaterFile.setLocalLocation("plugins/LWC.jar");

			needsUpdating.add(updaterFile);

			try {
				update();
				logger.info("Updated successful");
				return true;
			} catch(Exception e) {
				logger.info("Update failed: " + e.getMessage());
				e.printStackTrace();
			}
		}

		return false;
	}

	/**
	 * Get the latest version
	 * 
	 * @return
	 */
	public double getLatestVersion() {
		try {
			URL url = new URL(UPDATE_SITE + VERSION_FILE);
			
			InputStream inputStream = url.openStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			
			double version = Double.parseDouble(bufferedReader.readLine());
			
			bufferedReader.close();
			
			return version;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return 0.00;
	}
	
	/**
	 * Enable SSL. github is 100% ssl
	 */
	private void enableSSL() {
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
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

		logger.info("Need to download " + needsUpdating.size() + " object(s)");

		for (UpdaterFile item : needsUpdating) {
			logger.info(" - Downloading file : " + item.getRemoteLocation());

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

			logger.info("  + Download complete");
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
