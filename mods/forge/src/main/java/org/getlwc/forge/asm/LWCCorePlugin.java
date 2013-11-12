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
import org.getlwc.forge.ForgeServerInfo;
import org.getlwc.forge.ForgeServerLayer;
import org.getlwc.forge.LWC;

import java.util.Map;

public class LWCCorePlugin implements IFMLLoadingPlugin, IFMLCallHook {

    /**
     * If the core mod has been initialized
     */
    public static boolean INITIALIZED = false;

    public String[] getLibraryRequestClass() {
        return null;
    }

    public String[] getASMTransformerClass() {
        INITIALIZED = true;
        return new String[] {
                "org.getlwc.forge.asm.transformers.events.BlockBreakTransformer",
                "org.getlwc.forge.asm.transformers.events.ExplosionTransformer",
                "org.getlwc.forge.asm.transformers.events.SignUpdateTransformer",
                "org.getlwc.forge.asm.transformers.events.BlockPlaceTransformer",
                "org.getlwc.forge.asm.transformers.events.RedstoneTransformer",
                "org.getlwc.forge.asm.transformers.events.PistonUpdateStateTransformer",
                "org.getlwc.forge.asm.transformers.misc.UpdateClientInfoTransformer"
        };
    }

    public String getModContainerClass() {
        return null;
    }

    public String getSetupClass() {
        return "org.getlwc.forge.asm.LWCCorePlugin";
    }

    public void injectData(Map<String, Object> data) {

    }

    public Void call() throws Exception {
        if (LWC.instance == null) {
            System.out.println("LWC => init()");
            LWC.instance = new LWC();

            // create an engine
            ForgeServerLayer layer = new ForgeServerLayer();
            Engine engine = SimpleEngine.getOrCreateEngine(layer, new ForgeServerInfo(), new ForgeConsoleCommandSender());
            LWC.instance.setupServer(engine, layer);
        }

        return null;
    }
}