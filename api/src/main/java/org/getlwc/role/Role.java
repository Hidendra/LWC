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
