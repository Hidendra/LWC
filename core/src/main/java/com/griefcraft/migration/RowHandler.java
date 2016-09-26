package com.griefcraft.migration;

import java.util.Map;

/**
 * The handler that is applied to each row in a table walker. The handler chooses how to handle each row.
 */
public interface RowHandler {

    /**
     * Called when the row handler is first started
     */
    public void onStart();

    /**
     * Called when the row handler completes walking over all rows
     */
    public void onComplete();

    /**
     * Handle the given row
     *
     * @param walker
     * @param row
     */
    public void handle(TableWalker walker, Map<String, Object> row);

}
