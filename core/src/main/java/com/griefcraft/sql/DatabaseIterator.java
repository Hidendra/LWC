package com.griefcraft.sql;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class DatabaseIterator<T> implements Iterator<T> {

    /**
     * Fetch size from the database
     */
    private static final int FETCH_SIZE = 1000;

    /**
     * The database protections are being pulled from.
     */
    protected PhysDB database;

    /**
     * A queue of all available objects in the current batch
     */
    private Queue<T> objects = new LinkedList<T>();

    /**
     * The current protection being iterated
     */
    protected T current = null;

    /**
     * The current limit
     */
    private int limit = 0;

    public DatabaseIterator(PhysDB database) {
        this.database = database;
        fetchMore();
    }

    public boolean hasNext() {
        return !objects.isEmpty();
    }

    public T next() {
        current = objects.poll();

        if (objects.isEmpty()) {
            fetchMore();
        }

        return current;
    }

    /**
     * Removes the current object.
     */
    public abstract void remove();

    /**
     * Fetches more objects, given a start point & count to fetch.
     *
     * @param start
     * @param count
     * @return
     */
    public abstract List<T> fetchMore(int start, int count);

    /**
     * Fetches more objects
     */
    private void fetchMore() {
        List<T> loaded = fetchMore(limit, FETCH_SIZE);

        objects.addAll(loaded);
        limit += loaded.size();
    }

}
