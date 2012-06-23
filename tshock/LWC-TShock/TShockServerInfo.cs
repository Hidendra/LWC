using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using TShockAPI;
using Terraria;
using com.griefcraft;

namespace LWC_TShock
{
    class TShockServerInfo : ServerInfo
    {

        private LWCPlugin plugin;

        public TShockServerInfo(LWCPlugin plugin)
        {
            this.plugin = plugin;
        }

        public ServerMod getServerMod()
        {
            return ServerMod.TSHOCK;
        }

        public string getServerVersion()
        {
            return "TShock " + TShock.VersionNum.ToString();
        }

        public string getLayerVersion()
        {
            return plugin.Version + " (C#)";
        }

    }
}
