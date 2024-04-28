package net.rossquerz.mca.util;

public class RegionBoundingRectangle extends BlockAlignedBoundingRectangle {
    protected final int regionX;
    protected final int regionZ;

    public RegionBoundingRectangle(int regionX, int regionZ) {
        super(regionX << 9, regionZ << 9, 512);
        this.regionX = regionX;
        this.regionZ = regionZ;
    }

    public int getRegionX() {
        return regionX;
    }

    public int getRegionZ() {
        return regionZ;
    }

    public boolean containsChunk(int x, int z) {
        return super.contains(x << 4, z << 4);
    }

    public boolean containsChunk(IntPointXZ xz) {
        return super.contains(xz.getX() << 4, xz.getZ() << 4);
    }

    public static RegionBoundingRectangle forChunk(int x, int z) {
        return new RegionBoundingRectangle(x >> 5, z >> 5);
    }
}
