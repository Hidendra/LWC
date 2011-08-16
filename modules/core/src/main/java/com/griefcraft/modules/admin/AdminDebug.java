package com.griefcraft.modules.admin;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.MetaData;
import com.griefcraft.scripting.ModuleLoader;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.Colors;
import com.griefcraft.util.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;

public class AdminDebug extends JavaModule {

    @Override
    public void onCommand(LWCCommandEvent event) {
        if(event.isCancelled()) {
            return;
        }

        if(!event.hasFlag("a", "admin")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if(!args[0].equals("debug")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        ModuleLoader moduleLoader = lwc.getModuleLoader();

        sender.sendMessage("Players: " + Colors.Green + lwc.getPlugin().getServer().getOnlinePlayers().length);
        sender.sendMessage("Loaded modules: " + Colors.Green + moduleLoader.getModuleCount());
        sender.sendMessage("Module hierarchy:");

        for (Map.Entry<Plugin, List<MetaData>> entry : moduleLoader.getRegisteredModules().entrySet()) {
            Plugin plugin = entry.getKey();
            List<MetaData> modules = entry.getValue();

            sender.sendMessage(Colors.Green + plugin.getDescription().getName() + Colors.Yellow + " -> " + Colors.Green + modules.size() + Colors.Yellow + " registered modules");
        }

        sender.sendMessage(" ");
        // send a report
        moduleLoader.dispatchEvent(new LWCCommandEvent(sender, "admin", new String[] { "report" }));

        return;
    }

}