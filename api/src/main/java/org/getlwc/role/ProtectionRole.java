package org.getlwc.role;

import org.getlwc.AccessProvider;
import org.getlwc.Engine;
import org.getlwc.I18n;
import org.getlwc.model.Protection;
import org.getlwc.model.State;

import java.util.EnumSet;

import static org.getlwc.I18n._;

public abstract class ProtectionRole extends Role implements AccessProvider {

    /**
     * The state this role is in
     */
    private State state = State.NEW;

    /**
     * The protection this role is for
     */
    private final Protection protection;

    /**
     * The access to grant to players that match this role
     */
    private Access access;

    public ProtectionRole(Engine engine, Protection protection, String roleName, Access roleAccess) {
        super(engine, roleName);
        this.protection = protection;
        this.access = roleAccess;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": name=\"" + getName() + "\" access=" + access.toString() + " protection=\"" + protection + "\"";
    }

    @Override
    public void loadData(String data) {
        throw new UnsupportedOperationException("ProtectionRole.loadData is not used");
    }

    /**
     * Get the protection that this role is for
     *
     * @return
     */
    public Protection getProtection() {
        return protection;
    }

    /**
     * Set the name of the role
     *
     * @param name
     */
    public void setName(String name) {
        super.setName(name);
        state = State.MODIFIED;
    }

    /**
     * Get the {@link ProtectionRole.Access} this role can provide
     *
     * @return
     */
    public Access getAccess() {
        return access;
    }

    /**
     * Set the access for the protection
     *
     * @param access
     */
    public void setProtectionAccess(Access access) {
        this.access = access;
        state = State.MODIFIED;
    }

    /**
     * Get the state this role is in
     *
     * @return
     */
    public State getState() {
        return state;
    }

    /**
     * Change the state this role is in
     *
     * @param state
     */
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public void saveImmediately() {
        // this will update or create the role depending on the current state
        engine.getDatabase().saveOrCreateRole(this);
        state = State.UNMODIFIED;
    }

    @Override
    public boolean isSaveNeeded() {
        return state == State.MODIFIED || state == State.NEW;
    }

    @Override
    public void remove() {
        engine.getDatabase().removeRole(this);
        state = State.REMOVED;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        ProtectionRole o = (ProtectionRole) object;
        return getType().equalsIgnoreCase(o.getType()) && getName().equals(o.getName()) && access == o.access && state == o.state;
    }


    /**
     * Access levels for protections. ordinal values are used here meaning they must remain in a constant order. As well,
     * the enum values are ranked in power of ascending order meaning Access(4) has more power than
     * Access(1) will. This also implies that the initial implementation is complete and that adding
     * any more access levels would be a pain.
     * <p/>
     * As well, the only exception to these rules is EXPLICIT_DENY which will immediately deny access to the
     * protection. This will not always be used but may be useful in some cases.
     */
    public static enum Access {

        /**
         * Immediately reject access to the protection.
         */
        EXPLICIT_DENY(I18n.markAsTranslatable("explicit_deny")),

        /**
         * User has NO access to the protection
         */
        NONE(I18n.markAsTranslatable("none")),

        /**
         * The user can view the protection but not modify it in any way. The implementation of this depends
         * on the mod and if the mod does not support preventing the inventory from being modified somehow
         * then access will just be blocked.
         */
        GUEST(I18n.markAsTranslatable("guest")),

        /**
         * User can only deposit into the protection
         */
        DEPOSITONLY(I18n.markAsTranslatable("depositonly")),

        /**
         * User can deposit and withdraw from the protection at will but not add or remove other users to it.
         */
        MEMBER(I18n.markAsTranslatable("member")),

        /**
         * User can modify the protection (add and remove members) but not add or remove other managers.
         */
        MANAGER(I18n.markAsTranslatable("manager")),

        /**
         * User has the same access as the user who created the protection. They can remove the protection,
         * add or remove ANY level to the protection (i.e. other owners) but they cannot remove themselves
         * from the protection
         */
        OWNER(I18n.markAsTranslatable("owner"));

        /**
         * Access levels that normal players can set
         */
        public final static EnumSet<Access> USABLE_ACCESS_LEVELS = EnumSet.range(NONE, OWNER);

        /**
         * Access levels that can view or interact with the protection
         */
        public final static EnumSet<Access> CAN_ACCESS = EnumSet.range(GUEST, OWNER);

        /**
         * The translated name for the enum
         */
        private String translatedName = null;

        Access(String translatedName) {
        }

        /**
         * Get the translated name of the access level
         *
         * @return translated name
         */
        public String getTranslatedName() {
            if (translatedName == null) {
                translatedName = _(toString().toLowerCase());
            }

            return translatedName;
        }

        /**
         * Match a {@link org.getlwc.role.ProtectionRole.Access} given a name.
         *
         * @param name
         * @return NULL if no {@link org.getlwc.role.ProtectionRole.Access} is matched
         */
        public static Access match(String name) {
            for (Access access : Access.values()) {
                if (access.toString().equalsIgnoreCase(name)) {
                    return access;
                }
            }

            return null;
        }

    }
}
