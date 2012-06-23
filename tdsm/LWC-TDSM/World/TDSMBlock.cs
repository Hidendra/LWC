using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Terraria_Server;
using com.griefcraft.world;

namespace LWC_TDSM.World
{
    class TDSMBlock : Block
    {

        /**
         * The world this block is in
         */
        private com.griefcraft.world.World world;

        /**
         * The tile handle
         */
        private TileRef handle;

        /**
         * we can't read the x/y value from the tileref for whatever reason
         */
        private int x;

        /**
         * we can't read the x/y value from the tileref for whatever reason
         */
        private int y;

        public TDSMBlock(com.griefcraft.world.World world, TileRef handle, int x, int y)
        {
            this.world = world;
            this.handle = handle;
            this.x = x;
            this.y = y;
        }

        public override int getType()
        {
            return handle.Type;
        }

        public override byte getData()
        {
            return 0; // TODO encode/decode handle.Data ?
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
            handle.SetType((byte) type);
        }

        public override void setData(byte data)
        {
            throw new NotImplementedException();
        }
    }
}
