/*
 * Copyright (c) 2011-2013 Tyler Blair
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
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR,
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

package org.getlwc.forge.entity;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import org.getlwc.ItemStack;
import org.getlwc.Location;
import org.getlwc.entity.Player;
import org.getlwc.forge.LWC;
import org.getlwc.forge.modsupport.ForgeEssentials;
import org.getlwc.forge.modsupport.ModSupport;
import org.getlwc.forge.world.ForgeWorld;
import org.getlwc.util.Color;

public class ForgePlayer extends Player {

    /**
     * The mod handle
     */
    private LWC mod;

    /**
     * Player handle
     */
    private EntityPlayer handle;

    public ForgePlayer(EntityPlayer handle) {
        this.handle = handle;
        this.mod = LWC.instance;
    }

    /**
     * {@inheritDoc}
     */
    public String getUUID() {
        // TODO: convert to unique id upon public availability of 1.7
        return handle.getEntityName();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return handle.getEntityName();
    }

    /**
     * {@inheritDoc}
     */
    public Location getLocation() {
        try {
            return new Location(new ForgeWorld(handle.worldObj), (int) handle.posX, (int) handle.posY, (int) handle.posZ);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void sendMessage(String message) {
        for (String line : message.split("\n")) {
            handle.addChatMessage(Color.replaceColors(line));
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPermission(String node) {
        if (ModSupport.isModInstalled(ModSupport.Mod.FORGE_ESSENTIALS)) {
            return ForgeEssentials.checkPermission(handle, node);
        }

        // no handled Perm plugin
        return noPermissionPlugin(node);
    }

    /**
     * No permission plugin is available so try to resolve permissions by if they are using a special command (OP required)
     *
     * @param node
     * @return
     */
    private boolean noPermissionPlugin(String node) {
        if (!node.startsWith("lwc.mod") && !node.startsWith("lwc.admin")) {
            return true;
        } else if (node.startsWith("lwc.admin")) {
            return isOP();
        } else {
            return isOP();
        }
    }

    /**
     * Checks if a player is an OP. This is either an OP on a MP server or the owner of a LAN/SSP server
     *
     * @return
     */
    private boolean isOP() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

        if (server.isSinglePlayer()) {
            return server instanceof IntegratedServer && server.getServerOwner().equalsIgnoreCase(getName());
        } else {
            return server.getConfigurationManager().getOps().contains(getName().toLowerCase());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemStack getItemInHand() {
        return mod.castItemStack(handle.getHeldItem());
    }
}