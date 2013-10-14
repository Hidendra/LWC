/*
 * Copyright (c) 2011-2013 Tyler Blair
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

package org.getlwc.sql;

import org.getlwc.Engine;
import org.getlwc.Location;
import org.getlwc.ProtectionRole;
import org.getlwc.model.AbstractAttribute;
import org.getlwc.model.Protection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryDatabase implements Database {

    /**
     * The {@link Engine} instance
     */
    private final Engine engine;

    /**
     * A set of all of the protections
     */
    private final Set<Protection> protections = new HashSet<Protection>();

    /**
     * Protections indexed by their ID
     */
    private final Map<Integer, Protection> protectionsIndexById = new HashMap<Integer, Protection>();

    /**
     * Protections indexed by their location
     */
    private Map<Location, Protection> protectionsIndexByLocation = new HashMap<Location, Protection>();

    /**
     * The protection auto increment ID
     */
    private final AtomicInteger protectionsId = new AtomicInteger(0);

    public MemoryDatabase(Engine engine) {
        this.engine = engine;
    }

    /**
     * Add a protection to the indexes
     *
     * @param protection
     */
    private void internalAddProtection(Protection protection) {
        protections.add(protection);
        protectionsIndexById.put(protection.getId(), protection);
        protectionsIndexByLocation.put(protection.getLocation(), protection);
    }

    /**
     * {@inheritDoc}
     */
    private void internalRemoveProtection(Protection protection) {
        protections.remove(protection);
        protectionsIndexById.remove(protection.getId());
        protectionsIndexByLocation.remove(protection.getLocation());
    }

    /**
     * {@inheritDoc}
     */
    public boolean connect() throws DatabaseException {
        return true; // already connected
    }

    /**
     * {@inheritDoc}
     */
    public void disconnect() {
        protections.clear();
        protectionsIndexById.clear();
        protectionsIndexByLocation.clear();
        protectionsId.set(0);
    }

    /**
     * {@inheritDoc}
     */
    public Protection createProtection(Location location) {
        Protection protection = new Protection(engine, protectionsId.getAndIncrement());
        protection.setWorld(location.getWorld());
        protection.setX(location.getBlockX());
        protection.setY(location.getBlockY());
        protection.setZ(location.getBlockZ());
        internalAddProtection(protection);
        return protection;
    }

    /**
     * {@inheritDoc}
     */
    public Protection loadProtection(Location location) {
        return protectionsIndexByLocation.get(location);
    }

    /**
     * {@inheritDoc}
     */
    public Protection loadProtection(int id) {
        return protectionsIndexById.get(id);
    }

    /**
     * {@inheritDoc}
     */
    public void saveProtection(Protection protection) {
        internalAddProtection(protection);
    }

    /**
     * {@inheritDoc}
     */
    public void removeProtection(Protection protection) {
        internalRemoveProtection(protection);
    }

    /**
     * {@inheritDoc}
     */
    public void saveOrCreateRole(ProtectionRole role) {
        // no need to create
    }

    /**
     * {@inheritDoc}
     */
    public void removeRole(ProtectionRole role) {
        // it will remove itself from the Protection object
    }

    /**
     * {@inheritDoc}
     */
    public void removeRoles(Protection protection) {
        // it will remove itself from the Protection object
    }

    /**
     * {@inheritDoc}
     */
    public void saveOrCreateProtectionAttribute(Protection protection, AbstractAttribute attribute) {
        // no need to create
    }

    /**
     * {@inheritDoc}
     */
    public void removeProtectionAttribute(Protection protection, AbstractAttribute attribute) {
        // it will remove itself from the Protection object
    }

    /**
     * {@inheritDoc}
     */
    public void removeProtectionAttributes(Protection protection) {
        // it will remove itself from the Protection object
    }

    /**
     * {@inheritDoc}
     */
    public Set<AbstractAttribute> loadProtectionAttributes(Protection protection) {
        return new HashSet<AbstractAttribute>(); // nothing to load from
    }

    /**
     * {@inheritDoc}
     */
    public Set<ProtectionRole> loadProtectionRoles(Protection protection) {
        return new HashSet<ProtectionRole>(); // nothing to load from
    }

}
