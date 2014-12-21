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

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.Field;
import java.util.UUID;

public class GraniteServerLayer extends ServerLayer {

    private PluginContainer container;

    @Inject
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
    public String getImplementationTitle() {
        return "Granite";
    }

    @Override
    public String getImplementationVersion() {
        // TODO
        return "Unknown";
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
