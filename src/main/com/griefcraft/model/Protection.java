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

package com.griefcraft.model;

public class Protection {

	/**
	 * Unique id (in sql)
	 */
	private int id;

	/**
	 * The block id
	 */
	private int blockId;

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
	private String data;

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

	/**
	 * @return id:owner->[x,y,z]
	 */
	@Override
	public String toString() {
		return String.format("%d:%s->[%d,%d,%d]", id, owner, x, y, z);
	}

	public String getDate() {
		return date;
	}

	public int getId() {
		return id;
	}

	public int getBlockId() {
		return blockId;
	}

	public String getOwner() {
		return owner;
	}

	public String getData() {
		return data;
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

	public void setId(int id) {
		this.id = id;
	}

	public void setBlockId(int blockId) {
		this.blockId = blockId;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void setData(String data) {
		this.data = data;
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
