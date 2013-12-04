package org.getlwc.role;

import org.getlwc.Engine;
import org.getlwc.entity.Player;
import org.getlwc.model.Protection;

public class PlayerRoleFactory implements RoleFactory {

    private Engine engine;

    public PlayerRoleFactory(Engine engine) {
        this.engine = engine;
    }

    public String getName() {
        return "lwc:rolePlayer";
    }

    public String match(String name) {
        return !name.contains(":") ? name : null;
    }

    public Role create(String name) {
        return new PlayerRole(engine, name);
    }

    public ProtectionRole create(Protection protection, String name, ProtectionRole.Access access) {
        Player player = engine.getServerLayer().getPlayer(name);

        return new PlayerRole(engine, protection, player != null ? player.getUUID() : name, access);
    }

    public static class PlayerRole extends ProtectionRole {

        public PlayerRole(Engine engine, String roleName) {
            super(engine, null, roleName, null);
        }

        public PlayerRole(Engine engine, Protection protection, String roleName, ProtectionRole.Access roleAccess) {
            super(engine, protection, roleName, roleAccess);
        }

        @Override
        public String getType() {
            return "lwc:rolePlayer";
        }

        @Override
        public boolean included(Player player) {
            return getName().equalsIgnoreCase(player.getName());
        }

        /**
         * {@inheritDoc}
         */
        public ProtectionRole.Access getAccess(Protection protection, Player player) {
            if (player.getUUID().equalsIgnoreCase(getName())) {
                return getAccess();
            } else if (player.getName().equalsIgnoreCase(getName())) {
                setName(player.getUUID());
                save();
                return getAccess();
            } else {
                return Access.NONE;
            }
        }

    }

}
