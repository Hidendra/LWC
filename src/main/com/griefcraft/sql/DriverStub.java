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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

class DriverStub implements Driver {
	private Driver driver;

	protected DriverStub(Driver d) {
		driver = d;
	}

	@Override
	public boolean acceptsURL(String u) throws SQLException {
		return driver.acceptsURL(u);
	}

	@Override
	public Connection connect(String u, Properties p) throws SQLException {
		return driver.connect(u, p);
	}

	@Override
	public int getMajorVersion() {
		return driver.getMajorVersion();
	}

	@Override
	public int getMinorVersion() {
		return driver.getMinorVersion();
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
		return driver.getPropertyInfo(u, p);
	}

	@Override
	public boolean jdbcCompliant() {
		return driver.jdbcCompliant();
	}
}