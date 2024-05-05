package io.github.ensgijs.nbt.mca.util;

public class ChunkBoundingRectangle extends BlockAlignedBoundingRectangle {
    /**
     * Maximum size of the world boarder.
     * Note that there are 368 XZ blocks (23 XZ chunks) outside the world boarder that can possibly be generated.
     * <p>Remember that the max bound is exclusive (don't treat getMaxChunkXZ as in bounds).</p>
     * @see RegionBoundingRectangle#MAX_WORLD_REGION_BOUNDS
     */
    public static final ChunkBoundingRectangle MAX_WORLD_BOARDER_BOUNDS =
            new ChunkBoundingRectangle(-1874999, -1874999, 1874999 * 2);

    public ChunkBoundingRectangle(int chunkX, int chunkZ) {
        this(chunkX, chunkZ, 1);
    }

    public ChunkBoundingRectangle(int chunkX, int chunkZ, int chunkWidthXZ) {
        super(chunkX << 4, chunkZ << 4, chunkWidthXZ << 4);
    }

    public int getMinChunkX() {
        return minBlockX >> 4;
    }

    public int getMinChunkZ() {
        return minBlockZ >> 4;
    }

    /** exclusive */
    public int getMaxChunkX() {
        return maxBlockX >> 4;
    }

    /** exclusive */
    public int getMaxChunkZ() {
        return maxBlockZ >> 4;
    }

    public int getWidthChunkXZ() {
        return widthBlockXZ >> 4;
    }

    public boolean containsChunk(int chunkX, int chunkZ) {
        return containsBlock(chunkX << 4, chunkZ << 4);
    }

    public boolean containsChunk(IntPointXZ chunkXZ) {
        return containsBlock(chunkXZ.getX() << 4, chunkXZ.getZ() << 4);
    }

    /**
     * Calculates a new absolute X such that the given absolute X is inside this chunk
     * at the same relative location as the given X was relative to its source chunk.
     *
     * @param blockX in block coordinates
     * @return an X within the bounds of this chunk
     */
    public final int relocateX(int blockX) {
        return minBlockX | (blockX & 0xF);
    }

    /**
     * Calculates a new absolute Z such that the given absolute Z is inside this chunk
     * at the same relative location as the given Z was relative to its source chunk.
     *
     * @param blockZ in block coordinates
     * @return an Z within the bounds of this chunk
     */
    public final int relocateZ(int blockZ) {
        return minBlockZ | (blockZ & 0xF);
    }

    public final IntPointXZ relocate(IntPointXZ blockXZ) {
        return new IntPointXZ(minBlockX | (blockXZ.getX() & 0xF), minBlockZ | (blockXZ.getZ() & 0xF));
    }

    public final IntPointXZ relocate(int blockX, int blockZ) {
        return new IntPointXZ(minBlockX | (blockX & 0xF), minBlockZ | (blockZ & 0xF));
    }

    /**
     * Calculates a new absolute X such that the given absolute X is inside this chunk
     * at the same relative location as the given X was relative to its source chunk.
     *
     * @param blockX in block coordinates
     * @return an X within the bounds of this chunk
     */
    public final double relocateX(double blockX) {
        double bin = blockX % 16;
        return (bin >= 0 ? minBlockX : maxBlockX) + bin;
    }

    /**
     * Calculates a new absolute Z such that the given absolute Z is inside this chunk
     * at the same relative location as the given Z was relative to its source chunk.
     *
     * @param blockZ in block coordinates
     * @return an Z within the bounds of this chunk
     */
    public final double relocateZ(double blockZ) {
        double bin = blockZ % 16;
        return (bin >= 0 ? minBlockZ : maxBlockZ) + bin;
    }

    @Override
    public String toString() {
        return String.format("chunks[%d %d to %d %d]",
                getMinChunkX(), getMinChunkZ(), getMaxChunkX() - 1, getMaxChunkZ() - 1);
    }
}
