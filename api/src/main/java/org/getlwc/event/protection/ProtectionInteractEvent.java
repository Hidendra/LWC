package org.getlwc.event.protection;

import org.getlwc.event.ProtectionEvent;
import org.getlwc.model.Protection;

public class ProtectionInteractEvent extends ProtectionEvent {
    public ProtectionInteractEvent(Protection protection) {
        super(protection);
    }
}
