/**
 * This file is part of LWC (https://github.com/Hidendra/LWC)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.griefcraft.modules.credits;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;
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
			while(LWC.ENABLED) {
				synchronized(scrolling) {
					Iterator<Map.Entry<CommandSender, Integer>> iter = scrolling.entrySet().iterator();

					while(iter.hasNext()) {
						Map.Entry<CommandSender, Integer> entry = iter.next();
						CommandSender sender = entry.getKey();
						int index = entry.getValue();

						// Done!
						if(index >= credits.length) {
							iter.remove();
							continue;
						}

						// if they're a player, and not online, don't send
						if((sender instanceof Player) && !((Player) sender).isOnline()) {
							iter.remove();
							continue;
						}

						// if it's 0, we should bulk send the first few
						if(index == 0) {
							for(int i=0; i<FIRST_SEND; i++) {
								if(index >= credits.length) {
									break;
								}

								sender.sendMessage(credits[index]);
								index ++;
							}
						} else {
							sender.sendMessage(credits[index]);
							index ++;
						}

						// update the index
						entry.setValue(index);
					}
				}

				try {
					Thread.sleep(UPDATE_INTERVAL);
				} catch(Exception e) { }
			}
		}

	}

	@Override
	public void load(LWC lwc) {
		credits = new String[] {
				Colors.Green + "LWC, a Protection mod",
				" ",

				Colors.Red + "Plugin core",
				"Hidendra",
				"angelsl",
				"morganm",
				" ",

				Colors.Red + "Translations",
				"Dawodo - German",
				"Geoning - Polish",
				"dudsonowa - Polish",
				"andrewkm - Polish",
				"cehel - French",
				"Madzero - Dutch",
				"aoa2003 - Dutch",
				"hofec - Czech",
				"christley - Swedish",
				"IlyaGulya - Russian",
				"Raul \"RME\" Martinez - Spanish",
				"David \"DME\" Martinez - Spanish",
				"TCarlsen - Danish",
				" ",
				
				Colors.Red + "Donations",
				Colors.Gray + "(chronological order)",
				"darknavi",
				"Vetyver",
				"pablo0713",
				"Irishsailor & Land of Legend server",
				"aidan",
				"Monstertke",
				"wokka",
				"Andreoli3",
				" ",
				
				Colors.Red + "And....",
				Colors.LightBlue + "Old Griefcraft server -- love you guys!",
				"jobsti",
				"Joy",
				"KaneHart",
				"Kainzo (you find issues before I have a chance to look :3)",
				"& the Herocraft team",
				"#bukkit",
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
				" ",
				
				"To everyone else and anyone I missed....",
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
	public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args) {
		if(!StringUtils.hasFlag(command, "credits") && !StringUtils.hasFlag(command, "thanks")) {
			return DEFAULT;
		}

		if(!scrolling.containsKey(sender)) {
			scrolling.put(sender, 0);
		} else {
			scrolling.remove(sender);
		}

		return CANCEL;
	}

}
