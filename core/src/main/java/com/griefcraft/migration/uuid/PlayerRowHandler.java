package com.griefcraft.migration.uuid;

import com.griefcraft.migration.RowHandler;
import com.griefcraft.migration.TableWalker;
import com.griefcraft.model.PlayerInfo;
import com.griefcraft.sql.PhysDB;
import com.griefcraft.util.PlayerRegistry;

import java.util.Map;
import java.util.UUID;

public class PlayerRowHandler implements RowHandler {

    public void onStart() {

    }

    public void onComplete() {

    }

    public void handle(TableWalker walker, Map<String, Object> row) {
        PhysDB database = (PhysDB) walker.getDatabase();
        String name = (String) row.get("name");

        PlayerInfo playerInfo = PlayerRegistry.getPlayerInfo(name);
        database.log("Converted " + name + " to " + playerInfo);
    }

}
