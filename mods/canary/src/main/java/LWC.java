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

import org.getlwc.Engine;
import org.getlwc.ItemStack;
import org.getlwc.ServerLayer;
import org.getlwc.SimpleEngine;

import java.util.HashMap;
import java.util.Map;

public class LWC extends Plugin {

    /**
     * The LWC engine
     */
    private Engine engine;

    /**
     * The server layer
     */
    private final ServerLayer layer = new CanaryServerLayer(this);

    /**
     * The listener class
     */
    private PluginListener listener = new CanaryListener(this);

    @Override
    public void enable() {

        // Set the name
        setName("LWC");

        engine = SimpleEngine.getOrCreateEngine(layer, new CanaryServerInfo(), new CanaryConsoleCommandSender());
        engine.onLoad();

        // Register our listeners
        etc.getLoader().addListener(PluginLoader.Hook.OPEN_INVENTORY, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.BLOCK_RIGHTCLICKED, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.COMMAND, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.SERVERCOMMAND, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.EXPLOSION, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.REDSTONE_CHANGE, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.PLAYER_CONNECT, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.PLAYER_DISCONNECT, listener, this, PluginListener.Priority.MEDIUM);
    }

    @Override
    public void disable() {

    }

    /**
     * Get the LWC engine
     *
     * @return
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * Wrap a native Canary player
     *
     * @param player
     * @return
     */
    public org.getlwc.entity.Player wrapPlayer(Player player) {
        return layer.getPlayer(player.getName());
    }

    /**
     * Get a World object for the native Canary world
     *
     * @param worldName
     * @return
     */
    public org.getlwc.World getWorld(String worldName) {
        return layer.getWorld(worldName);
    }

    /**
     * Cast a map of enchantments to our native enchantment mappings
     *
     * @param enchantments
     * @return
     */
    public Map<Integer, Integer> castEnchantments(Enchantment[] enchantments) {
        Map<Integer, Integer> ret = new HashMap<Integer, Integer>();

        for (Enchantment enchantment : enchantments) {
            ret.put(enchantment.getType().getType(), enchantment.getLevel());
        }

        return ret;
    }

    /**
     * Cast an item stack to our native ItemStack
     * @param item
     * @return
     */
    public ItemStack castItemStack(Item item) {
        if (item == null) {
            return null;
        }

        return new ItemStack(item.getItemId(), item.getAmount(), (short) item.getDamage(), item.getMaxAmount(), castEnchantments(item.getEnchantments()));
    }

}
