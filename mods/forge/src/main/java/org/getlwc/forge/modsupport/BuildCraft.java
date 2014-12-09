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

                Protection protection = engine.getProtectionManager().loadProtection(location);

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
