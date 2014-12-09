/**
 * Copyright (c) 2011-2014 Tyler Blair
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
package org.getlwc.granite.experimental;

import org.getlwc.command.CommandContext;
import org.getlwc.command.CommandSender;
import org.getlwc.command.ConsoleCommandSender;
import org.getlwc.granite.GranitePlugin;
import org.getlwc.util.StringUtils;
import org.granitemc.granite.reflect.CommandComposite;
import org.granitemc.granite.reflect.GraniteServerComposite;
import org.granitemc.granite.reflect.ReflectionUtils;
import org.granitemc.granite.reflect.composite.Hook;
import org.granitemc.granite.reflect.composite.HookListener;
import org.granitemc.granite.utils.Mappings;
import org.granitemc.granite.utils.MinecraftUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GraniteCommandComposite extends CommandComposite {

    public GraniteCommandComposite(final GranitePlugin plugin) {
        addHook("executeCommand", new HookListener() {
            @Override
            public Object activate(Object self, Method method, Method proxyCallback, Hook hook, Object[] args) throws InvocationTargetException, IllegalAccessException {
                if (hook.getWasHandled()) {
                    return 0;
                }

                String[] commandArgs = ((String) args[1]).split(" ");
                if (commandArgs[0].startsWith("/")) {
                    commandArgs[0] = commandArgs[0].substring(1);
                }

                CommandSender sender = plugin.wrapCommandSender((org.granitemc.granite.api.command.CommandSender) MinecraftUtils.wrap(args[0]));
                CommandContext.Type type = (sender instanceof ConsoleCommandSender) ? CommandContext.Type.SERVER : CommandContext.Type.PLAYER;

                String baseCommand = commandArgs[0];
                String arguments = StringUtils.join(commandArgs, 1);

                try {
                    boolean result = plugin.getEngine().getCommandHandler().handleCommand(new CommandContext(type, sender, baseCommand, arguments));

                    if (result) {
                        hook.setWasHandled(true);
                    }
                } catch (org.getlwc.command.CommandException e) {
                    plugin.getEngine().getConsoleSender().sendMessage("An error was encountered while processing a command: {0}", e.getMessage());
                    e.printStackTrace();

                    sender.sendMessage("&4[LWC] An internal error occurred while processing this command");
                }

                return 0;
            }
        });
    }

    /**
     * Inject this composite into the server
     */
    public void injectComposite() {
        Field commandManagerField = Mappings.getField("MinecraftServer", "commandManager");
        ReflectionUtils.forceStaticAccessible(commandManagerField);

        try {
            commandManagerField.set(GraniteServerComposite.instance.parent, this.parent);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
