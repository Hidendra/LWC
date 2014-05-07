package com.griefcraft.migration.uuid;

import com.griefcraft.lwc.LWC;
import com.griefcraft.migration.RowHandler;
import com.griefcraft.migration.TableWalker;
import com.griefcraft.model.History;
import com.griefcraft.model.Protection;
import com.griefcraft.sql.Column;
import com.griefcraft.sql.PhysDB;
import com.griefcraft.sql.Table;

import java.util.Map;

public class HistoryRowHandler implements RowHandler {

    public void onStart() {
        PhysDB database = LWC.getInstance().getPhysicalDatabase();

        if (database.getInternal("uuidConversionHistoryTableCreated") == null) {
            database.log("Renaming old history table");

            database.executeUpdateNoException("ALTER TABLE " + database.getPrefix() + "history RENAME TO " + database.getPrefix() + "history_old_converting");

            database.log("Creating new history table");

            // ensure the new table exists
            Table history = new Table(database, "history");
            Column column;
            {
                column = new Column("id");
                column.setType("INTEGER");
                column.setPrimary(true);
                history.add(column);

                column = new Column("protectionId");
                column.setType("INTEGER");
                history.add(column);

                column = new Column("player");
                column.setType("INTEGER");
                history.add(column);

                column = new Column("x");
                column.setType("INTEGER");
                history.add(column);

                column = new Column("y");
                column.setType("INTEGER");
                history.add(column);

                column = new Column("z");
                column.setType("INTEGER");
                history.add(column);

                column = new Column("type");
                column.setType("INTEGER");
                history.add(column);

                column = new Column("status");
                column.setType("INTEGER");
                history.add(column);

                column = new Column("metadata");
                column.setType("VARCHAR(255)");
                history.add(column);

                column = new Column("timestamp");
                column.setType("long");
                history.add(column);
            }

            history.execute();

            database.log("Restoring indexes");

            database.createIndex("history", "history_main", "protectionId");
            database.createIndex("history", "history_utility", "player");
            database.createIndex("history", "history_utility2", "x, y, z");

            database.setInternal("uuidConversionHistoryTableCreated", "true");
        }

        database.log("Beginning conversion of old history");
    }

    public void onComplete() {
        PhysDB database = LWC.getInstance().getPhysicalDatabase();
        database.executeUpdateNoException("ALTER TABLE " + database.getPrefix() + "history_old_converting RENAME TO " + database.getPrefix() + "history_old");
        database.log("Converted all protections to new format");
        database.precache();
    }

    public void handle(TableWalker walker, Map<String, Object> row) {
        PhysDB database = LWC.getInstance().getPhysicalDatabase();

        int id = (Integer) row.get("id");

        History history = database.loadHistory(id);

        // TODO convert history metadata
        history.setExists(false);
        history.saveNow();
    }

}
