package org.getlwc.event;

public interface EventConsumer<T extends Event> {

    /**
     * Performs the operation on the given argument.
     *
     * @param event
     */
    public void accept(T event);

}
