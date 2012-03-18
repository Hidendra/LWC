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

import com.griefcraft.cache.LRUCache;
import com.griefcraft.util.Tuple;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleCommandHandler implements CommandHandler {

    /**
     * The max amount of cached entries to keep
     */
    private final static int CACHE_SIZE = 50000;

    /**
     * A map of all of the commands. It is keyed using the command's command. The method's instance
     * is stored in a separate map.
     */
    private final Map<String, Tuple<Command, Method>> commands = new HashMap<String, Tuple<Command, Method>>();

    /**
     * A map of all of the command instances
     */
    private final Map<Command, Object> instances = new HashMap<Command, Object>();

    /**
     * A cache of accepted commands
     */
    private final LRUCache<String, Tuple<Command, Method>> cache = new LRUCache<String, Tuple<Command, Method>>(CACHE_SIZE);

    public boolean handleCommand(CommandContext context) throws CommandException {
        Tuple<Command, Method> found;
        
        // Normalize the command name
        String commandName = normalizeCommand(context.getCommand());
        System.out.println("Used command: \"" + commandName + "\"");
        
        String fullKey = (commandName + " " + context.getArguments()).trim();

        // First try the full command used
        if ((found = tryFindCommand(fullKey)) != null) {
            executeCommand(found, context);
            return true;
        }
        
        // Try the arguments
        // We only want to
        if (context.getArguments().length() > 0) {
            String[] arguments = context.getArguments().split(" ");

            // Create the key
            String key = (commandName + " " + arguments[0]).trim();

            // Try it
            if ((found = tryFindCommand(key)) != null) {
                executeCommand(found, context);
                return true;
            }

            if (arguments.length > 1) {
                // Second depth
                key = (commandName + " " + arguments[0] + " " + arguments[1]).trim();

                // Try it
                if ((found = tryFindCommand(key)) != null) {
                    executeCommand(found, context);
                    return true;
                }
            }
        }

        // Fallback to the base command
        if ((found = tryFindCommand(commandName)) != null) {
            executeCommand(found, context);
            return true;
        }

        return false;
    }

    public List<Command> registerCommands(Object object) {
        List<Command> registered = new ArrayList<Command>();
        Class<?> clazz = object.getClass();

        // Iterate through all of the methods
        // We are only looking for annotated methods
        for (Method method : clazz.getMethods()) {
            // Check for the command annotation
            if (method.isAnnotationPresent(Command.class)) {

                // Grab the annotation instance
                Command command = method.getAnnotation(Command.class);
                
                // Create the tuple
                Tuple<Command, Method> tuple = new Tuple<Command, Method>(command, method);

                // Add it to the commands
                commands.put(normalizeCommand(command.command()), tuple);
                
                // Register all of the aliases
                for (String alias : command.aliases()) {
                    commands.put(normalizeCommand(alias), tuple);
                }
                
                // Add the instance
                instances.put(command, object);
                
                // Add it to the registered list
                registered.add(command);
            }
        }

        return registered;
    }

    /**
     * Execute the command using the given command context
     *
     * @param pair
     * @param context
     */
    private void executeCommand(Tuple<Command, Method> pair, CommandContext context) throws CommandException {
        Command command = pair.first();
        Method method = pair.second();
        
        // Grab the instance as well
        Object instance = instances.get(command);

        // Now just execute the method
        try {
            method.invoke(instance, context);
        } catch (InvocationTargetException e) {
            throw new CommandException(command.command() + " threw an exception!",  e);
        } catch (IllegalAccessException e) {
            throw new CommandException(command.command() + " threw an exception!",  e);
        }
    }

    /**
     * Try and handle the given command key
     *
     * @param tryCommand
     * @return the command instance found
     */
    private Tuple<Command, Method> tryFindCommand(String tryCommand) {
        // Check the cache
        Tuple<Command, Method> cached = cache.get(tryCommand);

        if (cached != null) {
            // Bingo!
            return cached;
        }

        // Try the commands map
        Tuple<Command, Method> matched = commands.get(tryCommand);

        // Found it?
        if (matched != null) {
            // Cache it!
            cache.put(tryCommand, matched);

            // Return it!
            return matched;
        }

        return null;
    }

    /**
     * Normalize a command name
     *
     * @param command
     * @return
     */
    private String normalizeCommand(String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        return command.trim().toLowerCase();
    }


}
