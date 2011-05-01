/**
 * This file is part of LWC (https://github.com/Hidendraame/LWC)
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

package com.griefcraft.modules.modadmin;

import java.util.Map;

import org.bukkit.command.CommandSender;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.MetaData;
import com.griefcraft.scripting.Module;
import com.griefcraft.scripting.PackageData;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;

public class ModuleAdmin extends JavaModule {

	@Override
	public String getName() {
		return "com.griefcraft.modules.modadmin";
	}

	@Override
	public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
		if(!StringUtils.hasFlag(command, "module") && !StringUtils.hasFlag(command, "modules")) {
			return DEFAULT;
		}
		
		if(!lwc.isAdmin(sender)) {
			lwc.sendLocale(sender, "protection.accessdenied");
			return CANCEL;
		}
		
		if(args.length == 0) {
			lwc.sendLocale(sender, "help.modules");
			return CANCEL;
		}
		
		String subCommand = args[0].toLowerCase();
		
		if(subCommand.equals("list")) {
			lwc.sendLocale(sender, "protection.modadmin.list");
			
			for(PackageData pack : lwc.getModuleLoader().getModuleEngine().getLoadedPackages()) {
				String version = String.format("%.2f", pack.getVersion());
				String latestVersion = String.format("%.2f", pack.getLatestVersion());
				
				String versionColor = Colors.Green;
				
				if(pack.getLatestVersion() > pack.getVersion()) {
					versionColor = Colors.Red;
				}
				
				lwc.sendLocale(sender, "protection.modadmin.list.iter", "name", pack.getName(), "curr_color", versionColor, "version", version, "latest", latestVersion);
			}
		}
		else if(subCommand.equals("tree")) {
			lwc.sendLocale(sender, "protection.modadmin.tree");
			
			for(Map.Entry<String, Map<String, MetaData>> entry : lwc.getModuleLoader().getPackageModules().entrySet()) {
				String packageName = entry.getKey();
				Map<String, MetaData> packages = entry.getValue();
				PackageData packageData = null;
				
				if(packageName == null) {
					packageName = "null";
				} else {
					packageData = lwc.getModuleLoader().getModuleEngine().getPackageData(packageName);
				}
				
				// header
				lwc.sendLocale(sender, "protection.modadmin.tree.header", "name", packageName, "status", lwc.getLocale(packageData == null ? "no" : "yes").toUpperCase());
				
				// send the modules in the package now
				for(MetaData metaData : packages.values()) {
					Module module = metaData.getModule();
					
					if(module == null) {
						continue;
					}
					
					lwc.sendLocale(sender, "protection.modadmin.tree.iter", "name", module.getClass().getName());
				}
			}
		}
		else if(subCommand.equals("install")) {
			if(args.length < 2) {
				lwc.sendSimpleUsage(sender, "/lwc modules install <name>");
				return CANCEL;
			}
			
			String packageName = StringUtils.join(args, 1);
			PackageData packageData = lwc.getModuleLoader().getModuleEngine().getPackageData(packageName);
			
			if(packageData == null) {
				lwc.sendLocale(sender, "protection.modadmin.noexist", "name", packageName);
				return CANCEL;
			}
			
			if(packageData.getPackage() != null) {
				lwc.sendLocale(sender, "protection.modadmin.alreadyinstalled", "name", packageName);
				return CANCEL;
			}
			
			boolean result = lwc.getModuleLoader().getModuleEngine().installPackage(packageData);
			
			if(result) {
				lwc.sendLocale(sender, "protection.modadmin.install.success", "name", packageName);
			} else {
				lwc.sendLocale(sender, "protection.modadmin.install.error", "name", packageName);
			}
		}
		else if(subCommand.equals("remove")) {
			if(args.length < 2) {
				lwc.sendSimpleUsage(sender, "/lwc modules remove <name>");
				return CANCEL;
			}
			
			String packageName = StringUtils.join(args, 1);
			PackageData packageData = lwc.getModuleLoader().getModuleEngine().getPackageData(packageName);
			
			if(packageData == null) {
				lwc.sendLocale(sender, "protection.modadmin.noexist", "name", packageName);
				return CANCEL;
			}
			
			if(packageData.getPackage() == null) {
				lwc.sendLocale(sender, "protection.modadmin.notinstalled", "name", packageName);
				return CANCEL;
			}
			
			if(packageData.isRequired()) {
				lwc.sendLocale(sender, "protection.modadmin.required", "name", packageName);
				return CANCEL;
			}
			
			boolean result = lwc.getModuleLoader().getModuleEngine().removePackage(packageData);
			
			if(result) {
				lwc.sendLocale(sender, "protection.modadmin.remove.success", "name", packageName);
			} else {
				lwc.sendLocale(sender, "protection.modadmin.remove.error", "name", packageName);
			}
		}
		
		return CANCEL;
	}
	
}
