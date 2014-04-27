package com.griefcraft.migration;

import java.util.Map;

/**
 * The handler that is applied to each row in a table walker. The handler chooses how to handle each row.
 */
public interface RowHandler {

    /**
     * Handle the given row
     *
     * @param walker
     * @param row
     */
    public void handle(TableWalker walker, Map<String, Object> row);

}
