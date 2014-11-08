package org.getlwc.content.role;

import org.getlwc.Engine;
import org.getlwc.role.RoleCreationException;
import org.getlwc.role.RoleFactory;

import java.util.UUID;

import static org.getlwc.I18n._;

public class PlayerRoleFactory implements RoleFactory<PlayerRole> {

    private Engine engine;

    public PlayerRoleFactory(Engine engine) {
        this.engine = engine;
    }

    @Override
    public PlayerRole createFromValue(String value) throws RoleCreationException {
        UUID uuid;

        try {
            uuid = UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            // TODO attempt to match player name
            throw new RoleCreationException(_("Could not find UUID for player: {0}", value));
        }

        return new PlayerRole(uuid);
    }

}
