package org.getlwc;

public enum BlockFace {

    UP(0, 1, 0),
    DOWN(0, -1, 0),
    NORTH(0, 0, -1),
    EAST(1, 0, 0),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0);

    private final int deltaX;
    private final int deltaY;
    private final int deltaZ;

    private BlockFace(final int deltaX, final int deltaY, final int deltaZ) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
    }

    /**
     * @return
     */
    public int getDeltaX() {
        return deltaX;
    }

    /**
     * @return
     */
    public int getDeltaY() {
        return deltaY;
    }

    /**
     * @return
     */
    public int getDeltaZ() {
        return deltaZ;
    }

    /**
     * Convert from Notchian block faces to native BlockFace
     *
     * @param face
     * @return
     */
    public static BlockFace fromNotch(int face) {
        switch (face) {
            case 0:
                return BlockFace.DOWN;
            case 1:
                return BlockFace.UP;
            case 2:
                return BlockFace.NORTH;
            case 3:
                return BlockFace.SOUTH;
            case 4:
                return BlockFace.WEST;
            case 5:
                return BlockFace.EAST;
            default:
                throw new UnsupportedOperationException("Notchian block face " + face + " is not supported");
        }
    }

}
