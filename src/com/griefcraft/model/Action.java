package com.griefcraft.model;

public class Action {

	public int id;
	private String action;
	private String player;
	private int chestID;
	private String data;

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @return the chestID
	 */
	public int getChestID() {
		return chestID;
	}

	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}

	/**
	 * @return the id
	 */
	public int getID() {
		return id;
	}

	/**
	 * @return the player
	 */
	public String getPlayer() {
		return player;
	}

	/**
	 * @param action
	 *            the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @param chestID
	 *            the chestID to set
	 */
	public void setChestID(int chestID) {
		this.chestID = chestID;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(String data) {
		this.data = data;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setID(int id) {
		this.id = id;
	}

	/**
	 * @param player
	 *            the player to set
	 */
	public void setPlayer(String player) {
		this.player = player;
	}

}
