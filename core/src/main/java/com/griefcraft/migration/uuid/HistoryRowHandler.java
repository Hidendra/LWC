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
        database.log("Beginning conversion of old history");
    }

    public void onComplete() {
        PhysDB database = LWC.getInstance().getPhysicalDatabase();
        database.executeUpdateNoException("ALTER TABLE " + database.getPrefix() + "history_old_converting RENAME TO " + database.getPrefix() + "history_old");
        database.log("Converted all history to new format");
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
