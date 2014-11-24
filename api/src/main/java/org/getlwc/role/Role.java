package org.getlwc.role;

import org.getlwc.AccessProvider;
import org.getlwc.model.Protection;

/**
 * A role can be attached to a protection to provide access control.
 * Typically, most roles will extend {@link org.getlwc.role.AbstractRole}
 * to easily allow access control.
 */
public interface Role extends AccessProvider {

    /**
     * Get this role's type. Used to serialize in the database
     *
     * @return
     */
    public String getType();

    /**
     * Serializes the role into a string for use in storage
     *
     * @return
     */
    public String serialize();

    /**
     * Returns the access that this role provides
     *
     * @return
     */
    public Protection.Access getAccess();

    /**
     * Sets the access on the role
     *
     * @param access
     */
    public void setAccess(Protection.Access access);

    /**
     * Returns true if the access on the role has been changed
     *
     * @return
     */
    public boolean accessChanged();

    /**
     * Marks the role as unchanged
     */
    public void markUnchanged();

}
