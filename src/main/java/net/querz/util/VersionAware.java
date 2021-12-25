package net.querz.util;
import java.util.Map;
import java.util.TreeMap;

/**
 * Simple utility class for managing data version support.
 */
public class VersionAware<T> {
    private final TreeMap<Integer, T> versionedValues = new TreeMap<>();

    /**
     * Registers a value.
     * @param minVersion minimum version for which to return the given value.
     * @param value value to associate with the given version
     * @return self for chaining
     */
    public VersionAware<T> register(int minVersion, T value) {
        versionedValues.put(minVersion, value);
        return this;
    }

    /**
     * Gets the best value for the given version.
     * @param forVersion version of interest.
     * @return an entry with the greatest version less than or equal to forVersion, or null if there is no such version registered.
     */
    public T get(int forVersion) {
        Map.Entry<Integer, T> entry = versionedValues.floorEntry(forVersion);
        return entry != null ? entry.getValue() : null;
    }
}
