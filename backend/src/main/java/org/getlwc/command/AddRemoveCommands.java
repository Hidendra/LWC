package org.getlwc.command;

import org.getlwc.Engine;
import org.getlwc.entity.Player;
import org.getlwc.event.events.ProtectionEvent;
import org.getlwc.event.notifiers.ProtectionEventNotifier;
import org.getlwc.model.Protection;
import org.getlwc.provider.ProtectionProvider;
import org.getlwc.role.ProtectionRole;

public class AddRemoveCommands {

    private Engine engine;

    public AddRemoveCommands(Engine engine) {
        this.engine = engine;
    }

    @Command(
            command = "lwc modify",
            description = "Add/remove/modify access roles that are on a protection",
            usage = "[-]<role name> [access]",
            permission = "lwc.modify",
            aliases = {"cmodify", "cadd"},
            min = 1,
            accepts = SenderType.PLAYER
    )
    public void modify(CommandContext context) {
        final Player player = (Player) context.getCommandSender();

        String baseRoleName = context.getArgument(1);
        String accessName = context.getArgument(2, "member");
        final ProtectionRole.Access access = ProtectionRole.Access.fromString(accessName);
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

                ProtectionRole.Access playerAccess = protection.getAccess(player);

                if (playerAccess.ordinal() < ProtectionRole.Access.MANAGER.ordinal()) {
                    player.sendTranslatedMessage("&4Access denied.");
                    return true;
                }

                ProtectionProvider<ProtectionRole> provider = engine.getProtectionManager().getRoleManager().match(roleName);
                ProtectionRole role = provider != null ? provider.create(protection) : null;

                if (role == null) {
                    player.sendTranslatedMessage("&4Role identifier not recognized: {0}", roleName);
                    return true;
                }

                role.setName(roleName);

                if (isRemoving) {
                    ProtectionRole toDelete = protection.getRole(role.getType(), role.getName());

                    if (role.getAccess() == ProtectionRole.Access.OWNER && playerAccess != ProtectionRole.Access.OWNER) {
                        player.sendTranslatedMessage("&4Only owners can remove other owners.");
                    } else if (role.getAccess() == ProtectionRole.Access.MANAGER && playerAccess != ProtectionRole.Access.OWNER) {
                        player.sendTranslatedMessage("&4Only owners can remove managers.");
                    } else if (toDelete != null) {
                        player.sendTranslatedMessage("&2Removed successfully.", toDelete.getName());
                        protection.removeRole(toDelete);
                    } else {
                        player.sendTranslatedMessage("&4Protection does not contain role matching: &e{0}", roleName);
                    }
                } else { // either adding or modifying an existing entry
                    ProtectionRole existing = protection.getRole(role.getType(), role.getName());

                    if (existing != null) {
                        if (role.getAccess() == ProtectionRole.Access.OWNER && playerAccess != ProtectionRole.Access.OWNER) {
                            player.sendTranslatedMessage("&4Only owners can modify other owners.");
                        } else if (role.getAccess() == ProtectionRole.Access.MANAGER && playerAccess != ProtectionRole.Access.OWNER) {
                            player.sendTranslatedMessage("&4Only owners can modify managers.");
                        } else {
                            existing.setProtectionAccess(access);
                            protection.addRole(existing);
                            protection.save();

                            player.sendTranslatedMessage("&2Changed successfully.", existing.getName(), existing.getAccess());
                        }
                    } else {
                        role.setProtectionAccess(access);
                        protection.addRole(role);
                        protection.save();

                        player.sendTranslatedMessage("&2Added successfully.", role.getClass().getSimpleName(), role.getName(), role.getAccess());
                    }
                }

                return true;
            }
        });
    }

}
