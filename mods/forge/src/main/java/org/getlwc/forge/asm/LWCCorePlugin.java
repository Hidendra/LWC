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

package org.getlwc.forge.asm;

import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import org.getlwc.Engine;
import org.getlwc.SimpleEngine;
import org.getlwc.forge.ForgeConsoleCommandSender;
import org.getlwc.forge.ForgeMod;
import org.getlwc.forge.ForgeServerInfo;
import org.getlwc.forge.ForgeServerLayer;

import java.util.Map;

public class LWCCorePlugin implements IFMLLoadingPlugin, IFMLCallHook {

    /**
     * If the core mod has been initialized
     */
    public static boolean INITIALIZED = false;

    public static final String[] TRANSFORMERS = new String[] {
            "org.getlwc.forge.asm.transformers.events.BlockBreakTransformer",
            "org.getlwc.forge.asm.transformers.events.ExplosionTransformer",
            "org.getlwc.forge.asm.transformers.events.SignUpdateTransformer",
            "org.getlwc.forge.asm.transformers.events.BlockPlaceTransformer",
            "org.getlwc.forge.asm.transformers.events.RedstoneTransformer",
            "org.getlwc.forge.asm.transformers.events.PistonUpdateStateTransformer",
            "org.getlwc.forge.asm.transformers.events.HopperPushItemTransformer",
            "org.getlwc.forge.asm.transformers.events.HopperSuckItemTransformer",
            "org.getlwc.forge.asm.transformers.events.EntityBreakDoorTransformer",
            "org.getlwc.forge.asm.transformers.misc.UpdateClientInfoTransformer"
    };

    @Override
    public String[] getASMTransformerClass() {
        INITIALIZED = true;

        for (String className : LWCCorePlugin.TRANSFORMERS) {
            try {
                Class<? extends AbstractTransformer> transformerClass = (Class<? extends AbstractTransformer>) Class.forName(className);
                AbstractMultiClassTransformer.TRANSFORMER_STATUSES.put(transformerClass, TransformerStatus.PENDING);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return TRANSFORMERS;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return "org.getlwc.forge.asm.LWCCorePlugin";
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public Void call() throws Exception {
        if (ForgeMod.instance == null) {
            System.out.println("LWC => init()");
            new ForgeMod().ensureEngineLoaded();
        } else {
            ForgeMod.instance.ensureEngineLoaded();
        }

        return null;
    }
}