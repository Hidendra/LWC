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

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.griefcraft.logging.Logger;
import com.griefcraft.lwc.LWCInfo;

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
	
	/**
	 * Internal config
	 */
	private HashMap<String, String> config = new HashMap<String, String>();

	public Updater() {
		enableSSL();
		
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
		String[] paths = new String[] { "lib/sqlite.jar", getFullNativeLibraryPath() };

		for (String path : paths) {
			File file = new File(path);

			if (file != null && !file.exists() && !file.isDirectory()) {
				UpdaterFile updaterFile = new UpdaterFile(UPDATE_SITE + path);
				updaterFile.setLocalLocation(path);

				needsUpdating.add(updaterFile);
			}
		}

		double latestVersion = getLatestVersion();

		if (latestVersion > LWCInfo.VERSION) {
			logger.info("Update detected for LWC");
			logger.info("Latest version: " + latestVersion);
		}
	}
	
	/**
	 * Force update of binaries
	 */
	private void requireBinaryUpdate() {
		String[] paths = new String[] { "lib/sqlite.jar", getFullNativeLibraryPath() };

		for (String path : paths) {
			UpdaterFile updaterFile = new UpdaterFile(UPDATE_SITE + path);
			updaterFile.setLocalLocation(path);

			needsUpdating.add(updaterFile);
		}
	}

	/**
	 * Check to see if the distribution is outdated
	 * 
	 * @return
	 */
	public boolean checkDist() {

		double latestVersion = getLatestVersion();

		if (latestVersion > LWCInfo.VERSION) {
			UpdaterFile updaterFile = new UpdaterFile(UPDATE_SITE + DIST_FILE);
			updaterFile.setLocalLocation("plugins/LWC.jar");

			needsUpdating.add(updaterFile);

			try {
				update();
				logger.info("Updated successful");
				return true;
			} catch (Exception e) {
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
	 * @return the current internal sqlite version
	 */
	public double getCurrentInternalSQLiteVersion() {
		return Double.parseDouble(config.get("sqlite"));
	}
	
	/**
	 * @return the latest internal sqlite version
	 */
	public double getLatestInternalSQLiteVersion() {
		try {
			URL url = new URL(UPDATE_SITE + VERSION_FILE);

			InputStream inputStream = url.openStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

			bufferedReader.readLine();
			double version = Double.parseDouble(bufferedReader.readLine());

			bufferedReader.close();

			return version;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0.00;
	}
	
	/**
	 * @return the internal config file
	 */
	private File getInternalFile() {
		return new File("plugins/lwc/internal.ini");
	}
	
	/**
	 * Parse the internal config file
	 */
	private void parseInternalConfig() {
		try {
			File file = getInternalFile();
			
			if(!file.exists()) {
				saveInternal();
				return;
			}
			
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			
			while((line = reader.readLine()) != null) {
				if(line.trim().startsWith("#")) {
					continue;
				}
				
				if(!line.contains(":")) {
					continue;
				}
				
				/*
				 * Split the array
				 */
				String[] arr = line.split(":");
				
				if(arr.length < 2) {
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
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create the internal updater config file
	 */
	public void saveInternal() {
		try {
			File file = getInternalFile();
			
			if(file.exists()) {
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
			
			for(String key : config.keySet()) {
				String value = config.get(key);
				
				writer.write(key + ":" + value + "\n");
			}
			
			writer.flush();
			writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the full path to the native library for sqlite
	 */
	public String getFullNativeLibraryPath() {
		return getOSSpecificFolder() + getOSSpecificFileName();
	}
	
	/**
	 * @return the os/arch specific file name for sqlite's native library
	 */
	public String getOSSpecificFileName() {
		String osname = System.getProperty("os.name").toLowerCase();
		
		if(osname.contains("windows")) {
			return "sqlitejdbc.dll";
		} else if(osname.contains("mac")) {
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
		
		if(osname.contains("windows")) {
			return "lib/native/Windows/" + arch + "/";
		} else if(osname.contains("mac")) {
			return "lib/native/Mac/" + arch + "/";
		} else { /* We assume linux/unix */
			return "lib/native/Linux/" + arch + "/";
		}
	}

	/**
	 * Ensure we have all of the required files (if not, download them)
	 */
	public void update() throws Exception {
		/*
		 * Check internal versions
		 */
		double latestVersion = getLatestInternalSQLiteVersion();
		if(latestVersion > getCurrentInternalSQLiteVersion()) {
			requireBinaryUpdate();
			logger.info("Binary update required");
			config.put("sqlite", latestVersion + "");
		}

		if (needsUpdating.size() == 0) {
			return;
		}
		
		/*
		 * Make the native folder hierarchy if needed
		 */
		File folder = new File(getOSSpecificFolder());
		folder.mkdirs();

		logger.info("Need to download " + needsUpdating.size() + " file(s)");

		Iterator<UpdaterFile> iterator = needsUpdating.iterator();

		while (iterator.hasNext()) {
			UpdaterFile item = iterator.next();

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
			iterator.remove();
		}
		
		/*
		 * In the event we updated binaries, we should force an ini save!
		 */
		saveInternal();
	}

	/**
	 * Enable SSL. github is 100% ssl
	 */
	private void enableSSL() {
		/*
		 * This seems hackish.. 
		 * but seems more than a few people don't have their trust stores OR OpenJDK
		 * 
		 * This approach does not depend on com.sun !!
		 */
		TrustManager[] trustAllCerts = new TrustManager[]{
			    new X509TrustManager() {
			        @Override
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			            return null;
			        }
			        @Override
					public void checkClientTrusted(
			            java.security.cert.X509Certificate[] certs, String authType) {
			        }
			        @Override
					public void checkServerTrusted(
			            java.security.cert.X509Certificate[] certs, String authType) {
			        }
			    }
			};
		
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
		    sc.init(null, trustAllCerts, new java.security.SecureRandom());
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			logger.info("SEVERE ERROR :: SSL NOT SUPPORTED");
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
