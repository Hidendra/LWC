using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Terraria;
using com.griefcraft.world;

namespace LWC_TShock.World
{
    class TShockBlock : Block
    {

        /**
         * The tile handle
         */
        private Tile handle;

        /**
         * The world this block is in
         */
        private com.griefcraft.world.World world;

        /**
         * The block's x coordinate
         */
        private int x;

        /**
         * The block's y coordinate
         */
        private int y;

        public TShockBlock(com.griefcraft.world.World world, Tile handle, int x, int y)
        {
            this.world = world;
            this.handle = handle;
            this.x = x;
            this.y = y;
        }

        public override int getType()
        {
            return handle.type;
        }

        public override byte getData()
        {
            return 0; // TODO
        }

        public override com.griefcraft.world.World getWorld()
        {
            return world;
        }

        public override int getX()
        {
            return x;
        }

        public override int getY()
        {
            return y;
        }

        public override int getZ()
        {
            return 0;
        }

        public override void setType(int type)
        {
            handle.type = (byte) type;
        }

        public override void setData(byte b)
        {
            // TODO
        }
    }
}
