package net.rossquerz.mca.util;

public class BlockAlignedBoundingRectangle {
    final int widthXZ;

    final int minX;
    final int minZ;
    final int maxX;  // exclusive
    final int maxZ;  // exclusive

    final double minXd;
    final double minZd;
    final double maxXd;  // exclusive
    final double maxZd;  // exclusive

    public int getMinX() {
        return minX;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public int getWidthXZ() {
        return widthXZ;
    }

    public BlockAlignedBoundingRectangle(int minX, int minZ, int widthXZ) {
        this.minXd = this.minX = minX;
        this.minZd = this.minZ = minZ;
        this.maxXd = this.maxX = minX + widthXZ;
        this.maxZd = this.maxZ = minZ + widthXZ;
        this.widthXZ = widthXZ;
    }

    public boolean contains(int x, int z) {
        return minX <= x && x < maxX && minZ <= z && z < maxZ;
    }

    public boolean contains(IntPointXZ xz) {
        return contains(xz.getX(), xz.getZ());
    }

    public boolean contains(double x, double z) {
        return minXd <= x && x < maxXd && minZd <= z && z < maxZd;
    }

    /**
     * Constrains the given 3d bounding cuboid to this rectangle. Note the given bounds (min and max) are both inclusive.
     * @return If all corners are outside this rectangle, or if the given bounds are not valid, false is returned.
     */
    public boolean constrain(int[] bounds) {
        if (bounds == null || bounds.length != 6) return false;
        if (bounds[0] > bounds[3] || bounds[2] > bounds[5])
            throw new IllegalArgumentException("Bounds not in min/max order!");
        boolean c1ok = contains(bounds[0], bounds[2]);
        boolean c2ok = contains(bounds[3], bounds[5]);
        if (c1ok && c2ok) {
            return true;
        }
        if (!c1ok && !c2ok) {
            if (bounds[0] >= maxX || bounds[2] >= maxZ || bounds[3] < minX || bounds[5] < minZ)
                return false;
        }

        if (!c1ok) {
            if (bounds[0] < minX) bounds[0] = minX;
            else if (bounds[0] >= maxX) bounds[0] = maxX - 1;
            if (bounds[2] < minZ) bounds[2] = minZ;
            else if (bounds[2] >= maxZ) bounds[2] = maxZ - 1;
        }

        if (!c2ok) {
            if (bounds[3] < minX) bounds[3] = minX;
            else if (bounds[3] >= maxX) bounds[3] = maxX - 1;
            if (bounds[5] < minZ) bounds[5] = minZ;
            else if (bounds[5] >= maxZ) bounds[5] = maxZ - 1;
        }

        return true;
    }
}
