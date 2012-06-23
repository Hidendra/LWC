using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Terraria_Server;
using com.griefcraft.world;

namespace LWC_TDSM.World
{
    class TDSMWorld : com.griefcraft.world.World
    {

        /**
         * The world's name
         */
        private string name;

        public TDSMWorld(string name)
        {
            this.name = name;
        }

        public string getName()
        {
            return name;
        }

        public Block getBlockAt(int x, int y, int z)
        {
            return new TDSMBlock(this, Main.tile.At(x, y), x, y);
        }
    }
}
