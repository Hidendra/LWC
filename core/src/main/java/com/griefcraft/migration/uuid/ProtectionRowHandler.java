package com.griefcraft.migration.uuid;

import com.griefcraft.migration.RowHandler;
import com.griefcraft.migration.TableWalker;
import com.griefcraft.sql.Database;

import java.util.Map;

public class ProtectionRowHandler implements RowHandler {

    public void handle(TableWalker walker, Map<String, Object> row) {
        Database database = walker.getDatabase();

        database.log("Handling id=" + row.get("id") + " owner=" + row.get("owner"));
    }

}
