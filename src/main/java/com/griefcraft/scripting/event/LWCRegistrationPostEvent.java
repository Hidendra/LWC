package com.griefcraft.scripting.event;

import com.griefcraft.model.Protection;
import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class LWCRegistrationPostEvent extends LWCProtectionEvent {

    public LWCRegistrationPostEvent(Player player, Protection protection) {
        // they registered the protection; safe to assume they can admin & access it!
        super(ModuleLoader.Event.POST_REGISTRATION, player, protection, true, true);
    }

}
