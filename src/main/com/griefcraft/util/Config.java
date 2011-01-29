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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.griefcraft.logging.Logger;
import com.griefcraft.lwc.LWCInfo;

public class Config extends Properties {

	/**
	 * Load the logger instance
	 */
	private Logger logger = Logger.getLogger(getClass().getSimpleName());

	/**
	 * Only keep one instance of Config
	 */
	private static Config instance;

	/**
	 * Create default config values. DEPENDS on enums in ConfigValues.class
	 */
	{
		for (ConfigValues value : ConfigValues.values()) {
			setProperty(value.getName(), value.getDefaultValue());
		}
	}

	/**
	 * Initialized via Config.init()
	 */
	private Config(String path) {
		try {
			File conf = new File(path);

			if (!conf.exists()) {
				save();
				return;
			}

			InputStream inputStream = new FileInputStream(conf);
			load(inputStream);
			inputStream.close();

			logger.info("Loaded " + size() + " config entries");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Destroy the config instance
	 */
	public static void destroy() {
		instance = null;
	}

	/**
	 * @return the instance of Config
	 */
	public static Config getInstance() {
		return instance;
	}

	/**
	 * Get a config instance of a specific file
	 * 
	 * @param path
	 * @return
	 */
	public static Config getInstance(String path) {
		return new Config(path);
	}

	/**
	 * Init the config class
	 */
	public static void init() {
		if (instance == null) {
			instance = new Config(LWCInfo.CONF_FILE);
		}
	}

	public void save() {
		try {
			File file = new File(LWCInfo.CONF_FILE);

			if (!file.exists()) {
				file.createNewFile();
			}

			OutputStream outputStream = new FileOutputStream(file);

			store(outputStream, "# LWC configuration file\n\n# + Github project page: https://github.com/Hidendra/LWC\n# + hMod thread link: http://forum.hey0.net/showthread.php?tid=837\n");
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
