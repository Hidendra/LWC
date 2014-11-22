package org.getlwc.forge.modsupport;

import org.getlwc.Engine;
import org.getlwc.Location;
import org.getlwc.World;
import org.getlwc.model.Protection;

public class BuildCraft {

    /**
     * Hook into anything we need to for BuildCraft
     *
     * @param engine
     */
    public static void run(final Engine engine) {

        //
        engine.getConsoleSender().sendMessage("Using BuildCraft version: {0}", buildcraft.core.Version.VERSION);

        // Add extraction handler
        buildcraft.api.transport.PipeManager.registerExtractionHandler(new buildcraft.api.transport.IExtractionHandler() {

            @Override
            public boolean canExtractItems(Object extractor, net.minecraft.world.World worldHandle, int x, int y, int z) {
                /**
                 * TODO: This should only be temporary until we PREVENT pipes entirely from being placed next to the chest.
                 *       This is because the owner(!) or accessors should be allowed to place pipes to pump items out/in
                 *       if they so wish without messing with configs!
                 */

                World world = engine.getServerLayer().getWorld(worldHandle.getWorldInfo().getWorldName());
                Location location = new Location(world, x, y, z);

                Protection protection = engine.getProtectionManager().findProtection(location);

                if (protection != null) {
                    return false;
                }

                return true;
            }

            @Override
            public boolean canExtractFluids(Object extractor, net.minecraft.world.World worldHandle, int x, int y, int z) {
                return true;
            }

        });

    }

}
