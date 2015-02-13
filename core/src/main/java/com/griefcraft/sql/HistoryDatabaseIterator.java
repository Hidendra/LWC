package com.griefcraft.sql;

import com.griefcraft.model.History;

import java.util.List;

public class HistoryDatabaseIterator extends DatabaseIterator<History> {

    public HistoryDatabaseIterator(PhysDB database) {
        super(database);
    }

    @Override
    public void remove() {
        current.remove();
    }

    @Override
    public List<History> fetchMore(int start, int count) {
        return database.loadHistory(start, count);
    }

}
