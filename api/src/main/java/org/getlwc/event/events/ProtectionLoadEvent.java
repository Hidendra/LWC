package org.getlwc.event.events;

import org.getlwc.model.Protection;

/**
 * Event representing when a protection is first loaded from the database.
 */
public class ProtectionLoadEvent extends ProtectionEvent {

    public ProtectionLoadEvent(Protection protection) {
        super(protection);
    }

}
