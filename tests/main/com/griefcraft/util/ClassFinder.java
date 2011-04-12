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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.griefcraft.BukkitPlugin;

public class ClassFinder {

	/**
	 * Get the classes for a specific package name
	 * 
	 * @param packageName
	 * @return
	 * @throws IOException
	 */
	public static Class<?>[] getClasses(String packageName) {
		try {
			String packageName2 = packageName.replaceAll("\\.", "/") + "/";
		    List<Class<?>> classes = new ArrayList<Class<?>>();
		    URL jarUrl = BukkitPlugin.class.getProtectionDomain().getCodeSource().getLocation();
		    
		    JarFile jarFile = new JarFile(new File(jarUrl.getPath()));
		    Enumeration<JarEntry> entries = jarFile.entries();
		    
		    while(entries.hasMoreElements()) {
		    	JarEntry entry = entries.nextElement();
		    	String entryName = entry.getName();
		    	
		    	if(entryName.contains(packageName2) && entryName.endsWith(".class")) {
		    		String className = entryName.replace(packageName2, "");
		    		className = className.substring(0, className.length() - ".class".length());
		    		className = packageName + "." + className;
		    		
		    		Class<?> clazz = Class.forName(className);
		    		classes.add(clazz);
		    	}
		    }
		    
		    jarFile.close();
		    
		    return classes.toArray(new Class[classes.size()]);
	    
		} catch(IOException e) {
			e.printStackTrace();
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}

		return new Class<?>[0];
	}
	
}
