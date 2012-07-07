using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Terraria_Server;
using com.griefcraft;
using com.griefcraft.@event;
using com.griefcraft.world;

namespace LWC_TDSM
{
    class TDSMPlayer : com.griefcraft.entity.Player
    {

        /**
         * The plugin object
         */
        private LWCPlugin plugin;

        /**
         * The player handle
         */
        private Player handle;

        /**
         * The event delegate
         */
        private PlayerEventDelegate eventDelegate;

        public TDSMPlayer(LWC lwc, LWCPlugin plugin, Player handle)
        {
            this.handle = handle;
            this.eventDelegate = new PlayerEventDelegate(lwc, this);
        }

        public override void sendMessage(string message)
        {
            foreach (string line in message.Split('\n'))
            {
                handle.sendMessage(line);
            }
        }

        public override bool hasPermission(string message)
        {
            // TODO
            return true;
        }

        public override string getName()
        {
            return handle.Name;
        }

        public override Location getLocation()
        {
            return new Location(LWCPlugin.MAIN_WORLD, handle.Location.X, handle.Location.Y, 0);
        }

        public override PlayerEventDelegate getEventDelegate()
        {
            return eventDelegate;
        }
    }
}
