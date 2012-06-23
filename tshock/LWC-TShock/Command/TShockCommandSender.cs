using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using TShockAPI;
using Terraria;
using com.griefcraft.command;

namespace LWC_TShock.Command
{
    class TShockCommandSender : CommandSender
    {
        
        /**
         * The player handle
         */
        private TSPlayer handle;

        public TShockCommandSender(TSPlayer handle)
        {
            this.handle = handle;
        }

        public void sendMessage(string message)
        {
            foreach (string line in message.Split('\n'))
            {
                handle.SendMessage(line);
            }
        }

        public void sendLocalizedMessage(string str, params object[] objarr)
        {
            throw new NotImplementedException();
        }

        public bool hasPermission(string str)
        {
            // TODO
            return true;
        }
    }
}
