package org.getlwc;

import org.getlwc.entity.Entity;
import org.getlwc.model.Protection;

/**
 * A provider that wants to know when a protection is interacted by. If this interface is attached to
 * an attribute it will only be called on protections that have that attribute
 */
public interface InteractProvider {

    /**
     * Called when a protection is interacted
     *
     * @param protection
     * @param entity
     * @param access
     */
    public void onInteract(Protection protection, Entity entity, Protection.Access access);

}
