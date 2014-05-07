package com.griefcraft.migration.uuid;

import com.griefcraft.migration.RowHandler;
import com.griefcraft.migration.TableWalker;
import com.griefcraft.util.PlayerRegistry;

import java.util.Map;

public class PlayerRowHandler implements RowHandler {

    public void onStart() {

    }

    public void onComplete() {

    }

    public void handle(TableWalker walker, Map<String, Object> row) {
        String name = (String) row.get("name");
        PlayerRegistry.getPlayerInfo(name);
    }

}
