/*
 * Copyright (c) 2011, 2012, Tyler Blair
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package com.griefcraft.sql;

import com.griefcraft.Location;
import com.griefcraft.Role;
import com.griefcraft.model.AbstractAttribute;
import com.griefcraft.model.Protection;

import java.util.Set;

public interface Database {

    /**
     * Connect to the database
     *
     * @return
     * @throws DatabaseException
     */
    public boolean connect() throws DatabaseException;

    /**
     * Disconnct from the database
     */
    public void disconnect();

    /**
     * Create a protection in the world.
     *
     * @param location
     * @return
     */
    public Protection createProtection(Location location);

    /**
     * Load a protection from the database at the given location
     *
     * @return
     */
    public Protection loadProtection(Location location);

    /**
     * Load a protection from the database for the given id
     *
     * @param id
     * @return
     */
    public Protection loadProtection(int id);

    /**
     * Save a protection to the database
     *
     * @param protection
     */
    public void saveProtection(Protection protection);

    /**
     * Remove a protection and all associated data about it from the database
     *
     * @param protection
     */
    public void removeProtection(Protection protection);

    /**
     * Save a role to the database. If the role needs to be created, this method is required
     * to create it in the database as well.
     *
     * @param role
     */
    public void saveOrCreateRole(Role role);

    /**
     * Remove a role from the database
     *
     * @param role
     */
    public void removeRole(Role role);

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
     * Load all of a protection's attributes from the database
     *
     * @param protection
     * @return
     */
    public Set<AbstractAttribute> loadProtectionAttributes(Protection protection);

    /**
     * Load all of a protection's roles
     *
     * @param protection
     * @return
     */
    public Set<Role> loadProtectionRoles(Protection protection);

}
