package org.getlwc.attribute;

import org.getlwc.AccessProvider;
import org.getlwc.Engine;
import org.getlwc.entity.Player;
import org.getlwc.model.Protection;
import org.getlwc.role.ProtectionRole;

import java.util.Set;

public class PasswordAttribute extends HashedStringAttribute implements AccessProvider {

    public PasswordAttribute(Engine engine, String name, String value) {
        super(engine, name, value);
    }

    public PasswordAttribute(Engine engine, String name) {
        super(engine, name);
    }

    public ProtectionRole.Access getAccess(Protection protection, Player player) {
        if (player.hasAttribute("password_authorized")) {
            Set<Integer> authorized = (Set<Integer>) player.getAttribute("password_authorized");

            if (authorized.contains(protection.getId())) {
                return ProtectionRole.Access.MEMBER;
            }
        }

        player.sendTranslatedMessage("&4This protection is locked by a password.\n&4To enter the password, use: &3/cunlock <password>");
        player.setAttribute("password_request", this);
        player.setAttribute("password_protection_id", protection.getId());
        return ProtectionRole.Access.EXPLICIT_DENY;
    }

}
