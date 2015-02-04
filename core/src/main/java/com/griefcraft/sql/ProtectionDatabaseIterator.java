package com.griefcraft.sql;

import com.griefcraft.model.Protection;
import com.griefcraft.sql.PhysDB;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ProtectionDatabaseIterator implements Iterator<Protection> {

    /**
     * Fetch size from the database
     */
    private static final int FETCH_SIZE = 1000;

    /**
     * The database protections are being pulled from.
     */
    private PhysDB database;

    /**
     * A queue of all available protections in the current batch
     */
    private Queue<Protection> protections = new LinkedList<Protection>();

    /**
     * The current protection being iterated
     */
    private Protection current = null;

    /**
     * The current limit
     */
    private int limit = 0;

    public ProtectionDatabaseIterator(PhysDB database) {
        this.database = database;
        fetchMore();
    }

    public boolean hasNext() {
        return !protections.isEmpty();
    }

    public Protection next() {
        current = protections.poll();

        if (protections.isEmpty()) {
            fetchMore();
        }

        return current;
    }

    public void remove() {
        current.remove();
    }

    /**
     * Fetches more protections
     */
    private void fetchMore() {
        List<Protection> loaded = database.loadProtections(limit, FETCH_SIZE);

        for (Protection protection : loaded) {
            protections.add(protection);
        }

        limit += loaded.size();
    }

}
