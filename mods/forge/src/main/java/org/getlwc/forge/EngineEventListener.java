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
package org.getlwc.forge;

import cpw.mods.fml.common.FMLCommonHandler;
import org.getlwc.Engine;
import org.getlwc.event.Listener;
import org.getlwc.event.engine.BaseCommandRegisteredEvent;
import org.getlwc.forge.asm.AbstractSingleClassTransformer;

import java.lang.reflect.Method;

public class EngineEventListener {

    private Engine engine;
    private ForgeMod mod;

    /**
     * Cached method for CommandHandler.registerCommand
     */
    private Method cachedRegisterCommandMethod = null;

    public EngineEventListener(Engine engine, ForgeMod mod) {
        this.engine = engine;
        this.mod = mod;
    }

    @SuppressWarnings("unused")
    @Listener
    public void onRegisterBaseCommand(BaseCommandRegisteredEvent event) {
        try {
            if (cachedRegisterCommandMethod == null) {
                // find registerCommand(ICommand) without invoking it directly
                for (Method method : net.minecraft.command.CommandHandler.class.getDeclaredMethods()) {
                    Class<?>[] paramTypes = method.getParameterTypes();

                    if (paramTypes.length != 1) {
                        continue;
                    }

                    if (paramTypes[0].getCanonicalName().equals(AbstractSingleClassTransformer.getClassName("ICommand", true)) || paramTypes[0].getCanonicalName().equals(AbstractSingleClassTransformer.getClassName("ICommand", false))) {
                        cachedRegisterCommandMethod = method;
                        break;
                    }
                }
            }

            if (cachedRegisterCommandMethod != null) {
                cachedRegisterCommandMethod.invoke(FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager(), new NativeCommandHandler(event.getNormalizedCommand()));
            }
        } catch (Exception e) {
            System.err.println(" !!!! LWC is likely not compatible with this version of Minecraft. You need to update!");
            e.printStackTrace();
        }
    }

}
