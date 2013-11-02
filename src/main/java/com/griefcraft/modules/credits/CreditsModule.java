/*
 * Copyright 2011 Tyler Blair. All rights reserved.
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

package com.griefcraft.modules.credits;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.Colors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CreditsModule extends JavaModule {

    /**
     * How many lines to send when they first use /lwc credits
     */
    private static final int FIRST_SEND = 2;

    /**
     * How often to send messages
     */
    private static final long UPDATE_INTERVAL = 1150L;

    /**
     * The credits list
     */
    private String[] credits;

    /**
     * Players to send to and the credits index
     */
    private final Map<CommandSender, Integer> scrolling = Collections.synchronizedMap(new HashMap<CommandSender, Integer>());

    private class CreditsTask implements Runnable {

        public void run() {
            while (LWC.ENABLED) {
                synchronized (scrolling) {
                    Iterator<Map.Entry<CommandSender, Integer>> iter = scrolling.entrySet().iterator();

                    while (iter.hasNext()) {
                        Map.Entry<CommandSender, Integer> entry = iter.next();
                        CommandSender sender = entry.getKey();
                        int index = entry.getValue();

                        // Done!
                        if (index >= credits.length) {
                            iter.remove();
                            continue;
                        }

                        // if they're a player, and not online, don't send
                        if ((sender instanceof Player) && !((Player) sender).isOnline()) {
                            iter.remove();
                            continue;
                        }

                        // if it's 0, we should bulk send the first few
                        if (index == 0) {
                            for (int i = 0; i < FIRST_SEND; i++) {
                                if (index >= credits.length) {
                                    break;
                                }

                                sender.sendMessage(credits[index]);
                                index++;
                            }
                        } else {
                            sender.sendMessage(credits[index]);
                            index++;
                        }

                        // update the index
                        entry.setValue(index);
                    }
                }

                try {
                    Thread.sleep(UPDATE_INTERVAL);
                } catch (Exception e) {
                }
            }
        }

    }

    @Override
    public void load(LWC lwc) {
        credits = new String[]{
                Colors.Green + "LWC, a Protection mod developed by Hidendra ....",
                "Serving Minecraft loyally since September 2010 ....",
                " ",

                Colors.Red + "Core contributions",
                "angelsl",
                "morganm",
                " ",

                Colors.Red + "Translations",
                Colors.Green + "German - " + Colors.White + "Dawodo",
                Colors.Green + "Polish - " + Colors.White + "Geoning, dudsonowa, and andrewkm",
                Colors.Green + "French - " + Colors.White + "cehel",
                Colors.Green + "Dutch - " + Colors.White + "Madzero and aoa2003",
                Colors.Green + "Czech - " + Colors.White + "hofec",
                Colors.Green + "Swedish - " + Colors.White + "christley",
                Colors.Green + "Russian - " + Colors.White + "IlyaGulya",
                Colors.Green + "Spanish - " + Colors.White + "Raul \"RME\" Martinez and David \"DME\" Martinez",
                Colors.Green + "Danish - " + Colors.White + "TCarlsen, cannafix",
                Colors.Green + "Italian - " + Colors.White + "portaro",
                Colors.Green + "Hungarian - " + Colors.White + "dretax",
                " ",

                Colors.Red + "Donations",
                Colors.Gray + "(chronological order)",
                "darknavi",
                "Vetyver",
                "pablo0713",
                Colors.Red + "IrishSailor & Land of Legend server X2",
                "aidan",
                Colors.Bold + Colors.Gold + "MonsterTKE X3",
                "wokka",
                "Andreoli3",
                Colors.Bold + Colors.Gold + "andrewkm X3",
                "Eric King",
                "Twizz",
                "spunkiie",
                "RustyDagger",
                "Sam (Nodex servers)",
                "doomkidkiller",
                "untergrundbiber",
                "Northland Gaming",
                "imaxorz & the Shade Crest server",
                "schlex",
                "TnT",
                "jakez",
                "jkcclemens",
                "drayshak",
                "DMarby",
                "Afforess",
                "RagnaCraft",
                "Christopher Dziomba",
                "Grant McFerrin",
                "Matthew Kemmerer",
                "Daniel Tung",
                "Sway",
                "ZachBora",
                "jordan1986",
                " ",

                Colors.Red + "And....",
                Colors.LightBlue + "Old Griefcraft server -- love you guys!",
                "jobsti",
                "Joy",
                "KaneHart",
                "Kainzo (finds issues before I have a chance to look)",
                "& the rest of the HEROCRAFT team",
                "Bryan (CursedChild)",
                "Ken (i_pk_pjers_i)",
                "SeeD419",
                "Lim-Dul",
                "arensirb",
                "RustyDagger",
                "HotelErotica",
                "andrewkm",
                "Moo0",
                "Dawodo",
                "xPaw",
                "Samkio",
                "msleeper",
                "Taco",
                "Acrobat",
                "SquallSeeD31",
                "Wahrheit",
                "Kerazene",
                "spunkiie",
                "Zalastri",
                "MonsterTKE",
                "Tyrope",
                "krinsdeath",
                "VADemon",
                "armed_troop",
                "DeadFred",
                "ProjectInfinity",
                "KHobbits",
                "TnT",
                " ",

                Colors.Yellow + "To everyone else and anyone I missed....",
                "LWC would not be the awesome plugin it is today if not also for those not listed",
                " ",
                Colors.Blue + "THANK YOU!"
        };

        // not using the bukkit scheduler because tick rates vary from server to server
        // on one server, it'll send every second, while on another, every 5 seconds!
        CreditsTask task = new CreditsTask();
        new Thread(task).start();
        // lwc.getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(lwc.getPlugin(), task, 10, 10);
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("about", "credits", "thanks")) {
            return;
        }

        CommandSender sender = event.getSender();

        if (!scrolling.containsKey(sender)) {
            scrolling.put(sender, 0);
        } else {
            scrolling.remove(sender);
        }

        event.setCancelled(true);
    }

}
