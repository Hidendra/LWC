package com.griefcraft.scripting.event;

import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class LWCSendLocaleEvent extends LWCPlayerEvent implements Cancellable {

    private String locale;
    private boolean cancelled = false;

    public LWCSendLocaleEvent(Player player, String locale) {
        super(ModuleLoader.Event.SEND_LOCALE, player);

        this.locale = locale;
    }

    public String getLocale() {
        return locale;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
