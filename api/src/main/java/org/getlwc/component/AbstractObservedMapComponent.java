/**
 * Copyright (c) 2011-2014 Tyler Blair
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
package org.getlwc.component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AbstractObservedMapComponent<K, V> extends AbstractMapComponent<K, V> {

    /**
     * Objects that were added to the set
     */
    private final Set<V> objectsAdded = new HashSet<>();

    /**
     * Objects that were removed from the set
     */
    private final Set<V> objectsRemoved = new HashSet<>();

    /**
     * Resets all watched state
     */
    public void resetObservedState() {
        objectsAdded.clear();
        objectsRemoved.clear();
    }

    /**
     * Returns an unmodifiable set of objects that have been added to this set
     *
     * @return
     */
    public Set<V> getObjectsAdded() {
        return Collections.unmodifiableSet(objectsAdded);
    }

    /**
     * Returns an unmodifiable set of objects that have been removed from this set
     *
     * @return
     */
    public Set<V> getObjectsRemoved() {
        return Collections.unmodifiableSet(objectsRemoved);
    }

    @Override
    public V put(K key, V value) {
        objectsAdded.add(value);
        return super.put(key, value);
    }

    @Override
    public V remove(Object key) {
        V removed = super.remove(key);

        if (removed != null) {
            objectsRemoved.add(removed);
        }

        return removed;
    }

}
