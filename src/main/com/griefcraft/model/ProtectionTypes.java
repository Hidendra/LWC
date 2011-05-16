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

public class ProtectionTypes {

	/**
	 * Creatable by anyone
	 */
	public static final int PUBLIC = 0;
	public static final int PASSWORD = 1;
	public static final int PRIVATE = 2;

	/**
	 * Only creatable by lwc admins
	 */
	public static final int TRAP_KICK = 3;
	public static final int TRAP_BAN = 4;
	
	/**
	 * Status protections (for signs)
	 */
	public static final int STATUS = 5;

}
