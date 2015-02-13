package com.griefcraft.sql;

import com.griefcraft.model.Protection;
import com.griefcraft.sql.PhysDB;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ProtectionDatabaseIterator extends DatabaseIterator<Protection> {

    public ProtectionDatabaseIterator(PhysDB database) {
        super(database);
    }

    @Override
    public void remove() {
        current.remove();
    }

    @Override
    public List<Protection> fetchMore(int start, int count) {
        return database.loadProtections(start, count);
    }

}
