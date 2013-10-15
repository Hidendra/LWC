package org.getlwc;

public enum BlockFace {

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

}
