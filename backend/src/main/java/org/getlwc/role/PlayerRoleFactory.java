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

    public Role create(Protection protection, String name, Role.Access access) {
        Player player = engine.getServerLayer().getPlayer(name);

        return new PlayerRole(engine, protection, player != null ? player.getUUID() : name, access);
    }

    public static class PlayerRole extends Role {

        public PlayerRole(Engine engine, Protection protection, String roleName, Access roleAccess) {
            super(engine, protection, roleName, roleAccess);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getType() {
            return "lwc:rolePlayer";
        }

        /**
         * {@inheritDoc}
         */
        public Access getAccess(Protection protection, Player player) {
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
