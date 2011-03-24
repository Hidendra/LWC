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

public class AccessRight {

	private String entity;
	private int id;

	private int protectionId;

	private int rights;
	private int type;

	public static final int GROUP = 0;

	public static final int PLAYER = 1;

	/**
	 * Used in conjunction with /lwc -O
	 */
	public static final int RESULTS_PER_PAGE = 15;

	public String getEntity() {
		return entity;
	}

	public int getId() {
		return id;
	}

	public int getprotectionId() {
		return protectionId;
	}

	public int getRights() {
		return rights;
	}

	public int getType() {
		return type;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setProtectionId(int protectionId) {
		this.protectionId = protectionId;
	}

	public void setRights(int rights) {
		this.rights = rights;
	}

	public void setType(int type) {
		this.type = type;
	}

	public static String typeToString(int rights) {
		if (rights == GROUP) {
			return "Group";
		} else if (rights == PLAYER) {
			return "Player";
		}

		return "Unknown";
	}

}
