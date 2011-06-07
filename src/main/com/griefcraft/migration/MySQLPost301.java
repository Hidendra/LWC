package com.griefcraft.migration;

import com.griefcraft.lwc.LWC;
import com.griefcraft.sql.PhysDB;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLPost301 {

    /**
     * Check for a new table prefix, to auto rename the table :-)
     *
     * @param lwc
     */
    public static void checkDatabaseConversion(LWC lwc) {
        PhysDB physicalDatabase = lwc.getPhysicalDatabase();
        String prefix = lwc.getConfiguration().getString("database.prefix");

        if(prefix == null || prefix.length() == 0) {
            return;
        }

        // now to check the prefix...
        Connection connection = physicalDatabase.getConnection();

        // check for the table
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute("SELECT * FROM " + prefix + "protections");
        } catch(SQLException e) {
            // The table does not exist, let's go ahead and rename all of the tables
            physicalDatabase.renameTable("protections", prefix + "protections");
            physicalDatabase.renameTable("rights", prefix + "rights");
            physicalDatabase.renameTable("menu_styles", prefix + "menu_styles");
        } finally {
            if(statement != null) {
                try {
                    statement.close();
                } catch(SQLException e) { }
            }
        }

    }

}
