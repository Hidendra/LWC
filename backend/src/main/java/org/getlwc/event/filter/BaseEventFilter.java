package org.getlwc.event.filter;

import org.getlwc.event.Event;
import org.getlwc.event.Listener;
import org.getlwc.util.Filter;

public class BaseEventFilter implements Filter<Event> {

    private Listener listener;

    public BaseEventFilter(Listener listener) {
        this.listener = listener;
    }

    @Override
    public boolean accept(Event value) {
        return !(listener.ignoreCancelled() && value.isCancelled());
    }

}
