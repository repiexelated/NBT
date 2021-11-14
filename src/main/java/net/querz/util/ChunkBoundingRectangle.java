package net.querz.util;

public class ChunkBoundingRectangle extends BlockAlignedBoundingRectangle {
    protected final int chunkX;
    protected final int chunkZ;

    public ChunkBoundingRectangle(int chunkX, int chunkZ) {
        super(chunkX << 4, chunkZ << 4, 16);
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    /**
     * Calculates a new absolute X such that the given absolute X is inside this chunk
     * at the same relative location as the given X was relative to its source chunk.
     *
     * @param x in block coordinates
     * @return an X within the bounds of this chunk
     */
    public int relocateX(int x) {
        return minX | (x & 0xF);
    }

    /**
     * Calculates a new absolute Z such that the given absolute Z is inside this chunk
     * at the same relative location as the given Z was relative to its source chunk.
     *
     * @param z in block coordinates
     * @return an Z within the bounds of this chunk
     */
    public int relocateZ(int z) {
        return minZ | (z & 0xF);
    }

    /**
     * Calculates a new absolute X such that the given absolute X is inside this chunk
     * at the same relative location as the given X was relative to its source chunk.
     *
     * @param x in block coordinates
     * @return an X within the bounds of this chunk
     */
    public double relocateX(double x) {
        double bin = x % 16;
        return (bin >= 0 ? minX : maxX) + bin;
    }

    /**
     * Calculates a new absolute Z such that the given absolute Z is inside this chunk
     * at the same relative location as the given Z was relative to its source chunk.
     *
     * @param z in block coordinates
     * @return an Z within the bounds of this chunk
     */
    public double relocateZ(double z) {
        double bin = z % 16;
        return (bin >= 0 ? minZ : maxZ) + bin;
    }
}
