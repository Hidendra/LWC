package com.griefcraft.model;

public class Chest {

	/**
	 * Chest ID (in sql)
	 */
	private int id;

	/**
	 * The chest type
	 */
	private int type;

	/**
	 * The owner of the chest
	 */
	private String owner;

	/**
	 * The password for the chest
	 */
	private String password;

	/**
	 * The x coordinate
	 */
	private int x;

	/**
	 * The y coordinate
	 */
	private int y;

	/**
	 * The z coordinate
	 */
	private int z;

	/**
	 * The date created
	 */
	private String date;

	public String getDate() {
		return date;
	}

	public int getID() {
		return id;
	}

	public String getOwner() {
		return owner;
	}

	public String getPassword() {
		return password;
	}

	public int getType() {
		return type;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setID(int id) {
		this.id = id;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setZ(int z) {
		this.z = z;
	}

}
