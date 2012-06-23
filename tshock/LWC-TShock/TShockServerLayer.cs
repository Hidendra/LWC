using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using LWC_TShock.entity;
using TShockAPI;
using com.griefcraft;
using com.griefcraft.entity;
using com.griefcraft.sql;
using com.griefcraft.world;
using java.lang;
using java.sql;

namespace LWC_TShock
{
    class TShockServerLayer : ServerLayer
    {

        private LWCPlugin plugin;

        public TShockServerLayer(LWCPlugin plugin)
        {
            this.plugin = plugin;
        }

        public override bool isBlockProtectable(Block block)
        {
            return block.getType() == 21; // TODO
        }

        public override com.griefcraft.world.World getDefaultWorld()
        {
            return LWCPlugin.MAIN_WORLD;
        }

        protected override Player internalGetPlayer(string playerName)
        {
            List<TSPlayer> handles = TShock.Utils.FindPlayer(playerName);

            if (handles.Count > 0)
            {
                return new TShockPlayer(plugin.LWC, handles[0]);
            }

            return null;
        }

        protected override com.griefcraft.world.World internalGetWorld(string str)
        {
            return LWCPlugin.MAIN_WORLD; // TODO ???
        }

    }
}
