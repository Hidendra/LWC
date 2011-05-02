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
	
	/**
	 * Access right is for a group
	 */
	public static final int GROUP = 0;
	
	/**
	 * Access right is for a player
	 */
	public static final int PLAYER = 1;
	
	/**
	 * Used in conjuction with HeroList
	 */
	public static final int LIST = 2;

	/**
	 * Used in conjunction with /lwc -O
	 */
	public static final int RESULTS_PER_PAGE = 15;

	private String name;
	private int id;

	private int protectionId;

	private int rights;
	private int type;
	
	@Override
	public String toString() {
		return String.format("AccessRight = %d { protection=%d name=%s rights=%d type=%s", id, protectionId, name, rights, typeToString(rights));
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public int getProtectionId() {
		return protectionId;
	}

	public int getRights() {
		return rights;
	}

	public int getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name;
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

	public static String typeToString(int type) {
		if (type == GROUP) {
			return "Group";
		} else if (type == PLAYER) {
			return "Player";
		} else if (type == LIST) {
			return "List";
		}

		return "Unknown";
	}

}
