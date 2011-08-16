package com.griefcraft.scripting.event;

import com.griefcraft.model.Protection;
import com.griefcraft.scripting.Module;
import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class LWCProtectionInteractEvent extends LWCProtectionEvent implements IResult {

    private PlayerInteractEvent event;
    private List<String> actions;
    private Module.Result result;

    public LWCProtectionInteractEvent(PlayerInteractEvent event, Protection protection, List<String> actions, boolean canAccess, boolean canAdmin) {
        super(ModuleLoader.Event.INTERACT_PROTECTION, event.getPlayer(), protection, canAccess, canAdmin);

        this.event = event;
        this.actions = actions;
    }

    /**
     * Check if the player's actions contains the action
     *
     * @param action
     * @return
     */
    public boolean hasAction(String action) {
        return actions.contains(action);
    }

    public PlayerInteractEvent getEvent() {
        return event;
    }

    public List<String> getActions() {
        return actions;
    }

    public Module.Result getResult() {
        return result;
    }

    public void setResult(Module.Result result) {
        this.result = result;
    }

}
