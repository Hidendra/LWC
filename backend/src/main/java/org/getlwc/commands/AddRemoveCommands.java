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
            command = "lwc modify",
            usage = "[access] [-]<role name>( [access] [-]<role name>...)",
            permission = "lwc.modify",
            aliases = {"cmodify"},
            min = 1,
            accepts = SenderType.PLAYER
    )
    public void modify(CommandContext context) {
        final Player player = (Player) context.getCommandSender();
        final String[] parsed = StringUtils.split(context.getArguments());

        player.sendMessage("&eClick on a protection to apply modifications.");

        player.onAnyInteract(new ProtectionEventNotifier() {
            @Override
            public boolean call(ProtectionEvent event) {
                Protection protection = event.getProtection();

                // the access role to apply to the next matched role
                ProtectionAccess access = null;

                for (int i = 0; i < parsed.length; i ++) {
                    if (i + 1 > parsed.length) {
                        break;
                    }

                    String curr = parsed[i];

                    if (access == null) {
                        // are they removing ?
                        if (curr.startsWith("-")) {
                            curr = curr.substring(1);

                            //
                            Role role = engine.getRoleManager().matchAndCreateRoleByName(protection, curr, ProtectionAccess.NONE);

                            Role delete = null;
                            for (Role protectionRole : protection.getRoles()) {
                                if (role.getType() == protectionRole.getType() && role.getRoleName().equalsIgnoreCase(protectionRole.getRoleName())) {
                                    delete = protectionRole;
                                    break;
                                }
                            }

                            if (delete != null) {
                                player.sendMessage("&eRemoving role: &7" + curr);
                                protection.removeRole(delete);
                            } else {
                                player.sendMessage("&4Protection does not contain role matching: &e" + curr);
                            }

                            continue;
                        }

                        // attempt to match an access level
                        access = ProtectionAccess.match(curr);

                        if (access == null) {
                            player.sendMessage("&4Unknown symbol: &7" + curr + "&4 (Should be one of: &7" + ProtectionAccess.USABLE_ACCESS_LEVELS + "&4)");
                        }

                        if (access != null && !ProtectionAccess.USABLE_ACCESS_LEVELS.contains(access)) {
                            player.sendMessage("&4Protection access level &7" + access + "&4 is not allowed.");
                            access = null;
                        }
                    } else {
                        if (access == null) {
                            player.sendMessage("&4Invalid access level for name \"" + curr + "\". Valid levels: &7" + ProtectionAccess.USABLE_ACCESS_LEVELS);
                            return true;
                        }

                        Role role = engine.getRoleManager().matchAndCreateRoleByName(protection, curr, access);

                        if (role == null) {
                            player.sendMessage("&4\"" + curr + "\" does not match any usable roles");
                            return true;
                        }

                        protection.addRole(role);
                        protection.save();
                        player.sendMessage("&2Added a " + role.getClass().getSimpleName() + " for \"" + curr + "\" to the protection with access level " + access + " successfully!");
                    }
                }


                return true;
            }
        });
    }

}
