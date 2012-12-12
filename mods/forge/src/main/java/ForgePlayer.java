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

import com.griefcraft.entity.Player;
import com.griefcraft.event.PlayerEventDelegate;
import com.griefcraft.util.Color;
import com.griefcraft.world.Location;

public class ForgePlayer extends Player {

    /**
     * The mod handle
     */
    private LWC mod;

    /**
     * Player handle
     */
    private qx handle;

    /**
     * The event delegate
     */
    private PlayerEventDelegate eventDelegate;

    public ForgePlayer(qx handle) {
        this.handle = handle;
        this.mod = LWC.instance;
        this.eventDelegate = new PlayerEventDelegate(LWC.instance.getEngine(), this);
    }

    @Override
    public String getName() {
        return handle.bQ;
    }

    @Override
    public Location getLocation() {
        try {
            return new Location(new ForgeWorld(handle.p), (int) handle.t, (int) handle.u, (int) handle.v);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public PlayerEventDelegate getEventDelegate() {
        return eventDelegate;
    }

    public void sendMessage(String message) {
        for (String line : message.split("\n")) {
            handle.a(Color.replaceColors(line));
        }
    }

    public boolean hasPermission(String node) {
        return true; // TODO check for OP from lwc.admin.*, allow any others
    }

}