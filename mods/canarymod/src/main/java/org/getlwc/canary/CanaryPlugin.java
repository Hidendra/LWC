/**
 * Copyright (c) 2011-2014 Tyler Blair
 * All rights reserved.
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
package org.getlwc.canary;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import net.canarymod.Canary;
import net.canarymod.api.inventory.Enchantment;
import net.canarymod.api.inventory.Item;
import net.canarymod.logger.Logman;
import net.canarymod.plugin.Plugin;
import org.getlwc.Engine;
import org.getlwc.ItemStack;
import org.getlwc.Location;
import org.getlwc.ServerLayer;
import org.getlwc.SimpleEngine;
import org.getlwc.canary.entity.CanaryEntity;
import org.getlwc.canary.listeners.CanaryListener;
import org.getlwc.canary.permission.CanaryPermissionHandler;
import org.getlwc.entity.Entity;
import org.getlwc.entity.Player;
import org.getlwc.event.server.ServerStartingEvent;
import org.getlwc.event.server.ServerStoppingEvent;
import org.getlwc.lang.Locale;
import org.getlwc.util.registry.FallbackMinecraftRegistry;

import java.util.HashMap;
import java.util.Map;

public class CanaryPlugin extends Plugin {

    private SimpleEngine engine;

    /**
     * The server layer that provides Canary-specific calls
     */
    private final ServerLayer layer = new CanaryServerLayer(this);

    @Override
    public boolean enable() {
        Injector injector = Guice.createInjector(new CanaryEngineGuiceModule(this));
        engine = (SimpleEngine) injector.getInstance(Engine.class);
        System.out.println("engine = " + engine);

        engine.setPermissionHandler(new CanaryPermissionHandler());

        engine.getEventBus().subscribe(new EngineEventListener(engine, this));
        engine.getEventBus().post(new ServerStartingEvent());

        // Hooks
        Canary.hooks().registerListener(new CanaryListener(this), this);

        return true;
    }

    @Override
    public void disable() {
        engine.getEventBus().post(new ServerStoppingEvent());
        engine = null;
    }

    @Provides
    public CanaryPlugin providePlugin() {
        return this;
    }

    /**
     * @return the {@link Engine} object
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * Wraps a native Canary entity
     *
     * @param entity
     * @return
     */
    public Entity wrapEntity(net.canarymod.api.entity.Entity entity) {
        if (entity instanceof net.canarymod.api.entity.living.humanoid.Player) {
            return wrapPlayer((net.canarymod.api.entity.living.humanoid.Player) entity);
        } else {
            // TODO wasted creation
            return new CanaryEntity(this, entity);
        }
    }

    /**
     * Wraps a native Canary player
     *
     * @param player
     * @return
     */
    public Player wrapPlayer(net.canarymod.api.entity.living.humanoid.Player player) {
        Player res = layer.getPlayer(player.getName());

        if (!res.getLocale().getName().equals(player.getLocale())) {
            res.setLocale(new Locale(player.getLocale()));
            engine.getConsoleSender().sendMessage("Player " + res.getName() + " loaded using locale: " + res.getLocale().toString());
        }

        return res;
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
     * Cast a location to our native location
     *
     * @param location
     * @return
     */
    public Location castLocation(net.canarymod.api.world.position.Location location) {
        return new Location(getWorld(location.getWorld().getName()), location.getX(), location.getY(), location.getZ());
    }

    /**
     * Cast a map of enchantments to our native enchantment mappings
     *
     * @param enchantments
     * @return
     */
    public Map<Integer, Integer> castEnchantments(Enchantment[] enchantments) {
        Map<Integer, Integer> ret = new HashMap<>();

        for (Enchantment enchantment : enchantments) {
            ret.put(enchantment.getType().getId(), (int) enchantment.getLevel());
        }

        return ret;
    }

    /**
     * Cast an item stack to our native ItemStack
     *
     * @param item
     * @return
     */
    public ItemStack castItemStack(Item item) {
        if (item == null) {
            return null;
        }

        return new ItemStack(item.getId(), item.getAmount(), (short) item.getDamage(), item.getMaxAmount(), castEnchantments(item.getEnchantments()));
    }

}
