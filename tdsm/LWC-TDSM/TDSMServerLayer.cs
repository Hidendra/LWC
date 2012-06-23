using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Terraria_Server;
using com.griefcraft;
using com.griefcraft.sql;
using com.griefcraft.world;
using java.lang;
using Player = com.griefcraft.entity.Player;

namespace LWC_TDSM
{
    class TDSMServerLayer : ServerLayer
    {
        private LWCPlugin plugin;

        public TDSMServerLayer(LWCPlugin plugin)
        {
            this.plugin = plugin;
        }

        public override bool isBlockProtectable(Block block)
        {
            return block.getType() == 21; // TODO
        }

        public override com.griefcraft.world.World getDefaultWorld()
        {
            return LWCPlugin.MAIN_WORLD; // TODO
        }

        protected override Player internalGetPlayer(string playerName)
        {
            foreach (Terraria_Server.Player handle in Main.players)
            {
                if (handle.Active && handle.Name.Equals(playerName))
                {
                    return new TDSMPlayer(plugin.LWC, plugin, handle);
                }
            }

            return null;
        }

        protected override com.griefcraft.world.World internalGetWorld(string str)
        {
            return LWCPlugin.MAIN_WORLD; // TODO ???
        }

        public override java.sql.Driver overrideJDBCDriver(JDBCDatabase.Driver driver)
        {
            // TODO SQLite
            if (driver == JDBCDatabase.Driver.MYSQL)
            {
                Class.forName(typeof(com.mysql.jdbc.Driver).AssemblyQualifiedName);
                return (java.sql.Driver) new com.mysql.jdbc.Driver();
            }

            return null;
        }

    }
}
