using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using LWC_TShock.World;
using Terraria;
using com.griefcraft.world;

namespace LWC_TShock
{
    class TShockWorld : com.griefcraft.world.World
    {

        // the world's name
        private string name;

        public TShockWorld(string name)
        {
            this.name = name;
        }

        public string getName()
        {
            return name;
        }

        public Block getBlockAt(int x, int y, int z)
        {
            return new TShockBlock(this, Main.tile[x, y], x, y);
        }
    }
}
