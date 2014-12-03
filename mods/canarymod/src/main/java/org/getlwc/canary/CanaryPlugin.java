package org.getlwc.canary;

import net.canarymod.Canary;
import net.canarymod.api.inventory.Enchantment;
import net.canarymod.api.inventory.Item;
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
        engine = (SimpleEngine) SimpleEngine.getOrCreateEngine(layer, new CanaryServerInfo(), new CanaryConsoleCommandSender(getLogman()));
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
        Map<Integer, Integer> ret = new HashMap<Integer, Integer>();

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
