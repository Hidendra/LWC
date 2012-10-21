/*
 * Copyright (c) 2011, 2012, Tyler Blair
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

package com.griefcraft.commands;

import com.griefcraft.Engine;
import com.griefcraft.command.Command;
import com.griefcraft.command.CommandContext;
import com.griefcraft.command.SenderType;
import com.griefcraft.world.Location;

import java.util.Random;

public class BenchmarkCommands {

    /**
     * The LWC engine
     */
    private Engine engine;

    public BenchmarkCommands(Engine engine) {
        this.engine = engine;
    }

    @Command(
            command = "lwc test insert",
            accepts = SenderType.CONSOLE
    )
    public void insertTest(CommandContext context) {
        engine.getConsoleSender().sendMessage("Inserting 10,000 random protections");
        Random random = new Random();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            //lwc.getProtectionManager().createProtection(Protection.Type.PRIVATE, "virulent",
            //        new Location(lwc.getServerLayer().getDefaultWorld(), random.nextDouble() * 100000, random.nextDouble() * 100000, random.nextDouble() * 100000));
        }
        long time = System.currentTimeMillis() - start;
        engine.getConsoleSender().sendMessage(String.format("Done. %d ms total, %.2f ms per protection", time, time / 10000D));
    }

    @Command(
            command = "lwc test select",
            accepts = SenderType.CONSOLE
    )
    public void selectTest(CommandContext context) {
        engine.getConsoleSender().sendMessage("Selecting 10,000 random protections");
        Random random = new Random();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            engine.getDatabase().loadProtection(new Location(engine.getServerLayer().getDefaultWorld(), random.nextDouble() * 100000, random.nextDouble() * 100000, random.nextDouble() * 100000));
        }
        long time = System.currentTimeMillis() - start;
        engine.getConsoleSender().sendMessage(String.format("Done. %d ms total, %.2f ms per protection", time, time / 10000D));
    }

}
