package com.griefcraft.migration;

import com.griefcraft.lwc.LWC;
import com.griefcraft.sql.Database;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Observable;

public abstract class TableWalker extends Observable implements Runnable {

    /**
     * The number of ticks in between rounds
     */
    public static final int TICK_INTERVAL = 5;

    /**
     * The number of rows walked per round
     */
    public static final int WALKS_PER_ROUND = 50;

    /**
     * The database to use to walk over
     */
    protected Database database;

    /**
     * The handler being used for each row
     */
    protected RowHandler handler;

    /**
     * The current offset being used for selects
     */
    protected int offset = 0;

    /**
     * The task used for the walker
     */
    protected BukkitTask task;

    public TableWalker(Database database, RowHandler handler) {
        this(database, handler, 0);
    }

    public TableWalker(Database database, RowHandler handler, int startOffset) {
        this.database = database;
        this.handler = handler;
        this.offset = startOffset;
    }

    /**
     * Start the table walker
     */
    public void start() {
        if (task == null) {
            task = Bukkit.getScheduler().runTaskTimer(LWC.getInstance().getPlugin(), this, TICK_INTERVAL, TICK_INTERVAL);
        }
    }

    /**
     * Get the database the is being walked
     *
     * @return
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Get the row handler for this walker
     *
     * @return
     */
    public RowHandler getHandler() {
        return handler;
    }

    /**
     * Get the offset of the walker
     *
     * @return
     */
    public int getOffset() {
        return offset;
    }

    public abstract void run();

}
