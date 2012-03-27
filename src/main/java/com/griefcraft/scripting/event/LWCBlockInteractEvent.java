/*
 * Copyright 2011 Tyler Blair. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package com.griefcraft.scripting.event;

import com.griefcraft.scripting.Module;
import com.griefcraft.scripting.ModuleLoader;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Set;

public class LWCBlockInteractEvent extends LWCPlayerEvent implements IResult {

    private PlayerInteractEvent event;
    private Block block;
    private Set<String> actions;
    private Module.Result result = Module.Result.DEFAULT;

    public LWCBlockInteractEvent(PlayerInteractEvent event, Block block, Set<String> actions) {
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

    public Set<String> getActions() {
        return actions;
    }

    public Module.Result getResult() {
        return result;
    }

    public void setResult(Module.Result result) {
        this.result = result;
    }

}
