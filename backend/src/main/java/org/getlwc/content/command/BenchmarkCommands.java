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
package org.getlwc.content.command;

import org.getlwc.Engine;
import org.getlwc.Location;
import org.getlwc.command.Command;
import org.getlwc.command.CommandContext;
import org.getlwc.command.SenderType;

import java.util.Random;
import java.util.UUID;

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
            usage = "[rounds]",
            description = "Benchmark tests",
            accepts = SenderType.CONSOLE
    )
    public void insertTest(CommandContext context) {
        int rounds = Integer.parseInt(context.getArgument(1, "1"));

        engine.getConsoleSender().sendMessage("Inserting 10,000 * " + rounds + " random protections");

        int total = 0;

        for (int round = 0; round < rounds; round++) {
            Random random = new Random();
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                engine.getProtectionManager().createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"),
                        new Location(engine.getServerLayer().getDefaultWorld(), random.nextDouble() * 100000, random.nextDouble() * 100000, random.nextDouble() * 100000));
            }
            long time = System.currentTimeMillis() - start;
            total += time;
            engine.getConsoleSender().sendMessage(String.format("[%d/%d] %d ms total, %.2f ms average per protection", round + 1, rounds, time, time / 10000D));
        }

        long time = total / rounds;
        engine.getConsoleSender().sendMessage(String.format("Done %d rounds. %d ms total, %.2f ms average per protection", rounds, time, time / 10000D));
    }

    @Command(
            command = "lwc test select",
            usage = "[rounds]",
            description = "Benchmark tests",
            accepts = SenderType.CONSOLE
    )
    public void selectTest(CommandContext context) {
        int rounds = Integer.parseInt(context.getArgument(1, "1"));

        engine.getConsoleSender().sendMessage("Selecting 10,000 * " + rounds + " random protections");

        int total = 0;

        for (int round = 0; round < rounds; round++) {
            Random random = new Random();
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                engine.getDatabase().loadProtection(new Location(engine.getServerLayer().getDefaultWorld(), random.nextDouble() * 100000, random.nextDouble() * 100000, random.nextDouble() * 100000));
            }
            long time = System.currentTimeMillis() - start;
            total += time;
            engine.getConsoleSender().sendMessage(String.format("[%d/%d] %d ms total, %.2f ms average per protection", round + 1, rounds, time, time / 10000D));
        }

        long time = total / rounds;
        engine.getConsoleSender().sendMessage(String.format("Done %d rounds. %d ms total, %.2f ms average per protection", rounds, time, time / 10000D));
    }

}
