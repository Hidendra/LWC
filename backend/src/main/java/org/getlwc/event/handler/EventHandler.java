package org.getlwc.event.handler;

import org.getlwc.event.Event;

/**
 * Represents a handler for an event
 */
public interface EventHandler {

    /**
     * Returns the event type this handler is listening for
     *
     * @return
     */
    public Class<? extends Event> getEventType();

    /**
     * Invokes the event handler using the given event
     *
     * @param event
     */
    public void invoke(Event event);

}
