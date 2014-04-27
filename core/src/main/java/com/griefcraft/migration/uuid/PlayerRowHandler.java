package com.griefcraft.migration.uuid;

import com.griefcraft.migration.RowHandler;
import com.griefcraft.migration.TableWalker;
import com.griefcraft.sql.Database;
import com.griefcraft.util.UUIDRegistry;

import java.util.Map;
import java.util.UUID;

public class PlayerRowHandler implements RowHandler {

    public void handle(TableWalker walker, Map<String, Object> row) {
        Database database = walker.getDatabase();
        String name = (String) row.get("name");

        if (UUIDRegistry.isValidUUID(name)) {
            database.log("Converting player UUID: " + name);
            String playerName = UUIDRegistry.getName(UUID.fromString(name));

            if (playerName == null) {
                playerName = "[unknown]";
            }

            database.log("Matched UUID " + name + " => " + playerName);
        } else {
            database.log("Converting player name: " + name);
            UUID uuid = UUIDRegistry.getUUID(name);

            database.log("Matched name " + name + " => " + uuid);
        }
    }

}
