package net.querz.util;

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

    public boolean contains(double x, double z) {
        return minXd <= x && x < maxXd && minZd <= z && z < maxZd;
    }
}
