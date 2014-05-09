package org.getlwc.db;

import org.getlwc.model.AbstractAttribute;
import org.getlwc.model.Protection;

import java.util.Set;

public interface AttributeDatabase {

    /**
     * Load all of a protection's attributes from the database
     *
     * @param protection
     * @return
     */
    public Set<AbstractAttribute> loadProtectionAttributes(Protection protection);

    /**
     * Save or create an attribute in the database.
     *
     * @param protection
     * @param attribute
     */
    public void saveOrCreateProtectionAttribute(Protection protection, AbstractAttribute attribute);

    /**
     * Remove a protection's attribute from the database
     *
     * @param protection
     * @param attribute
     */
    public void removeProtectionAttribute(Protection protection, AbstractAttribute attribute);

    /**
     * Remove all protection attributes from a protection from the database
     *
     * @param protection
     */
    public void removeProtectionAttributes(Protection protection);

}
