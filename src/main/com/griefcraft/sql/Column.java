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

package com.griefcraft.sql;

/**
 * FIXME:
 */
public class Column {

	/**
	 * The table this column is assigned to
	 */
	private Table table;

	/**
	 * The column name
	 */
	private String name;

	/**
	 * The column type (INTEGER, BLOB, etc)
	 */
	private String type;

	/**
	 * If this column is the primary column
	 */
	private boolean primary = false;

	/**
	 * If the table should auto increment. Note: This is automatically set for SQLite with:
	 * <p>
	 * table.setPrimary(true);
	 * </p>
	 */
	private boolean autoIncrement = false;

	public Column(String name) {
		this.name = name;
	}

	public void setTable(Table table) {
		if (this.table == null) {
			this.table = table;
		}
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
		this.autoIncrement = primary;
	}

	public void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isPrimary() {
		return primary;
	}

	public boolean shouldAutoIncrement() {
		return autoIncrement;
	}
}
