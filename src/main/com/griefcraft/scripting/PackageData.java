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

public class PackageData {

	/**
	 * The package object
	 */
	private Package packageObject;
	
	/**
	 * The name of the service
	 */
	private String name;
	
	/**
	 * The service's current version
	 */
	private double version;
	
	/**
	 * The service's latest version
	 */
	private double latestVersion;
	
	/**
	 * The name of the file for the service
	 */
	private String fileName;
	
	/**
	 * True if the module is required by LWC
	 */
	private boolean required;

	public Package getPackage() {
		return packageObject;
	}
	
	public String getName() {
		return name;
	}

	public double getVersion() {
		return version;
	}

	public double getLatestVersion() {
		return latestVersion;
	}

	public String getFileName() {
		return fileName;
	}

	public boolean isRequired() {
		return required;
	}

	public void setPackage(Package packageObject) {
		this.packageObject = packageObject;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setVersion(double version) {
		this.version = version;
	}

	public void setLatestVersion(double latestVersion) {
		this.latestVersion = latestVersion;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}
	
}
