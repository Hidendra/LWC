using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Terraria_Server;
using Terraria_Server.Logging;
using com.griefcraft.command;

namespace LWC_TDSM
{
    class TDSMConsoleCommandSender : ConsoleCommandSender
    {
        public void sendMessage(string str)
        {
            Log(str);
        }

        public void sendLocalizedMessage(string str, params object[] objarr)
        {
            throw new NotImplementedException();
        }

        public bool hasPermission(string str)
        {
            return true;
        }

        private void Log(string message)
        {
            ProgramLog.Log("[LWC] " + message);
        }

    }
}
