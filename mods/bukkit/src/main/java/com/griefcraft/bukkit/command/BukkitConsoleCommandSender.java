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

package com.griefcraft.bukkit.command;

import com.griefcraft.command.ConsoleCommandSender;
import com.griefcraft.util.Color;

public class BukkitConsoleCommandSender implements ConsoleCommandSender {

    /**
     * The command sender handle
     */
    private org.bukkit.command.ConsoleCommandSender handle;

    public BukkitConsoleCommandSender(org.bukkit.command.ConsoleCommandSender handle) {
        if (handle == null) {
            throw new IllegalArgumentException("Console sender handle cannot be null");
        }

        this.handle = handle;
    }

    /**
     * {@inheritDoc}
     */
    public void sendMessage(String message) {
        for (String line : message.split("\n")) {
            handle.sendMessage("[LWC] " + Color.replaceColors(line));
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPermission(String node) {
        return true;
    }

}
