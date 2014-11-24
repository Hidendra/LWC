package org.getlwc.forge;

import cpw.mods.fml.common.FMLCommonHandler;
import org.getlwc.Engine;
import org.getlwc.event.Listener;
import org.getlwc.event.engine.BaseCommandRegisteredEvent;
import org.getlwc.forge.asm.AbstractSingleClassTransformer;

import java.lang.reflect.Method;

public class EngineEventListener {

    private Engine engine;
    private LWC mod;

    /**
     * Cached method for CommandHandler.registerCommand
     */
    private Method cachedRegisterCommandMethod = null;

    public EngineEventListener(Engine engine, LWC mod) {
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
