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

package com.griefcraft.modules.create;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.AccessRight;
import com.griefcraft.model.Action;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.ModuleLoader.Event;
import com.griefcraft.sql.MemDB;
import com.griefcraft.sql.PhysDB;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;

public class CreateModule extends JavaModule {
	
	@Override
	public Result onProtectionInteract(LWC lwc, Player player, Protection protection, List<String> actions, boolean canAccess, boolean canAdmin) {
		if(!actions.contains("create")) {
			return DEFAULT;
		}
		
		lwc.sendLocale(player, "protection.interact.error.alreadyregistered", "block", LWC.materialToString(protection.getBlockId()));
		lwc.removeModes(player);
		return CANCEL;
	}
	
	@Override
	public Result onBlockInteract(LWC lwc, Player player, Block block, List<String> actions) {
        if(!actions.contains("create")) {
        	return DEFAULT;
        }
        
        if(!lwc.isProtectable(block)) {
        	return DEFAULT;
        }
        
        PhysDB physDb = lwc.getPhysicalDatabase();
        MemDB memDb = lwc.getMemoryDatabase();
        
        Action action = memDb.getAction("create", player.getName());
        String actionData = action.getData();
        String[] split = actionData.split(" ");
        String protectionType = split[0].toLowerCase();
        String protectionData = StringUtils.join(split, 1);
        
        // misc data we'll use later
        String playerName = player.getName();
        String worldName = block.getWorld().getName();
        int blockX = block.getX();
        int blockY = block.getY();
        int blockZ = block.getZ();
        
        lwc.removeModes(player);
        Result registerProtection = lwc.getModuleLoader().dispatchEvent(Event.REGISTER_PROTECTION, player, block);
        
        // another plugin cancelled the registration
        if(registerProtection == Result.CANCEL) {
        	return ALLOW;
        }
        
        if(protectionType.equals("public")) {
            physDb.registerProtection(block.getTypeId(), ProtectionTypes.PUBLIC, worldName, playerName, "", blockX, blockY, blockZ);
            lwc.sendLocale(player, "protection.interact.create.finalize");
        }
        else if(protectionType.equals("password")) {
            String password = lwc.encrypt(protectionData);
            
            physDb.registerProtection(block.getTypeId(), ProtectionTypes.PASSWORD, worldName, playerName, password, blockX, blockY, blockZ);
            
            // load the freshly created protection!
            Protection protection = physDb.loadProtection(worldName, blockX, blockY, blockZ);
            memDb.registerPlayer(playerName, protection.getId());
            lwc.sendLocale(player, "protection.interact.create.finalize");
            lwc.sendLocale(player, "protection.interact.create.password");
        }
        else if(protectionType.equals("private")) {
            String[] rights = protectionData.split(" ");
            
            physDb.registerProtection(block.getTypeId(), ProtectionTypes.PRIVATE, worldName, playerName, "", blockX, blockY, blockZ);
            lwc.sendLocale(player, "protection.interact.create.finalize");
            
            // load the protection that was created
            Protection protection = physDb.loadProtection(worldName, blockX, blockY, blockZ);
            
            for(String right : rights) {
                boolean admin = false;
                int type = AccessRight.PLAYER;
                
                if(right.isEmpty()) {
                	continue;
                }
                
				if(right.startsWith("@")) {
                    admin = true;
                    right = right.substring(1);
                }
                
                String lowered = right.toLowerCase();
                
                if(lowered.startsWith("g:")) {
                    type = AccessRight.GROUP;
                    right = right.substring(2);
                }
                
                if(lowered.startsWith("l:")) {
                    type = AccessRight.LIST;
                    right = right.substring(2);
                }
                
                if(lowered.startsWith("list:")) {
                    type = AccessRight.LIST;
                    right = right.substring(5);
                }
                   
                String localeChild = AccessRight.typeToString(type).toLowerCase();
                
                // register the rights
                physDb.registerProtectionRights(protection.getId(), right, admin ? 1 : 0, type);
                lwc.sendLocale(player, "protection.interact.rights.register." + localeChild, "name", right, "isadmin", (admin ? "[" + Colors.Red + "ADMIN" + Colors.Gold + "]" : ""));
                
                // remove the protection from the cache (we updated the rights)
                protection.removeCache();
            }
        }
        else if(protectionType.equals("trap")) {
        	String[] splitData = protectionData.split(" ");
            String type = splitData[0].toLowerCase();
            String reason = "";
            
            if(splitData.length > 1) {
            	reason = StringUtils.join(splitData, 1);
            }
            
            int tmpType = ProtectionTypes.TRAP_KICK;
            
            if(type.equals("ban")) {
            	tmpType = ProtectionTypes.TRAP_BAN;
            }
            
            physDb.registerProtection(block.getTypeId(), tmpType, worldName, playerName, reason, blockX, blockY, blockZ);
            lwc.sendLocale(player, "protection.interact.create.finalize");
        }
        else if(protectionType.equals("status")) {
        	if(block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) {
        		lwc.sendLocale(player, "protection.create.status.notsign");
        		return CANCEL;
        	}
        	
        	protectionData = protectionData.trim();
        	
        	try {
        		int protectionId = Integer.parseInt(protectionData);
        		
        		if(lwc.getPhysicalDatabase().loadProtection(protectionId) == null) {
        			throw new Exception();
        		}
        	} catch(Exception e) {
        		lwc.sendLocale(player, "protection.admin.view.noexist");
        		return CANCEL;
        	}
        	
            physDb.registerProtection(block.getTypeId(), ProtectionTypes.STATUS, worldName, playerName, protectionData, blockX, blockY, blockZ);
            lwc.sendLocale(player, "protection.interact.create.finalize");
        }
        
        // get the newly protected protection
        Protection protection = physDb.loadProtection(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        
        if(protection != null) {
        	lwc.getModuleLoader().dispatchEvent(Event.POST_REGISTRATION, protection);
        }
        
        return CANCEL;
	}
	
	@Override
	public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
        if(!StringUtils.hasFlag(command, "c") && !StringUtils.hasFlag(command, "create")) {
        	return DEFAULT;
        }
        
        if(!(sender instanceof Player)) {
            return DEFAULT;
        }
        
        if(args.length == 0) {
        	lwc.sendLocale(sender, "help.creation");
        	return DEFAULT;
        }
        
        Player player = (Player) sender;
        
        String full = StringUtils.join(args, 0);
        String type = args[0].toLowerCase();
        String data = StringUtils.join(args, 1);
        
        if(type.equals("trap")) {
            if(!lwc.isAdmin(player)) {
                lwc.sendLocale(player, "protection.accessdenied");
                return CANCEL;
            }
            
            if(args.length < 2) {
                lwc.sendSimpleUsage(player, "/lwc -c trap <kick/ban> [reason]");
                return CANCEL;
            }
        }
        else if(type.equals("password")) {
            if(args.length < 2) {
                lwc.sendSimpleUsage(player, "/lwc -c password <Password>");
                return CANCEL;
            }
            
            String hiddenPass = StringUtils.transform(data, '*');
            lwc.sendLocale(player, "protection.create.password", "password", hiddenPass);
        }
        else if(type.equals("status")) {
        	if(data.isEmpty()) {
        		lwc.sendSimpleUsage(player, "/lwc -c status <protectionId>");
        		return CANCEL;
        	}
        	
        	// verify the id
        	try {
        		int protectionId = Integer.parseInt(data);
        		
        		if(lwc.getPhysicalDatabase().loadProtection(protectionId) == null) {
        			throw new Exception();
        		}
        	} catch(Exception e) {
        		lwc.sendLocale(player, "protection.admin.view.noexist");
        		return CANCEL;
        	}
        }
        else if(!type.equals("public") && !type.equals("private")) {
            lwc.sendLocale(player, "help.creation");
            return CANCEL;
        }
        
        MemDB db = lwc.getMemoryDatabase();
        db.unregisterAllActions(player.getName());
        db.registerAction("create", player.getName(), full);
        
        lwc.sendLocale(player, "protection.create.finalize", "type", lwc.getLocale(type));
        return CANCEL;
	}

}
