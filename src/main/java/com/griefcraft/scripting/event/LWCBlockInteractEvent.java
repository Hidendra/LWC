package com.griefcraft.scripting.event;

import com.griefcraft.scripting.Module;
import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class LWCBlockInteractEvent extends LWCPlayerEvent implements IResult {

    private PlayerInteractEvent event;
    private Block block;
    private List<String> actions;
    private Module.Result result = Module.Result.DEFAULT;

    public LWCBlockInteractEvent(PlayerInteractEvent event, Block block, List<String> actions) {
        super(ModuleLoader.Event.INTERACT_BLOCK, event.getPlayer());

        this.event = event;
        this.block = block;
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

    public Block getBlock() {
        return block;
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
