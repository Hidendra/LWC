using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using TShockAPI;
using com.griefcraft;
using com.griefcraft.@event;
using com.griefcraft.entity;
using com.griefcraft.world;

namespace LWC_TShock.entity
{
    class TShockPlayer : Player
    {

        /**
         * The plugin object
         */
        private LWCPlugin plugin;

        /**
         * The player's handle
         */
        private TSPlayer handle;

        /**
         * The event delegate
         */
        private PlayerEventDelegate eventDelegate;

        public TShockPlayer(LWC lwc, TSPlayer handle)
        {
            this.handle = handle;
            this.eventDelegate = new PlayerEventDelegate(lwc, this);
        }

        public override void sendMessage(string message)
        {
            foreach (string line in message.Split('\n'))
            {
                handle.SendMessage(line);
            }
        }

        public override void sendLocalizedMessage(string node, object[] arguments)
        {
            throw new NotImplementedException();
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
            return new Location(LWCPlugin.MAIN_WORLD, handle.X, handle.Y, 0);
        }

        public override PlayerEventDelegate getEventDelegate()
        {
            return eventDelegate;
        }

    }
}
