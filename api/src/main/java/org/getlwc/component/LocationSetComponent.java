package org.getlwc.component;

import org.getlwc.Location;

public class LocationSetComponent extends AbstractSetComponent<Location> {

    @Override
    public String toString() {
        return String.format("LocationSetComponent(%s)", getAll());
    }

}
