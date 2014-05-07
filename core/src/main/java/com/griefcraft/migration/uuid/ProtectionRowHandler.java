package com.griefcraft.migration.uuid;

import com.griefcraft.lwc.LWC;
import com.griefcraft.migration.RowHandler;
import com.griefcraft.migration.TableWalker;
import com.griefcraft.model.PlayerInfo;
import com.griefcraft.model.Protection;
import com.griefcraft.sql.Column;
import com.griefcraft.sql.Database;
import com.griefcraft.sql.PhysDB;
import com.griefcraft.sql.Table;
import com.griefcraft.util.PlayerRegistry;
import org.json.simple.parser.JSONParser;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

public class ProtectionRowHandler implements RowHandler {

    public void onStart() {
        PhysDB database = LWC.getInstance().getPhysicalDatabase();

        if (database.getInternal("uuidConversionProtectionsTableCreated") == null) {
            database.log("Renaming old protections table");

            database.executeUpdateNoException("ALTER TABLE " + database.getPrefix() + "protections RENAME TO " + database.getPrefix() + "protections_old_converting");

            database.log("Creating new protections table");

            // ensure the new table exists
            Table protections = new Table(database, "protections");
            Column column;
            {
                column = new Column("id");
                column.setType("INTEGER");
                column.setPrimary(true);
                protections.add(column);

                column = new Column("owner");
                column.setType("int");
                protections.add(column);

                column = new Column("type");
                column.setType("INTEGER");
                protections.add(column);

                column = new Column("x");
                column.setType("INTEGER");
                protections.add(column);

                column = new Column("y");
                column.setType("INTEGER");
                protections.add(column);

                column = new Column("z");
                column.setType("INTEGER");
                protections.add(column);

                column = new Column("flags");
                column.setType("INTEGER");
                protections.add(column);

                column = new Column("data");
                column.setType("TEXT");
                protections.add(column);

                column = new Column("blockId");
                column.setType("INTEGER");
                protections.add(column);

                column = new Column("world");
                column.setType("VARCHAR(255)");
                protections.add(column);

                column = new Column("password");
                column.setType("VARCHAR(255)");
                protections.add(column);

                column = new Column("date");
                column.setType("VARCHAR(255)");
                protections.add(column);

                column = new Column("last_accessed");
                column.setType("INTEGER");
                protections.add(column);
            }

            protections.execute();

            database.log("Restoring indexes");

            database.createIndex("protections", "protections_main", "x, y, z, world");
            database.createIndex("protections", "protections_utility", "owner");
            database.createIndex("protections", "protections_type", "type");

            database.setInternal("uuidConversionProtectionsTableCreated", "true");
        }

        database.log("Beginning conversion of old protections");
    }

    public void onComplete() {
        PhysDB database = LWC.getInstance().getPhysicalDatabase();
        database.executeUpdateNoException("ALTER TABLE " + database.getPrefix() + "protections_old_converting RENAME TO " + database.getPrefix() + "protections_old");
        database.log("Converted all protections to new format");
        database.precache();
    }

    public void handle(TableWalker walker, Map<String, Object> row) {
        PhysDB database = LWC.getInstance().getPhysicalDatabase();

        int id = (Integer) row.get("id");

        Protection protection = database.loadProtection(id);

        protection.convertPlayerNamesToUUIDs();
        protection.saveNow();
    }

}
