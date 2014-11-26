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
        super();

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
