package org.getlwc.event.handler;

import org.getlwc.event.Event;
import org.getlwc.util.Filter;

import java.lang.reflect.Method;

public class FilteredHandler extends BaseHandler {

    /**
     * The filter to apply to posted events
     */
    private Filter<Event> filter;

    public FilteredHandler(Object parent, Method method, Filter<Event> filter) {
        super(parent, method);
        this.filter = filter;
    }

    @Override
    public void invoke(Event event) {
        if (filter.accept(event)) {
            super.invoke(event);
        }
    }

}
