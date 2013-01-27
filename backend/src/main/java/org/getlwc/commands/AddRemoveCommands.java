package org.getlwc.commands;

import org.getlwc.Engine;
import org.getlwc.ProtectionAccess;
import org.getlwc.Role;
import org.getlwc.command.Command;
import org.getlwc.command.CommandContext;
import org.getlwc.command.SenderType;
import org.getlwc.entity.Player;
import org.getlwc.event.events.ProtectionEvent;
import org.getlwc.event.notifiers.ProtectionEventNotifier;
import org.getlwc.model.Protection;
import org.getlwc.util.StringUtils;

public class AddRemoveCommands {

    /**
     * The engine object
     */
    private Engine engine;

    public AddRemoveCommands(Engine engine) {
        this.engine = engine;
    }

    @Command(
            command = "lwc add",
            usage = "<access> <role name>( <access> <role name>...)",
            permission = "lwc.modify.add",
            aliases = {"cadd"},
            min = 2,
            accepts = SenderType.PLAYER
    )
    public void add(CommandContext context) {
        final Player player = (Player) context.getCommandSender();
        final String[] parsed = StringUtils.split(context.getArguments());

        if (parsed.length % 2 != 0) {
            player.sendMessage("Arguments need to be a multiple of 2");
            return;
        }

        player.sendMessage("&eClick on a protection to add your role(s).");

        player.onAnyInteract(new ProtectionEventNotifier() {
            @Override
            public boolean call(ProtectionEvent event) {
                Protection protection = event.getProtection();

                for (int i = 0; i < parsed.length; i += 2) {
                    if (i + 1 >= parsed.length) {
                        break;
                    }

                    String type = parsed[i];
                    String name = parsed[i + 1];

                    ProtectionAccess access = ProtectionAccess.match(type);

                    if (access == null) {
                        player.sendMessage("&4Invalid access level for name \"" + name + "\". Valid levels: &7" + ProtectionAccess.USABLE_ACCESS_LEVELS);
                        return true;
                    }

                    Role role = engine.getRoleManager().matchAndCreateRoleByName(protection, name, access);

                    if (role == null) {
                        player.sendMessage("&4\"" + name + "\" does not match any usable roles");
                        return true;
                    }

                    protection.addRole(role);
                    protection.save();
                    player.sendMessage("&2Added a " + role.getClass().getSimpleName() + " for \"" + name + "\" to the protection with access level " + access + " successfully!");
                }


                return true;
            }
        });
    }

}
