package org.getlwc.event;

public interface EventNotifier<T extends Event> {

    /**
     * Notifies the callee of the event being called.
     *
     * @param event
     * @return
     */
    public void call(T event);

}
