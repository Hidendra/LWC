using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Terraria_Server;
using com.griefcraft.command;

namespace LWC_TDSM.Command
{
    class TDSMCommandSender : CommandSender
    {

        /**
         * The sender to send to
         */
        private ISender handle;

        public TDSMCommandSender(ISender handle)
        {
            this.handle = handle;
        }

        public void sendMessage(string str)
        {
            foreach (string line in str.Split('\n'))
            {
                handle.sendMessage(line);
            }
        }

        public void sendLocalizedMessage(string str, params object[] objarr)
        {
            throw new NotImplementedException();
        }

        public bool hasPermission(string str)
        {
            return true; // TODO
        }
    }
}
