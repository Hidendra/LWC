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
            description = "Add/remove/modify access roles that are on a protection",
            usage = "[access] [-]<role name>( [access] [-]<role name>...)",
            permission = "lwc.modify",
            aliases = {"cmodify"},
            min = 1,
            accepts = SenderType.PLAYER
    )
    public void modify(CommandContext context) {
        final Player player = (Player) context.getCommandSender();
        final String[] parsed = StringUtils.split(context.getArguments());

        player.sendTranslatedMessage("&eClick on a protection to apply modifications.");

        player.onAnyInteract(new ProtectionEventNotifier() {
            @Override
            public boolean call(ProtectionEvent event) {
                Protection protection = event.getProtection();

                ProtectionAccess currAccess = protection.getAccess(player);

                if (currAccess.ordinal() < ProtectionAccess.MANAGER.ordinal()) {
                    player.sendTranslatedMessage("&4Access denied.");
                    return true;
                }

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

                            if (role.getRoleAccess() == ProtectionAccess.OWNER && currAccess != ProtectionAccess.OWNER) {
                                player.sendTranslatedMessage("&4Only owners can remove other owners.");
                                continue;
                            }

                            if (role.getRoleAccess() == ProtectionAccess.MANAGER && currAccess != ProtectionAccess.OWNER) {
                                player.sendTranslatedMessage("&4Only owners can remove managers.");
                                continue;
                            }

                            if (delete != null) {
                                player.sendTranslatedMessage("&eRemoving role: &7{0}", curr);
                                protection.removeRole(delete);
                            } else {
                                player.sendTranslatedMessage("&4Protection does not contain role matching: &e{0}", curr);
                            }

                            continue;
                        }

                        // attempt to match an access level
                        access = ProtectionAccess.match(curr);

                        if (access == null) {
                            player.sendTranslatedMessage("&4Unknown symbol: &7{0}&4 (Should be one of: &7{1}&4)", curr, ProtectionAccess.USABLE_ACCESS_LEVELS);
                        }

                        if (access != null && !ProtectionAccess.USABLE_ACCESS_LEVELS.contains(access)) {
                            player.sendTranslatedMessage("&4Protection access level &7{0}&4 is not allowed.", access);
                            access = null;
                        }

                        if (access == ProtectionAccess.OWNER && currAccess != ProtectionAccess.OWNER) {
                            player.sendTranslatedMessage("&4Only owners can add other owners.");
                            access = null;
                        }

                        if (access == ProtectionAccess.MANAGER && currAccess != ProtectionAccess.OWNER) {
                            player.sendTranslatedMessage("&4Only owners can add managers.");
                            access = null;
                        }
                    } else {
                        Role role = engine.getRoleManager().matchAndCreateRoleByName(protection, curr, access);

                        if (role == null) {
                            player.sendTranslatedMessage("&4\"{0}\" does not match any usable roles", curr);
                            return true;
                        }

                        protection.addRole(role);
                        protection.save();
                        player.sendTranslatedMessage("&2Added a {0} for \"{1}\" to the protection with access level {2} successfully!", role.getClass().getSimpleName(), curr, access);
                    }
                }


                return true;
            }
        });
    }

}
