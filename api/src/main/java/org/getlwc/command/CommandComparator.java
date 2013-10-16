package org.getlwc.command;

import java.util.Comparator;

public class CommandComparator implements Comparator<Command> {

    /**
     * {@inheritDoc}
     */
    public int compare(Command a, Command b) {
        return a.command().compareTo(b.command());
    }

}
