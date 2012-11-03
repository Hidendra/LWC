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

package com.griefcraft.model;

public abstract class AbstractAttribute extends AbstractSavable {

    /**
     * The attributes name
     */
    private final String name;

    public AbstractAttribute(String name) {
        this.name = name;
    }

    /**
     * Get the attributes name
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public void saveImmediately() {
        throw new UnsupportedOperationException("AbstractSavable.save() is not yet implemented");
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
        throw new UnsupportedOperationException("AbstractSavable.save() is not yet implemented");
    }

    /**
     * Get a string representation of the attribute which will be stored in the database.
     * When the attribute is being loaded from the database loadValue() will be
     * called so that the implementing class can decode the string.
     *
     * @return
     */
    public abstract String getStorableValue();

    /**
     * Load an attribute's value from the given string representation.
     * This is the inverse of the function getStorableValue()
     *
     * @param value
     */
    public abstract void loadValue(String value);

    /**
     * {@inheritDoc}
     */
    public abstract boolean isSaveNeeded();

}
