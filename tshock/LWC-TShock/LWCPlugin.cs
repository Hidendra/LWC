using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using Hooks;
using LWC_TDSM;
using LWC_TShock.Command;
using LWC_TShock.entity;
using TShockAPI;
using Terraria;
using com.griefcraft;
using com.griefcraft.command;
using com.griefcraft.world;
using Command = TShockAPI.Command;

namespace LWC_TShock
{
    [APIVersion(1, 12)]
    public class LWCPlugin : TerrariaPlugin
    {

        /**
         * The default world
         */
        public static com.griefcraft.world.World MAIN_WORLD = new TShockWorld("default");

        /**
         * The LWC object
         */
        public LWC LWC { get; internal set; }

        private ServerLayer layer;

        public override string Name
        {
            get { return "LWC"; }
        }
        public override string Author
        {
            get { return "Hidendra"; }
        }
        public override string Description
        {
            get { return "Native version of LWC using the in-progress LWC v5 engine via IKVM"; }
        }
        public override Version Version
        {
            get { return Assembly.GetExecutingAssembly().GetName().Version; }
        }

        public LWCPlugin(Main game) : base(game)
        {
            layer = new TShockServerLayer(this);
        }

        public override void Initialize()
        {
            GameHooks.PostInitialize += PostInitialize;
            NetHooks.GetData += GetData;
        }

        public void GetData(GetDataEventArgs evt)
        {
            if (evt.Handled)
            {
                return;
            }

            com.griefcraft.entity.Player player = layer.getPlayer(TShock.Players[evt.Msg.whoAmI].Name);
            com.griefcraft.world.World world = player.getLocation().getWorld();

            switch (evt.MsgID)
            {

                case PacketTypes.ChestGetContents:
                    {
                        int X = BitConverter.ToInt32(evt.Msg.readBuffer, evt.Index);
                        int Y = BitConverter.ToInt32(evt.Msg.readBuffer, evt.Index + 4);
                        Block block = world.getBlockAt(X, Y, 0);

                        bool result = player.getEventDelegate().onPlayerInteract(block);

                        if (result)
                        {
                            evt.Handled = true;
                        }
                    }

                    break;

                case PacketTypes.TileKill:
                    {
                        int X = BitConverter.ToInt32(evt.Msg.readBuffer, evt.Index);
                        int Y = BitConverter.ToInt32(evt.Msg.readBuffer, evt.Index + 4);
                        Block block = world.getBlockAt(X, Y, 0);
                    }

                    break;

            }
        }

        public void PostInitialize()
        {
            // create the LWC object
            Console.WriteLine();
            LWC = SimpleLWC.createLWC(layer, new TShockServerInfo(this), new TShockConsoleCommandSender(), new TShockConfiguration());

            Commands.ChatCommands.Add(new TShockAPI.Command(null, OnCommand, "lwc"));
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


        private void OnCommand(CommandArgs args)
        {
            CommandContext.Type type = CommandContext.Type.PLAYER;
            CommandSender sender = layer.getPlayer(args.Player.Name);
            
            if (args.Player.Name == "")
            {
                sender = LWC.getConsoleSender();
            }

            if (sender == null)
            {
                sender = LWC.getConsoleSender();
            }

            string message = args.Message;
            string normalized = normalizeCommand(message);

            int indexOfSpace = normalized.IndexOf(' ');

            try
            {
                if (indexOfSpace != -1)
                {
                    string command = normalized.Substring(0, indexOfSpace);
                    string arguments = message.Substring(indexOfSpace + 1);

                    // send out the command
                    LWC.getCommandHandler().handleCommand(new CommandContext(type, sender, command, arguments));
                }
                else // no arguments
                {
                    LWC.getCommandHandler().handleCommand(new CommandContext(type, sender, normalized));
                }
            }
            catch (CommandException e)
            {
                Log("Error while processing command: \"" + normalized + "\"");
                e.printStackTrace();
                // TODO
                sender.sendMessage("[LWC] An internal error occurred while processing this command");
            }
        }

        /**
         * Log a message to the console
         */
        private void Log(string message)
        {
            Console.WriteLine("[LWC] " + message);
        }
    }
}
