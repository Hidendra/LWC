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

package com.griefcraft.command;

public class CommandContext {

    public enum Type {

        /**
         * The command was executed by a player
         */
        PLAYER,

        /**
         * The command is an omnipotent server command
         */
        SERVER

    }

    /**
     * The command type
     */
    private final Type type;

    /**
     * The command sender
     */
    private final CommandSender sender;

    /**
     * The command
     */
    private final String command;

    /**
     * The arguments for the command
     */
    private final String arguments;

    public CommandContext(Type type, CommandSender sender, String command, String arguments) {
        if (type == null) {
            throw new IllegalArgumentException("Command type cannot be null");
        }
        if (sender == null) {
            throw new IllegalArgumentException("Command sender cannot be null");
        }
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (arguments == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }

        this.type = type;
        this.sender = sender;
        this.command = command;
        this.arguments = arguments;
    }

    public CommandContext(Type type, CommandSender sender, String command) {
        this(type, sender, command, "");
    }

    /**
     * Get the command type
     * 
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * Get the command sender
     * 
     * @return
     */
    public CommandSender getCommandSender() {
        return sender;
    }

    /**
     * Get the command that is being used
     * 
     * @return
     */
    public String getCommand() {
        return command;
    }

    /**
     * Get the arguments for the command
     *
     * @return
     */
    public String getArguments() {
        return arguments;
    }

}