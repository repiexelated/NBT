package net.querz.util;

/**
 * An immutable Minecraft style 2D point where the dimensions are X and Z.
 * <p>Because this class is immutable (x and z cannot be changed), you don't need to worry about
 * cloning it or copying it - if you need the same value somewhere else, just use the same instance!</p>
 */
public class IntPointXZ {
    protected final int x;
    protected final int z;

    public IntPointXZ(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public IntPointXZ multiply(int multiplier) {
        return new IntPointXZ(x * multiplier, z * multiplier);
    }

    public IntPointXZ multiply(IntPointXZ multiplier) {
        return new IntPointXZ(x * multiplier.x, z * multiplier.z);
    }

    public IntPointXZ divide(int denominator) {
        return new IntPointXZ(x / denominator, z / denominator);
    }

    public IntPointXZ divide(IntPointXZ denominator) {
        return new IntPointXZ(x / denominator.x, z / denominator.z);
    }

    public IntPointXZ add(IntPointXZ other) {
        return new IntPointXZ(x + other.x, z + other.z);
    }

    public IntPointXZ add(int x, int z) {
        return new IntPointXZ(this.x + x, this.z + z);
    }

    public IntPointXZ subtract(IntPointXZ other) {
        return new IntPointXZ(x - other.x, z - other.z);
    }

    public IntPointXZ subtract(int x, int z) {
        return new IntPointXZ(this.x - x, this.z - z);
    }

    public IntPointXZ transformBlockToChunk() {
        return new IntPointXZ(x >> 4, z >> 4);
    }

    public IntPointXZ transformChunkToRegion() {
        return new IntPointXZ(x >> 5, z >> 5);
    }

    public IntPointXZ transformBlockToRegion() {
        return new IntPointXZ(x >> 9, z >> 9);
    }

    public IntPointXZ transformRegionToBlock() {
        return new IntPointXZ(x << 9, z << 9);
    }

    public IntPointXZ transformRegionToChunk() {
        return new IntPointXZ(x << 5, z << 5);
    }

    public IntPointXZ transformChunkToBlock() {
        return new IntPointXZ(x << 4, z << 4);
    }

    @Override
    public int hashCode() {
        return x * 31 + z;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof IntPointXZ)) return false;
        final IntPointXZ o = (IntPointXZ) other;
        return this.x == o.x && this.z == o.z;
    }
}
