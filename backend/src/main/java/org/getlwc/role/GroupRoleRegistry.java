package org.getlwc.role;

import org.getlwc.Engine;
import org.getlwc.entity.Player;
import org.getlwc.model.Protection;

import java.util.Set;

public class GroupRoleRegistry implements RoleFactory {

    private Engine engine;

    public GroupRoleRegistry(Engine engine) {
        this.engine = engine;
    }

    public String match(String name) {
        String lowerCase = name.toLowerCase();

        if (lowerCase.startsWith("g:")) {
            return name.substring(2);
        } else if (lowerCase.startsWith("group:")) {
            return name.substring(6);
        } else {
            return null;
        }
    }

    public Role create(String name) {
        return new GroupRole(engine, name);
    }

    public ProtectionRole create(Protection protection, String name, ProtectionRole.Access access) {
        return new GroupRole(engine, protection, name, access);
    }

    public String getName() {
        return "lwc:roleGroup";
    }

    public static class GroupRole extends ProtectionRole {

        public GroupRole(Engine engine, String roleName) {
            super(engine, null, roleName, null);
        }

        public GroupRole(Engine engine, Protection protection, String roleName, ProtectionRole.Access roleAccess) {
            super(engine, protection, roleName, roleAccess);
        }

        @Override
        public String getType() {
            return "lwc:roleGroup";
        }

        @Override
        public boolean included(Player player) {
            Set<String> groups = engine.getPermissionHandler().getGroups(player);

            for (String group : groups) {
                if (group.equalsIgnoreCase(getName())) {
                    return true;
                }
            }

            return false;
        }

        /**
         * {@inheritDoc}
         */
        public ProtectionRole.Access getAccess(Protection protection, Player player) {
            if (included(player)) {
                return getAccess();
            } else {
                return Access.NONE;
            }
        }

    }

}
