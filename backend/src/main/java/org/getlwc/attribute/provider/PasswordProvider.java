package org.getlwc.attribute.provider;

import org.getlwc.Engine;
import org.getlwc.attribute.PasswordAttribute;
import org.getlwc.command.Command;
import org.getlwc.command.CommandContext;
import org.getlwc.command.CommandException;
import org.getlwc.command.SenderType;
import org.getlwc.entity.Player;
import org.getlwc.model.AbstractAttribute;
import org.getlwc.provider.BasicProvider;
import org.getlwc.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class PasswordProvider implements BasicProvider<AbstractAttribute> {

    public static final String NAME = "password";

    private final Engine engine;

    public PasswordProvider(Engine engine) {
        this.engine = engine;

        try {
            engine.getCommandHandler().registerCommands(this);
        } catch (CommandException e) {
            engine.getConsoleSender().sendTranslatedMessage("Failed to register password commands: {0}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public AbstractAttribute create() {
        return new PasswordAttribute(engine, NAME);
    }

    @Override
    public boolean shouldProvide(String input) {
        return input.toLowerCase().equals("password");
    }

    @Command(
            command = "lwc password",
            description = "Unlocks a protection via password if you have attempted to open one",
            permission = "lwc.password",
            aliases = {"cunlock"},
            accepts = SenderType.PLAYER,
            min = 1
    )
    public void passwordCommand(CommandContext context) {
        Player player = (Player) context.getCommandSender();
        String password = StringUtils.join(context.getArgumentsArray());

        Object request = player.getAttribute("password_request");

        if (request == null) {
            player.sendTranslatedMessage("&4You have no pending /cunlock.");
            return;
        }

        PasswordAttribute attribute = (PasswordAttribute) request;

        if (attribute.getValue().matches(password)) {
            int protectionId = (Integer) player.getAttribute("password_protection_id");
            player.sendTranslatedMessage("&2Granted access to the protection!");
            player.removeAttribute("password_request");
            player.removeAttribute("password_protection_id");

            if (!player.hasAttribute("password_authorized")) {
                player.setAttribute("password_authorized", new HashSet<Integer>());
            }

            Set<Integer> authorized = (Set<Integer>) player.getAttribute("password_authorized");
            authorized.add(protectionId);
        } else {
            player.sendTranslatedMessage("&4Invalid password.");
        }
    }

}
