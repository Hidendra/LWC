package org.getlwc.event.handler;

import org.getlwc.event.Event;
import org.getlwc.event.Listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BaseHandler {

    /**
     * The listener annotation
     */
    private Listener listener;

    /**
     * The object registering this handler
     */
    private Object parent;

    /**
     * The method being attached
     */
    private Method method;

    public BaseHandler(Listener listener, Object parent, Method method) {
        this.listener = listener;
        this.parent = parent;
        this.method = method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseHandler that = (BaseHandler) o;

        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        if (parent != null ? !parent.equals(that.parent) : that.parent != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = parent != null ? parent.hashCode() : 0;
        result = 31 * result + (method != null ? method.hashCode() : 0);
        return result;
    }

    /**
     * Returns the event type this handler is for
     *
     * @return
     */
    public Class<? extends Event> getEventType() {
        return (Class<? extends Event>) method.getParameterTypes()[0];
    }

    public Object getParent() {
        return parent;
    }

    public Method getMethod() {
        return method;
    }

    /**
     * Invokes the handler with the given event
     *
     * @param event
     */
    public void invoke(Event event) {
        if (listener.ignoreCancelled() && event.isCancelled()) {
            return;
        }

        try {
            method.invoke(parent, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
