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

package com.griefcraft.scripting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.griefcraft.logging.Logger;
import com.griefcraft.lwc.LWC;
import com.griefcraft.util.ConfigValues;
import com.griefcraft.util.Updater;
import com.griefcraft.util.config.ConfigurationNode;

public class ModuleEngine {

	private static Logger logger = Logger.getLogger("Module");

	/**
	 * The update site for packages
	 */
	public final static String UPDATE_SITE = "http://update.griefcraft.com/bukkit/lwc/modules/";
	
	/**
	 * The file listing the packages on the update site
	 */
	public final static String MODULES_LIST = "modules.yml";
	
	/**
	 * Map of the services
	 */
	private Map<String, PackageData> packages = new HashMap<String, PackageData>();
	
	public void init() {
		List<File> moduleJars = findPackageJars();
		
		// load the modules
		for(File file : moduleJars) {
			loadPackage(file);
		}
		
		// load the rest of service data and their latest versions
		loadModuleUpdates();
		
		// compare latest versions v versions
		checkUpdates();
	}
	
	/**
	 * @return the map of packages
	 */
	public Map<String, PackageData> getPackages() {
		return packages;
	}
	
	/**
	 * @return a List of the loaded packages
	 */
	public List<PackageData> getLoadedPackages() {
		List<PackageData> pkg = new ArrayList<PackageData>();
		
		for(PackageData pack : packages.values()) {
			if(pack != null) {
				pkg.add(pack);
			}
		}
		
		return pkg;
	}
	
	/**
	 * Install a package
	 * 
	 * @param packageData
	 * @return
	 */
	public boolean installPackage(PackageData packageData) {
		if(packageData.getPackage() != null) {
			return false;
		}
		
		downloadPackage(packageData);
		loadPackage(packageData);
		return true;
	}
	
	/**
	 * Uninstall a package
	 * 
	 * @param packageData
	 */
	public boolean removePackage(PackageData packageData) {
		if(packageData.getPackage() == null) {
			return false;
		}
		
		Package pack = packageData.getPackage();
		LWC.getInstance().getModuleLoader().removeModules(pack);
		packageData.setPackage(null);
		
		// delete the package file
		File file = getFile(packageData);
		
		if(!file.exists()) {
			return false;
		}
		
		return file.delete();
	}
	
	/**
	 * Check for updates and act on them if we're allowed to
	 */
	public void checkUpdates() {
		for(PackageData packageData : packages.values()) {
			Package pack = packageData.getPackage();
			
			// if it's required and not installed, we need to install it!
			// we return afterwards to verify it installed
			if(packageData.isRequired() && pack == null) {
				downloadPackage(packageData);
				loadPackage(packageData);
				checkUpdates();
				return;
			}
			
			if(pack == null) {
				continue;
			}
			
			double latestVersion = packageData.getLatestVersion();
			double version = packageData.getVersion();
			
			if(latestVersion > version) {
				logger.log("Update found for " + pack.getName().toUpperCase() + ". Latest version is: " + latestVersion);
				
				if(ConfigValues.AUTO_UPDATE.getBool()) {
					LWC.getInstance().getModuleLoader().removeModules(pack);
					downloadPackage(packageData);
					loadPackage(packageData);
				}
			}
			
		}
	}
	
	/**
	 * Reload a module
	 * 
	 * @param packageData
	 */
	public void loadPackage(PackageData packageData) {
		packageData.setPackage(null);
		
		File file = getFile(packageData);
		loadPackage(file);
	}
	
	/**
	 * Get the file object for a package
	 * 
	 * @param packageData
	 * @return
	 */
	private File getFile(PackageData packageData) {
		return new File(ModuleLoader.MODULE_PATH + packageData.getFileName());
	}
	
	/**
	 * Download a service
	 * 
	 * @param packageData
	 * @return
	 */
	public boolean downloadPackage(PackageData packageData) {
		if(packageData == null) {
			return false;
		}
		
		logger.log("Downloading module: " + packageData.getName() + " v" + packageData.getLatestVersion());
		
		String version = String.format("%.2f", packageData.getLatestVersion());
		try {
			File dest = new File(ModuleLoader.MODULE_PATH + packageData.getFileName());
			File backup = new File(ModuleLoader.MODULE_PATH + packageData.getFileName() + ".bak");
			URL url = new URL(UPDATE_SITE + packageData.getName() + "/" + version + "/" + packageData.getFileName());
			
			// rename it
			dest.renameTo(backup);
			
			// now get the streams to start downloading
			InputStream inputStream = url.openStream();
			OutputStream outputStream = new FileOutputStream(dest);
			
			// transfer now
			Updater.saveTo(inputStream, outputStream);
			
			// cleanup
			inputStream.close();
			outputStream.close();
			backup.delete();
		} catch (MalformedURLException e) {
			throw new PackageException("Error while downloading module: " + packageData.getName(), e);
		} catch (IOException e) {
			throw new PackageException("Error while downloading module: " + packageData.getName(), e);
		}
		
		
		return true;
	}
	
	/**
	 * Load a module
	 * 
	 * @param file
	 */
	public void loadPackage(File file) {
		ModuleLoader moduleLoader = LWC.getInstance().getModuleLoader();
		
		try {
			URL module = new URL("jar:file:" + file.getAbsolutePath() + "!/");
			URLClassLoader classLoader = new URLClassLoader(new URL[] { module }, getClass().getClassLoader());
			
			// load the RegisterService class
			Class<?> serviceClass = classLoader.loadClass("com.griefcraft.modules.impl.RegisterService");
			
			// create the service object
			Package pack = (Package) serviceClass.newInstance();
			
			// ask the service to register the modules it owns
			pack.registerModules(moduleLoader);
			
			// generate the service data wrapper
			PackageData packageData = new PackageData();
			packageData.setName(pack.getName().toLowerCase());
			packageData.setPackage(pack);
			packageData.setVersion(pack.getVersion());
			
			packages.put(pack.getName().toLowerCase(), packageData);
			logger.log("Loaded: " + pack.getName().toLowerCase() + " v" + pack.getVersion());
		} catch (MalformedURLException e) {
			throw new ModuleException("Error loading module", e);
		} catch (ClassNotFoundException e) {
			throw new ModuleException("Error loading module", e);
		} catch (InstantiationException e) {
			throw new ModuleException("Error loading module", e);
		} catch (IllegalAccessException e) {
			throw new ModuleException("Error loading module", e);
		}
	}
	
	/**
	 * Get the service data for a service
	 * 
	 * @param packageName
	 * @return
	 */
	public PackageData getPackageData(String packageName) {
		return packages.get(packageName);
	}
	
	/**
	 * Load the module updates
	 */
	public void loadModuleUpdates() {
		System.out.println("Loading!");
		
		try {
			URL url = new URL(UPDATE_SITE + MODULES_LIST);
			InputStream inputStream = url.openStream();
			
			// create the yaml object
			Yaml yaml = new Yaml();
			
			// load from the stream
			Object load = yaml.load(inputStream);
			
			// if it's null, we have a problem :s
			if(load == null) {
				throw new PackageException("Module file does not exist or is not in Yaml format");
			}
			
			// we're good!
			@SuppressWarnings("unchecked")
			Map<String, Object> root = (Map<String, Object>) load;
			
			// create the configuration
			ConfigurationNode configuration = new ConfigurationNode(root);
			
			for(String packageName : root.keySet()) {
				packageName = packageName.toLowerCase();
				double version = configuration.getDouble(packageName + ".version", 0.00);
				String fileName = configuration.getString(packageName + ".file", packageName + ".jar");
				boolean required = configuration.getBoolean(packageName + ".required", false);
				boolean config = configuration.getBoolean(packageName + ".config", false);
				
				PackageData packageData = getPackageData(packageName);
				
				// it wasn't loaded at runtime
				if(packageData == null) {
					packageData = new PackageData();
				}
				
				// now set what we can
				packageData.setName(packageName);
				packageData.setLatestVersion(version);
				packageData.setFileName(fileName);
				packageData.setRequired(required);
				packageData.setConfig(config);
				
				// reupdate (or place) it into the list
				packages.put(packageName, packageData);
			}
		} catch (MalformedURLException e) {
			throw new PackageException("Error while loading latest version in service: " + getClass().getName(), e);
		} catch (IOException e) {
			throw new PackageException("Error while loading latest version in service: " + getClass().getName(), e);
		}
	}
	
	/**
	 * Find module jars in the default module path
	 * 
	 * @return
	 */
	public List<File> findPackageJars() {
		List<File> moduleJars = new ArrayList<File>();
		File folder = new File(ModuleLoader.MODULE_PATH);
		
		if(!folder.exists() || !folder.isDirectory()) {
			return moduleJars;
		}
		
		for(File file : folder.listFiles()) {
			if(file.getPath().endsWith(".jar")) {
				moduleJars.add(file);
			}
		}
		
		return moduleJars;
	}
	
}
