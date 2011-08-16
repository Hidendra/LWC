package com.griefcraft.scripting.event;

import com.griefcraft.model.Protection;
import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.entity.Player;

public class LWCProtectionRemovePostEvent extends LWCProtectionEvent {

    public LWCProtectionRemovePostEvent(Protection protection) {
        // they registered the protection; safe to assume they can admin & access it!
        super(ModuleLoader.Event.POST_REMOVAL, protection.getBukkitOwner(), protection, true, true);
    }

}
