/*
 * Copyright 2011 Tyler Blair. All rights reserved.
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

package com.griefcraft.scripting;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.event.LWCAccessEvent;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCDropItemEvent;
import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.scripting.event.LWCProtectionRegistrationPostEvent;
import com.griefcraft.scripting.event.LWCProtectionRemovePostEvent;
import com.griefcraft.scripting.event.LWCRedstoneEvent;
import com.griefcraft.scripting.event.LWCSendLocaleEvent;

/**
 * This interface defines methods that modules may implement
 */
public interface Module {

    // Results returned by methods
    public enum Result {
        ALLOW, CANCEL, DEFAULT
    }

    /**
     * Called when the module is loaded
     */
    public void load(LWC lwc);

    /**
     * Find out the access level of a player to a protection
     *
     * @param event
     */
    public void onAccessRequest(LWCAccessEvent event);

    /**
     * Called when a player drops an item
     *
     * @param event
     */
    public void onDropItem(LWCDropItemEvent event);

    /**
     * Called when a player or console executes an LWC command
     *
     * @param event
     */
    public void onCommand(LWCCommandEvent event);

    /**
     * Called when redstone interacts with a protection
     *
     * @param event
     */
    public void onRedstone(LWCRedstoneEvent event);

    /**
     * Called when a protection is destroyed
     *
     * @param event
     */
    public void onDestroyProtection(LWCProtectionDestroyEvent event);

    /**
     * Called when a valid protection is interacted with
     *
     * @param event
     */
    public void onProtectionInteract(LWCProtectionInteractEvent event);

    /**
     * Called when an unprotected block is interacted with
     *
     * @param event
     */
    public void onBlockInteract(LWCBlockInteractEvent event);

    /**
     * Called immediately before a protection is registered
     *
     * @param event
     */
    public void onRegisterProtection(LWCProtectionRegisterEvent event);

    /**
     * Called after a protection is registered
     *
     * @param event
     */
    public void onPostRegistration(LWCProtectionRegistrationPostEvent event);

    /**
     * Called after a protection is removed (the Protection class given is immutable.)
     *
     * @param event
     */
    public void onPostRemoval(LWCProtectionRemovePostEvent event);

    /**
     * Called when LWC or another module sends a locale message to a player
     *
     * @param event
     */
    public void onSendLocale(LWCSendLocaleEvent event);

}
