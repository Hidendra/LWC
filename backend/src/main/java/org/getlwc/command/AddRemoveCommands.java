package org.getlwc.command;

import org.getlwc.Engine;
import org.getlwc.entity.Player;
import org.getlwc.event.events.ProtectionEvent;
import org.getlwc.event.notifiers.ProtectionEventNotifier;
import org.getlwc.model.Protection;
import org.getlwc.provider.ProtectionProvider;
import org.getlwc.role.ProtectionRole;
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
            aliases = {"cmodify", "cadd"},
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

                ProtectionRole.Access playerAccess = protection.getAccess(player);

                if (playerAccess.ordinal() < ProtectionRole.Access.MANAGER.ordinal()) {
                    player.sendTranslatedMessage("&4Access denied.");
                    return true;
                }

                // the access role to apply to the next matched role
                ProtectionRole.Access access = null;

                for (int i = 0; i < parsed.length; i++) {
                    if (i + 1 > parsed.length) {
                        break;
                    }

                    String token = parsed[i];

                    if (access == null) {
                        // are they removing ?
                        if (token.startsWith("-")) {
                            token = token.substring(1);

                            ProtectionProvider<ProtectionRole> provider = engine.getProtectionManager().getRoleManager().match(token);
                            ProtectionRole role = provider != null ? provider.create(protection) : null;

                            if (role == null) {
                                player.sendTranslatedMessage("&4\"{0}\" does not match any usable roles", token);
                                return true;
                            }

                            role.setName(token);

                            ProtectionRole delete = null;
                            for (ProtectionRole protectionRole : protection.getRoles()) {
                                if (role.getType().equals(protectionRole.getType()) && role.getName().equalsIgnoreCase(protectionRole.getName())) {
                                    delete = protectionRole;
                                    break;
                                }
                            }

                            if (role.getAccess() == ProtectionRole.Access.OWNER && playerAccess != ProtectionRole.Access.OWNER) {
                                player.sendTranslatedMessage("&4Only owners can remove other owners.");
                                continue;
                            }

                            if (role.getAccess() == ProtectionRole.Access.MANAGER && playerAccess != ProtectionRole.Access.OWNER) {
                                player.sendTranslatedMessage("&4Only owners can remove managers.");
                                continue;
                            }

                            if (delete != null) {
                                player.sendTranslatedMessage("&eRemoving role: &7{0}", token);
                                protection.removeRole(delete);
                            } else {
                                player.sendTranslatedMessage("&4Protection does not contain role matching: &e{0}", token);
                            }

                            continue;
                        }

                        // attempt to match an access level
                        access = ProtectionRole.Access.match(token);

                        if (access == null) {
                            access = ProtectionRole.Access.MEMBER;

                            ProtectionProvider<ProtectionRole> provider = engine.getProtectionManager().getRoleManager().match(token);
                            ProtectionRole role = provider != null ? provider.create(protection) : null;

                            if (role == null) {
                                player.sendTranslatedMessage("&4\"{0}\" does not match any usable roles", token);
                                return true;
                            }

                            role.setName(token);

                            ProtectionRole existing = protection.getRole(role.getType(), role.getName());

                            if (existing != null) {
                                existing.setProtectionAccess(role.getAccess());
                                existing.save();
                                player.sendTranslatedMessage("&2Changed {0} to {1} successfully!", existing.getName(), existing.getAccess());
                            } else {
                                role.setProtectionAccess(access);
                                protection.addRole(role);
                                protection.save();
                                player.sendTranslatedMessage("&2Added a {0} for \"{1}\" to the protection with access level {2} successfully!", role.getClass().getSimpleName(), token, access);
                            }
                        }

                        if (!ProtectionRole.Access.USABLE_ACCESS_LEVELS.contains(access)) {
                            player.sendTranslatedMessage("&4Protection access level &7{0}&4 is not allowed.", access);
                            access = null;
                            i++;
                        }

                        if (access == ProtectionRole.Access.OWNER && playerAccess != ProtectionRole.Access.OWNER) {
                            player.sendTranslatedMessage("&4Only owners can add other owners.");
                            access = null;
                            i++;
                        }

                        if (access == ProtectionRole.Access.MANAGER && playerAccess != ProtectionRole.Access.OWNER) {
                            player.sendTranslatedMessage("&4Only owners can add managers.");
                            access = null;
                            i++;
                        }
                    } else {
                        ProtectionProvider<ProtectionRole> provider = engine.getProtectionManager().getRoleManager().match(token);
                        ProtectionRole role = provider != null ? provider.create(protection) : null;

                        if (role == null) {
                            player.sendTranslatedMessage("&4\"{0}\" does not match any usable roles", token);
                            return true;
                        }

                        role.setName(token);

                        ProtectionRole existing = protection.getRole(role.getType(), role.getName());

                        if (existing != null) {
                            existing.setProtectionAccess(role.getAccess());
                            existing.save();
                            player.sendTranslatedMessage("&2Changed {0} to {1} successfully!", existing.getName(), existing.getAccess());
                        } else {
                            role.setProtectionAccess(access);
                            protection.addRole(role);
                            protection.save();
                            player.sendTranslatedMessage("&2Added a {0} for \"{1}\" to the protection with access level {2} successfully!", role.getClass().getSimpleName(), token, access);
                        }
                    }
                }


                return true;
            }
        });
    }

}
