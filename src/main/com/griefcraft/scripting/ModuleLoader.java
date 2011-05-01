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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.griefcraft.logging.Logger;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.Module.Result;

public class ModuleLoader {

	public enum Event {
		/**
		 * Not used with dispatchEvent
		 */
		LOAD(0),

		/**
		 * Called when a module is unloaded
		 */
		UNLOAD(0),

		/**
		 * Called when a console or player issues a command
		 */
		COMMAND(3),

		/**
		 * Called when redstone is passed to a protection
		 */
		REDSTONE(3),

		/**
		 * Called when a protection is destroyed
		 */
		DESTROY_PROTECTION(5),

		/**
		 * Called when a valid protection is left clicked
		 */
		INTERACT_PROTECTION(5),

		/**
		 * Called when a block is left clicked
		 */
		INTERACT_BLOCK(3);

		Event(int arguments) {
			this.arguments = arguments;
		}

		public int getExpectedArguments() {
			return arguments;
		}

		/**
		 * Expected amount of arguments (not counting the lwc object!)
		 */
		int arguments;
	}

	private static Logger logger = Logger.getLogger("Loader");

	/**
	 * Path to the root of scripts
	 */
	public final static String ROOT_PATH = "plugins/LWC/";

	/**
	 * Path to where modules are located
	 */
	public final static String MODULE_PATH = "plugins/LWC/modules/";

	/**
	 * Map of loaded modules
	 */
	private Map<String, Map<String, MetaData>> packageModules = new HashMap<String, Map<String, MetaData>>();

	/**
	 * The engine instance
	 */
	private ModuleEngine engine;

	public ModuleLoader() {
		engine = new ModuleEngine();
	}
	
	/**
	 * @return the map of service:package modules
	 */
	public Map<String, Map<String, MetaData>> getPackageModules() {
		return packageModules;
	}

	/**
	 * Initialize the Module Loader
	 */
	public void init() {
		engine.init();
	}

	/**
	 * Dispatch an event
	 * 
	 * @param event
	 * @param args
	 */
	public Result dispatchEvent(Event event, Object... args) {
		if(event.getExpectedArguments() > args.length) {
			return Result.DEFAULT;
		}

		LWC lwc = LWC.getInstance();
		Result result = Result.DEFAULT;
		logger.log("dispatching " + event + " with arguments: " + args.length);

		for(Map<String, MetaData> modules : packageModules.values()) {
			for(MetaData metaData : modules.values()) {
				String moduleName = metaData.getName();
				Module module = metaData.getModule();
				Result temp = Result.DEFAULT;

				switch(event) {

				case COMMAND:
					temp = module.onCommand(lwc, (CommandSender) args[0], (String) args[1], (String[]) args[2]);
					break;

				case REDSTONE:
					temp = module.onRedstone(lwc, (Protection) args[0], (Block) args[1], (Integer) args[2]);
					break;

				case DESTROY_PROTECTION:
					temp = module.onDestroyProtection(lwc, (Player) args[0], (Protection) args[1], (Block) args[2], (Boolean) args[3], (Boolean) args[4]);
					break;

				case INTERACT_PROTECTION:
					temp = module.onProtectionInteract(lwc, (Player) args[0], (Protection) args[1], (List<String>) args[2], (Boolean) args[3], (Boolean) args[4]);
					break;

				case INTERACT_BLOCK:
					temp = module.onBlockInteract(lwc, (Player) args[0], (Block) args[1], (List<String>) args[2]);
					break;
				}

				if(temp != Result.DEFAULT) {
					result = temp;
				}
				
				if(result == Result.CANCEL) {
					return result;
				}
			}
		}

		return result;
	}
	
	/**
	 * Register a module for a service
	 * 
	 * @param service
	 * @param module
	 */
	public void registerModule(Package service, Module module) {
		Map<String, MetaData> modules = packageModules.get(service.getName());
		
		if(modules == null) {
			modules = new HashMap<String, MetaData>();
		}
		
		modules.put(module.getName(), service.createMetaData(module));
		packageModules.put(service.getName(), modules);
		logger.log("Registered: " + module.getName() + " to " + service.getName());
	}
	
	/**
	 * Register a module not assigned to a service
	 * 
	 * @param module
	 */
	public void registerCustomModule(Module module) {
		registerModule(null, module);
	}

	/**
	 * Remove the modules for a service
	 * 
	 * @param service
	 */
	public void removeModules(Package service) {
		packageModules.remove(service);
	}
	
	/**
	 * Get the module engine
	 * 
	 * @return
	 */
	public ModuleEngine getModuleEngine() {
		return engine;
	}
	
	/**
	 * Get the meta data for a module
	 * 
	 * @param name
	 * @return
	 */
	public MetaData getMetaData(String name) {
		for(Map<String, MetaData> modules : packageModules.values()) {
			if(modules.containsKey(name)) {
				return modules.get(name);
			}
		}

		return null;
	}

	/**
	 * Get a module
	 * 
	 * @param name
	 * @return
	 */
	public Module getModule(String name) {
		for(Map<String, MetaData> modules : packageModules.values()) {
			if(modules.containsKey(name)) {
				return modules.get(name).getModule();
			}
		}

		return null;
	}

}
