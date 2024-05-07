package io.github.ensgijs.nbt.mca.util;

public class RegionBoundingRectangle extends ChunkBoundingRectangle {
    /**
     * Bounds of the maximum size of a minecraft world.
     * Note that there are 368 XZ blocks (23 XZ chunks) outside the world boarder that are still within these region bounds.
     * <p>Remember that the max bound is exclusive (don't treat getMaxRegionXZ as in bounds).</p>
     * @see ChunkBoundingRectangle#MAX_WORLD_BOARDER_BOUNDS
     */
    public static final RegionBoundingRectangle MAX_WORLD_REGION_BOUNDS =
            new RegionBoundingRectangle(-58594, -58594, 58594 * 2);

    public RegionBoundingRectangle(int regionX, int regionZ) {
        this(regionX, regionZ, 1);
    }

    public RegionBoundingRectangle(int regionX, int regionZ, int widthXZ) {
        super(regionX << 5, regionZ << 5, widthXZ << 5);
    }

    public boolean containsRegion(int regionX, int regionZ) {
        return containsBlock(regionX << 9, regionZ << 9);
    }

    public boolean containsRegion(IntPointXZ regionXZ) {
        return containsBlock(regionXZ.getX() << 9, regionXZ.getZ() << 9);
    }

    public final int getMinRegionX() {
        return minBlockX >> 9;
    }

    public final int getMinRegionZ() {
        return minBlockZ >> 9;
    }

    /** exclusive */
    public final int getMaxRegionX() {
        return maxBlockX >> 9;
    }

    /** exclusive */
    public final int getMaxRegionZ() {
        return maxBlockZ >> 9;
    }

    public final int getWidthRegionXZ() {
        return widthBlockXZ >> 9;
    }

    public static RegionBoundingRectangle forChunk(int x, int z) {
        return new RegionBoundingRectangle(x >> 5, z >> 5);
    }

    public static RegionBoundingRectangle forBlock(int x, int z) {
        return new RegionBoundingRectangle(x >> 9, z >> 9);
    }

    public ChunkBoundingRectangle asChunkBounds() {
        return new ChunkBoundingRectangle(getMinChunkX(), getMinChunkZ(), getWidthChunkXZ());
    }

    @Override
    public String toString() {
        return String.format("regions[%d..%d, %d..%d]",
                getMinRegionX(), getMaxRegionX() - 1, getMinRegionZ(), getMaxRegionZ() - 1);
    }
}
