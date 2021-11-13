package net.querz.mca.entities;

import net.querz.mca.DataVersion;
import net.querz.nbt.tag.CompoundTag;
import net.querz.util.ArgValidator;

import java.util.*;

public class EntityUtil {

    public static final UUID ZERO_UUID = new UUID(0, 0);

    private EntityUtil() {}

    /**
     * May return null if tag does not contain expected UUID fields or contains ZERO UUID value.
     */
    public static UUID getUuid(int dataVersion, CompoundTag tag) {
        ArgValidator.requireValue(tag);
        long most;
        long least;
        if (dataVersion >= DataVersion.JAVA_1_16_0.id()) {
            int[] bits = tag.getIntArray("UUID");
            if (bits == null || bits.length != 4) return null;
            most = ((long)bits[0] << 32) | ((long)bits[1] & 0xFFFF_FFFFL);
            least = ((long)bits[2] << 32) | ((long)bits[3] & 0xFFFF_FFFFL);
        } else {
            most = tag.getLong("UUIDMost");
            least = tag.getLong("UUIDLeast");
        }
        if (most != 0 || least != 0) {
            return new UUID(most, least);
        } else {
            return null;
        }
    }

    /**
     * @param dataVersion controls tag format where 1.16+ stores an int array
     *                    and lesser versions store "most" and "least" longs
     * @param tag not null
     * @param uuid not null, not ZERO_UUID value
     */
    public static void setUuid(int dataVersion, CompoundTag tag, UUID uuid) {
        ArgValidator.requireValue(tag, "tag");
        ArgValidator.requireValue(uuid, "uuid");
        ArgValidator.check(!ZERO_UUID.equals(uuid), "zero uuid");
        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();
        if (dataVersion >= DataVersion.JAVA_1_16_0.id()) {
            int[] bits = new int[4];
            bits[0] = (int) (most >> 32);
            bits[1] = (int) (most & 0xFFFF_FFFFL);
            bits[2] = (int) (least >> 32);
            bits[3] = (int) (least & 0xFFFF_FFFFL);
            tag.putIntArray("UUID", bits);
        } else {
            tag.putLong("UUIDMost", most);
            tag.putLong("UUIDLeast", least);
        }
    }

    /**
     * Normalizes the given yaw to be in the range [0..360) by removing excessive rotations.
     */
    public static float normalizeYaw(float yaw) {
        yaw = yaw % 360;
        if (yaw < 0) yaw += 360;
        return yaw;
    }

    /**
     * Clamps the given pitch to to [-90..90]
     */
    public static float clampPitch(float pitch) {
        if (pitch < -90f) return -90f;
        if (pitch > 90f) return 90f;
        return pitch;
    }
}
