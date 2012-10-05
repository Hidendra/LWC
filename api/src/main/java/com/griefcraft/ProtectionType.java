package com.griefcraft;

import com.griefcraft.entity.Player;
import com.griefcraft.model.Protection;

public interface ProtectionType {

    /**
     * The unique identifier for this protection type. This must NEVER change as it will be used
     * when storing the protection inside the database
     * @return a unique integer for the protection type
     */
    public int getId();

    /**
     * This protection type's name. <b>NOTE:</b> This MUST also be unique and should not clash with
     * other protection type names. When stored internally this is casted to lower-case so this
     * should also be considered.
     * @return a unique string name for the protection type
     */
    public String getName();

    /**
     * Get the access level a player has to a protection for this type
     *
     * @param protection
     * @param player
     * @return the {@link ProtectionAccess} level the player has on the protection
     */
    public ProtectionAccess getAccess(Protection protection, Player player);

}
