package org.getlwc.event.filter;

import org.getlwc.component.Component;
import org.getlwc.event.Event;
import org.getlwc.event.ProtectionListener;
import org.getlwc.event.protection.ProtectionEvent;
import org.getlwc.model.Protection;
import org.getlwc.util.Filter;

public class ProtectionEventFilter implements Filter<Event> {

    private ProtectionListener listener;

    public ProtectionEventFilter(ProtectionListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean accept(Event event) {
        if (!(event instanceof ProtectionEvent)) {
            return false;
        }

        ProtectionEvent protectionEvent = (ProtectionEvent) event;
        Protection protection = protectionEvent.getProtection();

        boolean shouldCallEvent = false;

        if (listener.components().length == 0) {
            shouldCallEvent = true;
        } else {
            for (Class<? extends Component> componentClass : listener.components()) {
                if (protection.hasComponent(componentClass)) {
                    shouldCallEvent = true;
                    break;
                }
            }
        }

        return shouldCallEvent;
    }

}
