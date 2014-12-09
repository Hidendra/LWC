package org.getlwc.granite;

import org.getlwc.ServerLayer;
import org.getlwc.World;
import org.getlwc.entity.Player;
import org.getlwc.granite.entity.GranitePlayer;
import org.getlwc.granite.world.GraniteWorld;
import org.granitemc.granite.api.Granite;
import org.granitemc.granite.api.plugin.PluginContainer;
import org.granitemc.granite.reflect.GraniteServerComposite;
import org.granitemc.granite.utils.Mappings;
import org.granitemc.granite.utils.MinecraftUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.UUID;

public class GraniteServerLayer extends ServerLayer {

    private PluginContainer container;

    public GraniteServerLayer(PluginContainer container) {
        this.container = container;
    }

    @Override
    public File getDataFolder() {
        return container.getDataDirectory();
    }

    @Override
    public World getDefaultWorld() {
        return new GraniteWorld((org.granitemc.granite.api.world.World) MinecraftUtils.wrap(experimentalGetWorlds()[0]));
    }

    @Override
    protected Player internalGetPlayer(String playerName) {
        org.granitemc.granite.api.entity.player.Player handle = Granite.getServer().getPlayer(playerName);

        return handle != null ? new GranitePlayer(handle) : null;
    }

    @Override
    protected World internalGetWorld(String worldName) {
        Granite.getLogger().info("[LWC] internalGetWorld(" + worldName + ")");
        // TODO multiworld support -- current world retrieval is a hack anyway :^)
        return getDefaultWorld();
    }

    @Override
    public UUID getOfflinePlayer(String ident) {
        // TODO
        return null;
    }

    private Object[] experimentalGetWorlds() {
        // TODO not yet supported by Granite natively
        try {
            Field worldServersField = Mappings.getClass("MinecraftServer").getDeclaredField("d");
            worldServersField.setAccessible(true);

            return (Object[]) worldServersField.get(GraniteServerComposite.instance.parent);
        } catch (Exception e) {
            Granite.getLogger().error("[LWC] Failed to get worlds experimentally");
            e.printStackTrace();
            return null;
        }
    }

}
