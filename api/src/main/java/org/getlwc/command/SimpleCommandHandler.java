/*
 * Copyright (c) 2011-2013 Tyler Blair
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

package org.getlwc.command;

import org.getlwc.Engine;
import org.getlwc.cache.LRUCache;
import org.getlwc.entity.Player;
import org.getlwc.util.StringUtils;
import org.getlwc.util.Tuple;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleCommandHandler implements CommandHandler {

    /**
     * The max amount of cached entries to keep
     */
    private final static int CACHE_SIZE = 50000;

    /**
     * Engine instance
     */
    private final Engine engine;

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

    /**
     * A set of the known base commands commands can use
     */
    private final Set<String> baseCommands = new HashSet<String>();

    public SimpleCommandHandler(Engine engine) {
        this.engine = engine;
    }

    /**
     * {@inheritDoc}
     */
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

        String[] arguments = StringUtils.split(context.getArguments());

        // try the full command first
        if (arguments.length > 1) {
            // Second depth
            String key = (commandName + " " + arguments[0] + " " + arguments[1]).trim();

            // Try it
            if ((found = tryFindCommand(key)) != null) {
                executeCommand(found, context);
                return true;
            }
        }

        // Try only the first argument
        if (arguments.length > 0) {

            // Create the key
            String key = (commandName + " " + arguments[0]).trim();

            // Try it
            if ((found = tryFindCommand(key)) != null) {
                executeCommand(found, context);
                return true;
            }
        }

        // Fallback to the base command
        if ((found = tryFindCommand(commandName)) != null) {
            executeCommand(found, context);
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public List<Command> registerCommands(Object object) throws CommandException {
        List<Command> registered = new ArrayList<Command>();
        Class<?> clazz = object.getClass();

        // Iterate through all of the methods
        // We are only looking for annotated methods
        for (Method method : clazz.getMethods()) {
            // Check for the command annotation
            if (method.isAnnotationPresent(Command.class)) {

                // verify arguments
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length != 1 || parameters[0] != CommandContext.class) {
                    if (parameters.length == 0) {
                        throw new CommandException("Command method \"" + method.getName() + "\" has too few parameters!");
                    } else if (parameters.length == 1) {
                        throw new CommandException("Command method \"" + method.getName() + "\" has too many parameters!");
                    } else {
                        throw new CommandException("Command method \"" + method.getName() + "\" has invalid parameters!");
                    }
                }

                // Grab the annotation instance
                Command command = method.getAnnotation(Command.class);

                checkCommand(command);

                // Create the tuple
                Tuple<Command, Method> tuple = new Tuple<Command, Method>(command, method);

                // Add it to the commands
                String normalized = normalizeCommand(command.command());
                commands.put(normalized, tuple);
                postRegisterCommand(normalized, command);

                // Register all of the aliases
                for (String alias : command.aliases()) {
                    normalized = normalizeCommand(alias);
                    commands.put(normalized, tuple);
                    postRegisterCommand(normalized, command);
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
     * Called after a command has been registered
     * @param normalized
     */
    private void postRegisterCommand(String normalized, Command command) {
        int indexOfSpace = normalized.indexOf(' ');

        if (indexOfSpace != -1) {
            normalized = normalized.substring(0, indexOfSpace);
        }

        if (!baseCommands.contains(normalized)) {
            if (engine != null) {
                engine.getServerLayer().onRegisterBaseCommand(normalized, command);
            }
            baseCommands.add(normalized);
        }
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

        // First check permissions
        if (!command.permission().isEmpty()) {
            CommandSender sender = context.getCommandSender();

            if (!sender.hasPermission(command.permission())) {
                // First tell them they do not have permission
                sender.sendTranslatedMessage("&4You do not have permission to use this command.");

                // Now send the command's help
                sendHelp(command, context.getCommandSender());
                return;
            }
        }

        // Grab the instance as well
        Object instance = instances.get(command);

        // First fix the command name if it needs to be fixed
        // If we used the command /lwc admin clear, admin clear will be the arguments
        // We want the command to instead be "lwc admin clear"
        String[] realCommandNameArray = command.command().split(" ");

        if (realCommandNameArray.length > 1) {
            // compare the base command (aliases will never have the same base command)
            if (realCommandNameArray[0].equals(context.getCommand().split(" ")[0])) {
                // They are the same, so it's safe to set it the same as the annotation
                context.setCommand(command.command());
            }
        }

        // Fix the arguments
        String[] commandNameArray = context.getCommand().split(" ");

        // If it's greater than 1 it requires a swift fixing
        if (commandNameArray.length > 1) {
            // Get the old arguments
            // Basically we want to join the arguments starting at commandNameArray.length - 1 (guaranteed to be at least 1)
            String[] argumentsArray = context.getArgumentsArray();

            // Now join it using the new starting index
            String joined = StringUtils.join(argumentsArray, commandNameArray.length - 1);

            // Good, good, now we can set it to the context
            context.setArguments(joined);
        }

        // verify accepts()
        try {
            verifySenderType(command, context);
        } catch (CommandException e) {
            context.getCommandSender().sendMessage("&4" + e.getMessage());
            return;
        }

        // Verify the command
        try {
            // Verify argument lengths
            verifyMinimumArguments(command, context);
            verifyMaximumArguments(command, context);
        } catch (CommandException e) {
            // Invalid!
            sendHelp(command, context.getCommandSender());
            return;
        }

        // Now just execute the method
        try {
            method.invoke(instance, context);
        } catch (InvocationTargetException e) {
            throw new CommandException(command.command() + " threw an exception!", e);
        } catch (IllegalAccessException e) {
            throw new CommandException(command.command() + " threw an exception!", e);
        }
    }

    /**
     * Send a command's help to the given command sender
     *
     * @param command
     * @param sender
     */
    private void sendHelp(Command command, CommandSender sender) {
        // Header
        sender.sendTranslatedMessage("&2=== &6/{0} &2===", command.command());

        // Usage
        sendUsage(command, sender);

        // Description
        sendDescription(command, sender);

        // Aliases
        sendAliases(command, sender);
    }

    /**
     * Send the command's usages to the given command sender
     *
     * @param command
     * @param sender
     */
    private void sendUsage(Command command, CommandSender sender) {
        sender.sendTranslatedMessage("&2Usage:       &6/{0} {1}", command.command(), command.usage());
    }

    /**
     * Send a command's description to the player
     *
     * @param command
     * @param sender
     */
    private void sendDescription(Command command, CommandSender sender) {
        String description = command.description();
        String permission = command.permission();

        if (!description.isEmpty()) {
            sender.sendTranslatedMessage("&2Description: &6{0}",  description);
        }

        if (!permission.isEmpty()) {
            sender.sendTranslatedMessage("&2Permission:  &6{0}",  permission);
        }
    }

    /**
     * Send all of the command's aliases to the sender
     *
     * @param command
     * @param sender
     */
    private void sendAliases(Command command, CommandSender sender) {
        String text = "";

        // Add aliases only if some are defined
        if (command.aliases().length > 0) {
            for (String alias : command.aliases()) {
                text += "&b/" + alias + "&f, ";
            }

            // Remove the trailing comma
            text = text.substring(0, text.length() - 2);
        }

        sender.sendTranslatedMessage("&2Aliases:     {0}", text);
    }

    /**
     * Verify the command's sender type (accepts())
     *
     * @param command
     * @param context
     * @throws CommandException
     */
    private void verifySenderType(Command command, CommandContext context) throws CommandException {
        CommandSender sender = context.getCommandSender();
        SenderType accepts = command.accepts();

        if (accepts == SenderType.CONSOLE && sender instanceof Player) {
            throw new CommandException("Only the console can use this command");
        } else if (accepts == SenderType.PLAYER && !(sender instanceof Player)) {
            throw new CommandException("Only a player can use this command");
        }
    }

    /**
     * Verify the minimum number of arguments required
     *
     * @param command
     * @param context
     * @throws CommandException
     */
    private void verifyMinimumArguments(Command command, CommandContext context) throws CommandException {
        int min = command.min();

        if (min > 0) {
            if (!(context.getArgumentsArray().length >= min)) {
                throw new CommandException("Number of arguments does not meet the minimum required");
            }
        }
    }

    /**
     * Verify the maximum number of arguments required
     *
     * @param command
     * @param context
     * @throws CommandException
     */
    private void verifyMaximumArguments(Command command, CommandContext context) throws CommandException {
        int max = command.max();

        if (max >= 0) {
            if (!(context.getArgumentsArray().length <= max)) {
                throw new CommandException("Number of arguments does not meet the maximum requirement!");
            }
        }
    }

    /**
     * Used when parsing commands from methods to ensure the command is valid
     *
     * @param command
     * @throws CommandException
     */
    private void checkCommand(Command command) throws CommandException {
        // check for min > max
        if (command.max() != -1 && command.min() > command.max()) {
            throw new CommandException("Command's minimum must not be higher than its maximum");
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
