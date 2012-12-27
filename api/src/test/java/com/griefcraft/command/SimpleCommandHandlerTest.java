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

import org.junit.Before;
import org.junit.Test;

public class SimpleCommandHandlerTest {

    /**
     * The test handler object
     */
    private CommandHandler handler;

    @Before
    public void init() {
        handler = new SimpleCommandHandler(null);
    }

    @Test
    public void validCommand() throws CommandException {
        handler.registerCommands(new ValidCommand());
    }

    @Test(expected = CommandException.class)
    public void invalidArguments() throws CommandException {
        handler.registerCommands(new InvalidArguments());
    }

    @Test(expected = CommandException.class)
    public void tooManyArguments() throws CommandException {
        handler.registerCommands(new TooManyArguments());
    }

    @Test(expected = CommandException.class)
    public void minHigherThanMax() throws CommandException {
        handler.registerCommands(new MinHigherThanMax());
    }

    /**
     * Completed valid command
     */
    private class ValidCommand {

        @Command(
                command = "test command",
                aliases = {"ctest", "wat"},
                description = "A test command!",
                max = 10,
                min = 5,
                permission = "test.command",
                usage = ""
        )
        public void command(CommandContext context) {
        }

    }

    /**
     * Should fail because of missing parameters
     */
    private class InvalidArguments {

        @Command(command = "invalid-arguments")
        public void command() {
        }

    }

    /**
     * Should fail because of too many arguments
     */
    private class TooManyArguments {

        @Command(command = "too-many-arguments")
        public void command(CommandContext context, String somestring) {
        }

    }

    /**
     * Should fail because max > min
     */
    private class MinHigherThanMax {

        @Command(command = "min-higher-than-max", min = 10, max = 1)
        public void command(CommandContext context) {
        }

    }

}
