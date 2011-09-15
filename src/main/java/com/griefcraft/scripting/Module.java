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
