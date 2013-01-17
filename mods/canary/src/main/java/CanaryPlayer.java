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

import com.griefcraft.Location;
import com.griefcraft.util.Color;

public class CanaryPlayer extends com.griefcraft.entity.Player {

    /**
     * The plugin object
     */
    private LWC plugin;

    /**
     * The player handle
     */
    private Player handle;

    public CanaryPlayer(LWC plugin, Player handle) {
        if (handle == null) {
            throw new IllegalArgumentException("Player handle cannot be null");
        }

        this.plugin = plugin;
        this.handle = handle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return handle.getName();
    }

    /**
     * {@inheritDoc}
     */
    public Location getLocation() {
        return new Location(plugin.getWorld(handle.getWorld().getName()), handle.getX(), handle.getY(), handle.getZ());
    }

    /**
     * {@inheritDoc}
     */
    public void sendMessage(String message) {
        for (String line : message.split("\n")) {
            handle.sendMessage(Color.replaceColors(line));
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPermission(String node) {
        // hard coded for now.. what ever shall we do :-)
        if (node.equals("lwc.protect")) {
            return handle.canUseCommand("/lwc");
        } else if (node.startsWith("lwc.admin")) {
            return handle.canUseCommand("/lwcadmin");
        } else {
            return handle.canUseCommand("/lwc");
        }
    }

}
