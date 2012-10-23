package com.griefcraft.model;

import com.griefcraft.AccessProvider;
import com.griefcraft.ProtectionAccess;

public abstract class Role extends AbstractSavable implements AccessProvider {

    /**
     * The state of the role
     */
    public enum State {

        /**
         * Role is new, needs to be inserted into the database
         */
        NEW,

        /**
         * Role has been modified
         */
        MODIFIED,

        /**
         * Role has not been modified
         */
        UNMODIFIED

    }

    /**
     * The state this role is in
     */
    private State state = State.NEW;

    /**
     * The protection this role is for
     */
    private Protection protection;

    /**
     * The role name for the player to grant access to
     */
    private String roleName;

    /**
     * The access to grant to players that match this role
     */
    private ProtectionAccess roleAccess;

    public Role(Protection protection, String roleName, ProtectionAccess roleAccess) {
        this.protection = protection;
        this.roleName = roleName;
        this.roleAccess = roleAccess;
    }

    /**
     * Get the protection that this role is for
     * @return
     */
    public Protection getProtection() {
        return protection;
    }

    /**
     * Get the name of the role this defines, e.g a player's name for a PlayerRole
     * @return
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Get the {@link ProtectionAccess} this role can provide
     * @return
     */
    public ProtectionAccess getRoleAccess() {
        return roleAccess;
    }

    /**
     * Get the state this role is in
     * @return
     */
    public State getState() {
        return state;
    }

    /**
     * Change the state this role is in
     * @param state
     */
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public void saveImmediately() {
        // this will update or create the role depending on the current state
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isSaveNeeded() {
        return state == State.MODIFIED;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Role o = (Role) object;
        return getId() == o.getId() && roleName.equals(o.roleName) && roleAccess == o.roleAccess && state == o.state;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash *= 17 + getId();
        hash *= 17 + roleName.hashCode();
        hash *= 17 + roleAccess.hashCode();
        return hash;
    }

    /**
     * Get a unique integer that will be used to represent this role
     * @return
     */
    public abstract int getId();

}
