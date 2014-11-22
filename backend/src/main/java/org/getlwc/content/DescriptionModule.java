package org.getlwc.content;

import org.getlwc.command.Command;
import org.getlwc.command.CommandContext;
import org.getlwc.command.SenderType;
import org.getlwc.content.component.DescriptionComponent;
import org.getlwc.entity.Player;
import org.getlwc.event.ProtectionListener;
import org.getlwc.event.ProtectionEvent;
import org.getlwc.event.protection.ProtectionLoadEvent;
import org.getlwc.event.notifiers.ProtectionEventNotifier;
import org.getlwc.model.Metadata;
import org.getlwc.model.Protection;

public final class DescriptionModule {

    /**
     * The metadata key name used to store the description
     */
    public static final String META_KEY = "description";

    @ProtectionListener
    public void bootstrap(ProtectionLoadEvent event) {
        Protection protection = event.getProtection();

        if (protection.hasMeta(META_KEY)) {
            protection.addComponent(new DescriptionComponent(protection.getMeta(META_KEY).getValue()));
        }
    }

    @Command(
            command = "lwc description",
            description = "Sets or removes a description on a protection",
            aliases = { "lwc desc" },
            accepts = SenderType.PLAYER
    )
    public void setDescription(CommandContext context) {
        final Player player = (Player) context.getCommandSender();
        final String description = context.getArguments().trim();
        final boolean removingDescription = description.isEmpty() || description.equalsIgnoreCase("none");

        if (removingDescription) {
            player.sendTranslatedMessage("Click on the protection to remove its description.");
        } else {
            player.sendTranslatedMessage("Click on the protection to set the description: &e{0}", description);
        }

        player.onAnyInteract(new ProtectionEventNotifier() {
            @Override
            public boolean call(ProtectionEvent event) {
                Protection protection = event.getProtection();

                if (removingDescription) {
                    protection.removeMeta(META_KEY);
                    protection.removeComponent(DescriptionComponent.class);
                    player.sendTranslatedMessage("&2Removed successfully.");
                } else {
                    protection.addMeta(new Metadata(META_KEY, description));
                    protection.addComponent(new DescriptionComponent(description));
                    player.sendTranslatedMessage("&2Added successfully.");
                }

                protection.save();

                return true;
            }
        });
    }

}
