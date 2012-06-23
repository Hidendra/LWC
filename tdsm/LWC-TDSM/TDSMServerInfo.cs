using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Terraria_Server;
using com.griefcraft;

namespace LWC_TDSM
{
    class TDSMServerInfo : ServerInfo
    {
        private LWCPlugin plugin;

        public TDSMServerInfo(LWCPlugin plugin)
        {
            this.plugin = plugin;
        }

        public ServerMod getServerMod()
        {
            return ServerMod.TDSM;
        }

        public string getServerVersion()
        {
            return "TDSM #" + Statics.BUILD + " (Terraria " + Statics.VERSION_NUMBER + ")";
        }

        public string getLayerVersion()
        {
            return plugin.Version + " (C#)";
        }
    }
}
