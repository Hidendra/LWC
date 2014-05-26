package org.getlwc.role.provider;

import org.getlwc.Engine;
import org.getlwc.model.Protection;
import org.getlwc.provider.ProtectionProvider;
import org.getlwc.role.PlayerRole;
import org.getlwc.role.ProtectionRole;

public class PlayerRoleProvider implements ProtectionProvider<ProtectionRole> {

    private final Engine engine;

    public PlayerRoleProvider(Engine engine) {
        this.engine = engine;
    }

    @Override
    public ProtectionRole create(Protection protection) {
        return new PlayerRole(engine, protection);
    }

    @Override
    public boolean shouldProvide(String input) {
        return false; // it is the default
    }

}
