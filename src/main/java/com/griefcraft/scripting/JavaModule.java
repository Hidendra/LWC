/**
 * This file is part of LWC (https://github.com/Hidendra/LWC)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

public class JavaModule implements Module {

    /**
     * Allow the event to occur (e.g allow the redstone, allow a protection destruction, and so on)
     */
    public final static Result ALLOW = Result.ALLOW;

    /**
     * Cancel the event from happening (e.g disallow protection interaction, disallow protection registration)
     */
    public final static Result CANCEL = Result.CANCEL;

    /**
     * The default result returned by events
     */
    public final static Result DEFAULT = Result.DEFAULT;

    public void load(LWC lwc) {
    }

    public void onAccessRequest(LWCAccessEvent event) {

    }

    public void onDropItem(LWCDropItemEvent event) {

    }

    public void onCommand(LWCCommandEvent event) {

    }

    public void onRedstone(LWCRedstoneEvent event) {

    }

    public void onDestroyProtection(LWCProtectionDestroyEvent event) {

    }

    public void onProtectionInteract(LWCProtectionInteractEvent event) {

    }

    public void onBlockInteract(LWCBlockInteractEvent event) {

    }

    public void onRegisterProtection(LWCProtectionRegisterEvent event) {

    }

    public void onPostRegistration(LWCProtectionRegistrationPostEvent event) {

    }

    public void onPostRemoval(LWCProtectionRemovePostEvent event) {

    }

    public void onSendLocale(LWCSendLocaleEvent event) {

    }

}
