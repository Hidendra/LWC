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

package com.griefcraft.command;

import com.griefcraft.util.StringUtils;

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
    private String command;

    /**
     * The arguments for the command
     */
    private String arguments;

    /**
     * The arguments split
     */
    private String[] argumentsArray;

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

        if (arguments.length() > 0) {
            argumentsArray = StringUtils.split(arguments);
        } else {
            argumentsArray = new String[0];
        }
    }

    public CommandContext(Type type, CommandSender sender, String command) {
        this(type, sender, command, "");
    }

    /**
     * Get an argument at the specified index. If the command defines min/max values, this is guaranteed
     * to return non-null if the index is within those ranges.
     *
     * @param index the index number. For example, the first argument is 1, second argument 2, etc
     * @return
     */
    public String getArgument(int index) {
        // Convert it to 0-n format, not 1-n
        index = index - 1;

        if (index < 0 || index > argumentsArray.length) {
            throw new IndexOutOfBoundsException("Index cannot be out of range!");
        }

        return argumentsArray[index];
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

    /**
     * Get the arguments array
     *
     * @return
     */
    public String[] getArgumentsArray() {
        return argumentsArray;
    }

    /**
     * Set the command
     *
     * @param command
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * Set the arguments of the command
     *
     * @param arguments
     */
    public void setArguments(String arguments) {
        arguments = arguments.trim();
        this.arguments = arguments;

        // regenerate the arguments
        if (arguments.isEmpty()) {
            argumentsArray = new String[0];
        } else {
            argumentsArray = StringUtils.split(arguments);
        }
    }

}