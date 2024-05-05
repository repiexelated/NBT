package io.github.ensgijs.nbt.mca.util;

/**
 * An XZ aligned bounding box that conceptually represents block coordinates.
 * @see ChunkBoundingRectangle
 * @see RegionBoundingRectangle
 */
public class BlockAlignedBoundingRectangle {

    protected final int widthBlockXZ;

    protected final int minBlockX;
    protected final int minBlockZ;
    protected final int maxBlockX;  // exclusive
    protected final int maxBlockZ;  // exclusive

    protected final double minXd;
    protected final double minZd;
    protected final double maxXd;  // exclusive
    protected final double maxZd;  // exclusive

    public int getMinBlockX() {
        return minBlockX;
    }

    public int getMinBlockZ() {
        return minBlockZ;
    }

    /** exclusive */
    public int getMaxBlockX() {
        return maxBlockX;
    }

    /** exclusive */
    public int getMaxBlockZ() {
        return maxBlockZ;
    }

    public int getWidthBlockXZ() {
        return widthBlockXZ;
    }

    public BlockAlignedBoundingRectangle(int minBlockX, int minBlockZ, int widthBlockXZ) {
        this.minXd = this.minBlockX = minBlockX;
        this.minZd = this.minBlockZ = minBlockZ;
        this.maxXd = this.maxBlockX = minBlockX + widthBlockXZ;
        this.maxZd = this.maxBlockZ = minBlockZ + widthBlockXZ;
        this.widthBlockXZ = widthBlockXZ;
    }

    public boolean containsBlock(int blockX, int blockZ) {
        return minBlockX <= blockX && blockX < maxBlockX && minBlockZ <= blockZ && blockZ < maxBlockZ;
    }

    public boolean containsBlock(IntPointXZ blockXZ) {
        return containsBlock(blockXZ.getX(), blockXZ.getZ());
    }

    public boolean containsBlock(double blockX, double blockZ) {
        return minXd <= blockX && blockX < maxXd && minZd <= blockZ && blockZ < maxZd;
    }

    /**
     * Constrains the given 3d bounding cuboid to this rectangle. Note the given bounds (min and max) are both inclusive.
     * @return If all corners are outside this rectangle, or if the given bounds are not valid, false is returned.
     */
    public final boolean constrain(int[] bounds) {
        if (bounds == null || bounds.length != 6) return false;
        if (bounds[0] > bounds[3] || bounds[2] > bounds[5])
            throw new IllegalArgumentException("Bounds not in min/max order!");
        boolean c1ok = containsBlock(bounds[0], bounds[2]);
        boolean c2ok = containsBlock(bounds[3], bounds[5]);
        if (c1ok && c2ok) {
            return true;
        }
        if (!c1ok && !c2ok) {
            if (bounds[0] >= maxBlockX || bounds[2] >= maxBlockZ || bounds[3] < minBlockX || bounds[5] < minBlockZ)
                return false;
        }

        if (!c1ok) {
            if (bounds[0] < minBlockX) bounds[0] = minBlockX;
            else if (bounds[0] >= maxBlockX) bounds[0] = maxBlockX - 1;
            if (bounds[2] < minBlockZ) bounds[2] = minBlockZ;
            else if (bounds[2] >= maxBlockZ) bounds[2] = maxBlockZ - 1;
        }

        if (!c2ok) {
            if (bounds[3] < minBlockX) bounds[3] = minBlockX;
            else if (bounds[3] >= maxBlockX) bounds[3] = maxBlockX - 1;
            if (bounds[5] < minBlockZ) bounds[5] = minBlockZ;
            else if (bounds[5] >= maxBlockZ) bounds[5] = maxBlockZ - 1;
        }

        return true;
    }

    @Override
    public String toString() {
        return String.format("blocks[%d %d to %d %d]",
                minBlockX, minBlockZ, maxBlockX - 1, maxBlockZ - 1);
    }
}
