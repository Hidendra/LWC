package org.getlwc.entity;

import org.getlwc.SimpleEngine;

public abstract class SimplePlayer extends Player {

    public boolean hasPermission(String node) {
        return SimpleEngine.getInstance().getPermission().hasPermission(this, node);
    }

}
