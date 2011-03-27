package com.griefcraft.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;
import com.griefcraft.util.Colors;

public class Menu implements ICommand {

	@Override
	public void execute(LWC lwc, CommandSender sender, String[] args) {
		if (args.length < 2) {
			lwc.sendSimpleUsage(sender, "/lwc menu <basic|advanced>");
			return;
		}

		String newStyle = args[1].toLowerCase();

		if (!newStyle.equals("basic") && !newStyle.equals("advanced")) {
			sender.sendMessage(Colors.Red + "Invalid style.");
			return;
		}

		Player player = (Player) sender;

		lwc.getPhysicalDatabase().setMenuStyle(player.getName(), newStyle);
		lwc.sendLocale(player, "protection.menu.finalize", "style", newStyle);
	}

	@Override
	public String getName() {
		return "menu";
	}

	@Override
	public boolean supportsConsole() {
		return false;
	}

	@Override
	public boolean validate(LWC lwc, CommandSender sender, String[] args) {
		return args[0].equalsIgnoreCase("menu");
	}

}
