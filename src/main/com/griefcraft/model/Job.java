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

public class Job {

	/**
	 * Job type constants
	 */
	public static final int REMOVE_BLOCK = 1;
	public static final int SEND_MESSAGE = 2;

	private int id;
	private int type;
	private String owner;
	private String payload;
	private long timestamp;

	public int getId() {
		return id;
	}

	public String getOwner() {
		return owner;
	}

	public String getPayload() {
		return payload;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getType() {
		return type;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setType(int type) {
		this.type = type;
	}

}
