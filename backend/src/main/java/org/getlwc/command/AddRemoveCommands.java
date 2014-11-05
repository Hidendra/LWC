package org.getlwc.command;

import org.getlwc.Engine;
import org.getlwc.entity.Player;
import org.getlwc.event.events.ProtectionEvent;
import org.getlwc.event.notifiers.ProtectionEventNotifier;
import org.getlwc.model.Protection;
import org.getlwc.provider.ProtectionProvider;
import org.getlwc.role.AbstractRole;
import org.getlwc.role.PlayerRole;
import org.getlwc.role.Role;

public class AddRemoveCommands {

    private Engine engine;

    public AddRemoveCommands(Engine engine) {
        this.engine = engine;
    }

    @Command(
            command = "lwc modify",
            description = "Add/remove/modify access roles that are on a protection",
            usage = "<player name> [access] | <type> <name> [access]",
            permission = "lwc.modify",
            aliases = {"cmodify", "cadd"},
            min = 1,
            max = 3,
            accepts = SenderType.PLAYER
    )
    public void modify(CommandContext context) {
        final Player player = (Player) context.getCommandSender();

        final String baseRoleType;
        String baseRoleName;
        String accessName;

        if (context.getArgumentsArray().length == 3) {
            baseRoleType = context.getArgument(1).toLowerCase();
            baseRoleName = context.getArgument(2);
            accessName = context.getArgument(3, "member");
        } else {
            baseRoleType = PlayerRole.TYPE; // default w/ no args
            baseRoleName = context.getArgument(1);
            accessName = context.getArgument(2, "member");
        }

        final Protection.Access access = Protection.Access.fromString(accessName);
        final boolean isRemoving = baseRoleName.startsWith("-");
        final String roleName = isRemoving ? baseRoleName.substring(1) : baseRoleName;

        if (access == null) {
            player.sendTranslatedMessage("&4Unknown access level: {0}", accessName);
            return;
        }

        if (isRemoving) {
            player.sendTranslatedMessage("&fClick on a protection to apply the modification: &4remove &e{0}&f", roleName);
        } else {
            player.sendTranslatedMessage("&fClick on a protection to apply the modification: &2add/change &e{0}&f to &e{1}", roleName, access);
        }

        player.onAnyInteract(new ProtectionEventNotifier() {
            @Override
            public boolean call(ProtectionEvent event) {
                Protection protection = event.getProtection();

                Protection.Access playerAccess = protection.getAccess(player);

                if (playerAccess.ordinal() < Protection.Access.MANAGER.ordinal()) {
                    player.sendTranslatedMessage("&4Access denied.");
                    return true;
                }

                Role role = engine.getProtectionManager().getRoleRegistry().loadRole(baseRoleType, roleName);

                if (role == null) {
                    player.sendTranslatedMessage("&4Role identifier not recognized: {0}", roleName);
                    return true;
                }

                if (isRemoving) {
                    Role toDelete = protection.getSimilarRole(role);

                    if (role.getAccess() == Protection.Access.OWNER && playerAccess != Protection.Access.OWNER) {
                        player.sendTranslatedMessage("&4Only owners can remove other owners.");
                    } else if (role.getAccess() == Protection.Access.MANAGER && playerAccess != Protection.Access.OWNER) {
                        player.sendTranslatedMessage("&4Only owners can remove managers.");
                    } else if (toDelete != null) {
                        player.sendTranslatedMessage("&2Removed successfully.");
                        protection.removeRole(toDelete);
                    } else {
                        player.sendTranslatedMessage("&4Protection does not contain role matching: &e{0}", roleName);
                    }
                } else { // either adding or modifying an existing entry
                    Role existing = protection.getSimilarRole(role);

                    if (existing != null) {
                        if (role.getAccess() == Protection.Access.OWNER && playerAccess != Protection.Access.OWNER) {
                            player.sendTranslatedMessage("&4Only owners can modify other owners.");
                        } else if (role.getAccess() == Protection.Access.MANAGER && playerAccess != Protection.Access.OWNER) {
                            player.sendTranslatedMessage("&4Only owners can modify managers.");
                        } else {
                            existing.setAccess(access);
                            protection.addRole(existing);
                            protection.save();

                            player.sendTranslatedMessage("&2Changed successfully.");
                        }
                    } else {
                        role.setAccess(access);
                        protection.addRole(role);
                        protection.save();

                        player.sendTranslatedMessage("&2Added successfully.");
                    }
                }

                return true;
            }
        });
    }

}
