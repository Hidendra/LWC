using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using LWC_TDSM.Command;
using LWC_TDSM.World;
using Terraria_Server;
using Terraria_Server.Commands;
using Terraria_Server.Logging;
using Terraria_Server.Plugins;
using com.griefcraft;
using com.griefcraft.command;
using com.griefcraft.sql;
using com.griefcraft.world;
using java.lang;
using java.sql;

namespace LWC_TDSM
{
    class LWCPlugin : BasePlugin
    {
        public static TDSMWorld MAIN_WORLD = new TDSMWorld("default");

        /**
         * The LWC backend
         */
        public LWC LWC { get; internal set; }

        /**
         * The server list
         */
        private ServerLayer layer;

        public LWCPlugin()
        {
            Name = "LWC";
            Description = "Native version of LWC using the in-progress LWC v5 engine via IKVM";
            Author = "Hidendra";
            Version = "v0.01-unstable";
            TDSMBuild = 39;

            layer = new TDSMServerLayer(this);
        }

        // TODO repurpose into a usable alternative
        public Driver createDriver(JDBCDatabase.Driver driver)
        {
            // TODO SQLite
            if (driver == JDBCDatabase.Driver.MYSQL)
            {
                Class.forName(typeof(com.mysql.jdbc.Driver).AssemblyQualifiedName);
                return (Driver)new com.mysql.jdbc.Driver();
            }

            return null;
        }

        /**
         * Wrap a player object into the generic TDSMPlayer object
         * @return TDSMPlayer
         */
        public com.griefcraft.entity.Player wrapPlayer(Player handle)
        {
            return layer.getPlayer(handle.Name);
        }

        /**
         * Get a world using the given name
         */
        public com.griefcraft.world.World getWorld(string worldName)
        {
            return layer.getWorld(worldName);
        }

        protected override void Enabled()
        {
            Log("Attempting to create backend...");

            // create the backend
            LWC = SimpleLWC.createLWC(layer, new TDSMServerInfo(this), new TDSMConsoleCommandSender(), new TDSMConfiguration() /* TODO */);

            // hooks
            Hook(HookPoints.ChestOpenReceived, OnChestOpen);
            AddCommand("lwc").WithAccessLevel(AccessLevel.PLAYER).Calls(OnCommand);
        }
        
        /**
         * Normalize a command, making player and console commands appear to be the same format
         * 
         * @param message
         * @return
         */
        private string normalizeCommand(string message)
        {
            if (message.StartsWith("/"))
            {
                if (message.Length == 1)
                {
                    return "";
                }
                else
                {
                    message = message.Substring(1);
                }
            }

            return message.Trim();
        }

        private void OnCommand(ISender sender, ArgumentList args)
        {
            CommandContext.Type type = CommandContext.Type.PLAYER;
            CommandSender isender = new TDSMCommandSender(sender);

            // check if the sender is a player and if so, wrap it
            if (sender is Player)
            {
                isender = wrapPlayer((Player) sender);
            }

            string message = "/lwc " + string.Join(" ", args.ToArray());
            string normalized = normalizeCommand(message);

            Log(normalized);

            int indexOfSpace = normalized.IndexOf(' ');

            try
            {
                if (indexOfSpace != -1)
                {
                    string command = normalized.Substring(0, indexOfSpace);
                    string arguments = message.Substring(indexOfSpace + 1);

                    // send out the command
                    LWC.getCommandHandler().handleCommand(new CommandContext(type, isender, command, arguments));
                } else // no arguments
                {
                    LWC.getCommandHandler().handleCommand(new CommandContext(type, isender, normalized));
                }
            } catch (CommandException e)
            {
                Log("Error while processing command: \"" + normalized + "\"");
                e.printStackTrace();
                // TODO
                sender.sendMessage("[LWC] An internal error occurred while processing this command");
            }
        }

        [Hook(HookOrder.NORMAL)]
        private void OnChestOpen(ref HookContext ctx, ref HookArgs.ChestOpenReceived args)
        {
            com.griefcraft.entity.Player player = wrapPlayer(ctx.Player);
            com.griefcraft.world.World world = player.getLocation().getWorld();
            Block block = world.getBlockAt(args.X, args.Y, 0);

            // send the event!
            bool result = player.getEventDelegate().onPlayerInteract(block);

            if (result)
            {
                ctx.SetResult(HookResult.ERASE);
            }
        }

        private void Log(string message)
        {
            ProgramLog.Log("[LWC] " + message);
        }

    }
}
