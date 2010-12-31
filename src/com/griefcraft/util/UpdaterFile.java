package com.griefcraft.util;

public class UpdaterFile {

	/**
	 * The remote url location
	 */
	private String remoteLocation;
	
	/**
	 * The local url location
	 */
	private String localLocation;
	
	public UpdaterFile(String location) {
		this.remoteLocation = location;
		this.localLocation = location;
	}
	
	/**
	 * @return the remote url location
	 */
	public String getRemoteLocation() {
		return remoteLocation;
	}
	
	/**
	 * @return the local file location
	 */
	public String getLocalLocation() {
		return localLocation;
	}
	
	/**
	 * Set the remote url location
	 * 
	 * @param remoteLocation
	 */
	public void setRemoteLocation(String remoteLocation) {
		this.remoteLocation = remoteLocation;
	}
	
	/**
	 * Set the local file location
	 * 
	 * @param localLocation
	 */
	public void setLocalLocation(String localLocation) {
		this.localLocation = localLocation;
	}
	
}
