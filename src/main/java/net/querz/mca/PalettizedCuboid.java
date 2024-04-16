package net.querz.mca;

import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.LongArrayTag;
import net.querz.nbt.tag.Tag;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static net.querz.mca.DataVersion.JAVA_1_16_20W17A;

/**
 * A PalettizedCuboid is serialized as a {@link net.querz.nbt.tag.CompoundTag} containing {@code data}
 * and {@code palette} entries. {@code data} is always of type {@link net.querz.nbt.tag.LongArrayTag}
 * while {@code palette} is a {@link net.querz.nbt.tag.ListTag} of some type {@link E}. If {@code palette}
 * contains only a single value then {@code data} is omitted and all data values are assumed to be the same.
 *
 * <p>The {@link #size()} is fixed and a cube of a power of two, typically 16 or 4.</p>
 * <p>A cuboid is a 3D rectangle, however in MCA chunk's x, y, and z sizes are all equal (currently).</p>
 *
 * <h2>NBT Examples</h2>
 * <h3>block_states</h3>
 * <pre>
 * "block_states": {
 *   "type": "CompoundTag",
 *   "value": {
 *     "data": {
 *       "type": "LongArrayTag",
 *       "value": [...]
 *     },
 *     "palette": {
 *       "type": "ListTag",
 *       "value": {
 *         "type": "CompoundTag",
 *         "list": [
 *           {
 *             "Name": {
 *               "type": "StringTag",
 *               "value": "minecraft:bedrock"
 *             }
 *           },
 *           ...
 *         ]
 *       }
 *     }
 *   }
 * </pre>
 * <h3>biomes</h3>
 * <pre>
 * "biomes": {
 *   "type": "CompoundTag",
 *   "value": {
 *     "data": {
 *       "type": "LongArrayTag",
 *       "value": [
 *         0,
 *         -7686143365460459264
 *       ]
 *     },
 *     "palette": {
 *       "type": "ListTag",
 *       "value": {
 *         "type": "StringTag",
 *         "list": [
 *           "minecraft:deep_dark",
 *           "minecraft:snowy_taiga",
 *           "minecraft:grove"
 *         ]
 *       }
 *     }
 *   }
 * </pre>
 */
public class PalettizedCuboid<E extends Tag<?>> implements Iterable<E>, Cloneable {
    private final int cordBitMask;
    private final int zShift;
    private final int yShift;

    /**
     * May be a sparse array and contain nulls, however, there should be no value which references a null palette index.
     * This is to simplify mutations and differ compaction / remapping to a convenient time.
     */
    protected final List<E> palette = new ArrayList<>();
    protected final Class<E> paletteEntryClass;
    protected final int[] data;

    /**
     * @param cubeEdgeLength The size of this {@link PalettizedCuboid} will be {@code cubeEdgeLength^3}.
     *                       The value of {@code cubeEdgeLength} is typically 16 or 4.
     *                       Must be a power of 2.
     * @param fillWith Required. Value to fill the initial {@link PalettizedCuboid} with.
     *                       The given value is cloned for each entry in the cuboid.
     */
    @SuppressWarnings("unchecked")
    public PalettizedCuboid(int cubeEdgeLength, E fillWith) {
        this(cubeEdgeLength, fillWith, (Class<E>) fillWith.getClass(), false);
    }

    @SuppressWarnings("unchecked")
    protected PalettizedCuboid(int cubeEdgeLength, E fillWith, Class<E> paletteEntryClass, boolean allowNullFill) {
        if (fillWith == null && !allowNullFill) throw new NullPointerException("fillWith must not be null");
        this.paletteEntryClass = paletteEntryClass;
        int bits = calculatePowerOfTwoExponent(cubeEdgeLength, true);
        this.cordBitMask = calculateBitMask(bits);
        this.zShift = bits;
        this.yShift = bits * 2;
        this.data = new int[cubeEdgeLength * cubeEdgeLength * cubeEdgeLength];
        Arrays.fill(this.data, 0);
        if (fillWith != null) {
            this.palette.add((E) fillWith.clone());
        }
    }

    /**
     * @param values Must be of a length that has a cube root and that root must itself be a power of 2.
     *               Ex. 4096 is 16^3 and 16 is 2^4; Ex. 64 is 4^3 and 4 is 2^2. The given values are cloned,
     *               neither the given values array nor its elements are taken by reference. However, any value
     *               which is repeated is only entered into the palette once.
     */
    @SuppressWarnings("unchecked")
    public PalettizedCuboid(E[] values) {
        this(cubeRoot(values.length), null, (Class<E>) values.getClass().getComponentType(), true);
        Map<E, Integer> indexLookup = new HashMap<>();
        for (int i = 0; i < data.length; i++) {
            if (values[i] == null) throw new NullPointerException("values must not contain nulls!");
            int paletteIndex = indexLookup.computeIfAbsent(values[i], k -> {
                palette.add((E) k.clone());
                return palette.size() - 1;
            });
            this.data[i] = paletteIndex;
        }
    }

    static int cubeRoot(int num) {
        int k = (int) Math.round(Math.pow(num, 1/3d));
        if (k * k * k != num)
            throw new IllegalArgumentException("the cube root of " + num + " is not an integer!");
        return k;
    }

    /**
     * If strict is true and num is not a power of 2 then IllegalArgumentException is thrown.
     * <p>If strict is false and num is not a power of 2 then the return value is the power of 2 which
     * is large enough to include num. Ex. num = 7 -> 3 (2^3 = 8)</p>
     */
    static int calculatePowerOfTwoExponent(int num, boolean strict) {
        int k = 0;
        boolean bump = false;
        while (num > 1) {
            k++;
            if (num % 2 != 0) {
                if (strict)
                    throw new IllegalArgumentException(num + " isn't a power of two!");
                else
                    bump = true;
            }
            num /= 2;
        }
        return k + (bump ? 1 : 0);
    }

    static int calculateBitMask(int numberOfBits) {
        if (numberOfBits < 0 || numberOfBits >= 32) throw new IllegalArgumentException();
        return ~(-1 << numberOfBits);
    }

    /** size of data array (ex. 64 for a 4x4x4 cuboid) */
    public int size() {
        return data.length;
    }

    /**
     * Length of one edge of the cuboid (ex. 4 for a 4x4x4 cuboid).<br>
     * <b>Note: this is a somewhat expensive call as it computes the value with each call using
     * {@link Math#pow(double, double)} and {@link Math#round(double)}.</b>
     */
    public int cubeEdgeLength() {
        return cubeRoot(data.length);
    }

    /** The current palette size. Note for an exact accurate palette count call {@link #optimizePalette()} first. */
    public int paletteSize() {
        return palette.size();
    }

    public boolean contains(E o) {
        return palette.contains(o);
    }

    public int countIf(Predicate<E> filter) {
        Set<Integer> counting = new HashSet<>();
        for (int i = 0; i < palette.size(); i++) {
            E paletteValue = palette.get(i);
            if (paletteValue != null && filter.test(paletteValue)) {
                counting.add(i);
            }
        }
        return (int) Arrays.stream(data).filter(counting::contains).count();
    }

    /**
     * Returns a copy of the palette values for every position in this cuboid.
     * <p>Modifying the returned value can be done safely, it will have no effect on this cuboid.</p>
     * <p>To avoid the overhead of making a copy use {@link #getByRef(int)} instead.</p>
     */
    @SuppressWarnings("unchecked")
    public E[] toArray() {
        E[] a = (E[]) java.lang.reflect.Array.newInstance(paletteEntryClass, data.length);
        for (int i = 0; i < data.length; i++) {
            a[i] = (E) palette.get(data[i]).clone();
        }
        return a;
    }

    /**
     * Returns the palette value for every position in this cuboid.
     * <p><b>WARNING if the returned Tags are modified it modifies every value which references the same palette entry!</b></p>
     * <p>Modifying the returned array itself does not change the cuboid.</p>
     */
    @SuppressWarnings("unchecked")
    public E[] toArrayByRef() {
        E[] a = (E[]) java.lang.reflect.Array.newInstance(paletteEntryClass, data.length);
        for (int i = 0; i < data.length; i++) {
            a[i] = palette.get(data[i]);
        }
        return a;
    }

    @SuppressWarnings("unchecked")
    private boolean replace(Collection<Integer> replacing, E replacement) {
        replacing.remove(-1);
        if (replacing.isEmpty()) return false;
        int replacementPaletteIndex = palette.indexOf(replacement);
        boolean addReplacementToPaletteIfDataModified;
        if (replacementPaletteIndex < 0) {
            replacementPaletteIndex = palette.size();
            addReplacementToPaletteIfDataModified = true;
        } else {
            replacing.remove(replacementPaletteIndex);
            if (replacing.isEmpty()) return false;
            addReplacementToPaletteIfDataModified = false;
        }

        // TODO: run some simulations to see if this "optimization" matters at all and if it does, tune the list/set threshold
        if (replacing.size() == 1) {
            replacing = Collections.singleton(replacing.iterator().next());
        } else if (replacing.size() < 5) {
            // small lists are (generally) faster than small hashsets
            if (!(replacing instanceof List))
                replacing = new ArrayList<>(replacing);
        } else if (!(replacing instanceof Set)) {
            replacing =  new HashSet<>(replacing);
        }

        boolean modified = false;
        for (int i = 0; i < data.length; i++) {
            if (replacing.contains(data[i])) {
                modified = true;
                data[i] = replacementPaletteIndex;
            }
        }
        if (modified) {
            for (int i : replacing) {
                palette.set(i, null);
            }
            if (addReplacementToPaletteIfDataModified)
                palette.add((E) replacement.clone());
        }
        return modified;
    }

    /**
     * @return True if any modifications were made, false otherwise.
     */
    public boolean replace(E oldValue, E newValue) {
        if (oldValue == null) throw new NullPointerException("oldValue required");
        if (newValue == null) throw new NullPointerException("newValue required");
        if (oldValue.equals(newValue)) return false;
        // Don't pass a singleton list/set type - they are immutable and will cause errors.
        return replace(new ArrayList<>(Collections.singletonList(palette.indexOf(oldValue))), newValue);
    }

    public final boolean replaceAll(E[] a, E replacement) {
        return replaceAll(Arrays.asList(a), replacement);
    }

    public boolean replaceAll(Collection<E> c, E replacement) {
        if (replacement == null) throw new NullPointerException("replacement required");
        if (c.isEmpty()) return false;
        Set<Integer> replacing = new HashSet<>();
        for (E e : c) {
            int i = palette.indexOf(e);
            if (i >= 0) {
                replacing.add(i);
            }
        }
        return replace(replacing, replacement);
    }

    public boolean replaceIf(Predicate<E> filter, E replacement) {
        if (replacement == null) throw new NullPointerException("replacement required");
        Set<Integer> replacing = new HashSet<>();
        for (int i = 0; i < palette.size(); i++) {
            E paletteValue = palette.get(i);
            if (paletteValue != null && filter.test(paletteValue)) {
                replacing.add(i);
            }
        }
        return replace(replacing, replacement);
    }

    public final boolean retainAll(E[] a, E replacement) {
        return retainAll(Arrays.asList(a), replacement);
    }

    public boolean retainAll(Collection<E> c, E replacement) {
        if (replacement == null) throw new NullPointerException("replacement required");
        Set<Integer> replacing = new HashSet<>();
        for (int i = 0; i < palette.size(); i++) {
            E paletteValue = palette.get(i);
            if (paletteValue != null && !c.contains(paletteValue)) {
                replacing.add(i);
            }
        }
        return replace(replacing, replacement);
    }

    /**
     * Sets the entire volume to the given value.
     * @param fillWith value to fill volume with, this value is cloned (not taken by reference).
     */
    @SuppressWarnings("unchecked")
    public void fill(E fillWith) {
        palette.clear();
        palette.add((E) fillWith.clone());
        Arrays.fill(data, 0);
    }

    /**
     * Computes the wrapped index of XYZ. If, for example, xSize is 16 and a value of 20 is passed
     * for X it will behave the same as if a value of 4 were passed instead.
     * @param x X index
     * @param y Y index
     * @param z Z index
     * @return wrapped element index
     */
    public int indexOf(int x, int y, int z) {
        return ((y & cordBitMask) << yShift) | ((z & cordBitMask) << zShift) | (x & cordBitMask);
    }

    /**
     * Returns a copy of the palette value at the specified position in this cuboid.
     * <p>Modifying the returned value can be done safely, it will have no effect on this cuboid.</p>
     * <p>To avoid the overhead of making a copy use {@link #getByRef(int)} instead.</p>
     *
     * @param index index of the element to return
     * @return the element at the specified position in this cuboid
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    @SuppressWarnings("unchecked")
    public E get(int index) {
        return (E) palette.get(data[index]).clone();
    }

    /**
     * Returns the palette value at the specified position in this cuboid.
     * <p><b>WARNING if the returned value is modified it modifies every value which references the same palette entry!</b></p>
     *
     * @param index index of the element to return
     * @return the element at the specified position in this cuboid
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public E getByRef(int index) {
        return palette.get(data[index]);
    }

    /**
     * Replaces the element at the specified position in this cuboid with
     * the specified element.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    @SuppressWarnings("unchecked")
    public void set(int index, E element) {
        if (index < 0 || index >= data.length) throw new IndexOutOfBoundsException();
        int paletteIndex = palette.indexOf(element);
        if (paletteIndex < 0) {
            paletteIndex = palette.size();
            palette.add((E) element.clone());
        }
        data[index] = paletteIndex;
    }

    /**
     * Replaces the element at the specified position in this cuboid with
     * the specified element.
     *
     * <p>Never throws IndexOutOfBoundsException. XYZ are always wrapped into bounds.</p>
     * @param x X index of the element to replace
     * @param y Y index of the element to replace
     * @param z Z index of the element to replace
     * @param element element to be stored at the specified position
     */
    public void set(int x, int y, int z, E element) {
        set(indexOf(x, y, z), element);
    }

    /**
     * Returns a copy of the palette value at the specified position in this cuboid.
     * <p>Modifying the returned value can be done safely, it will have no effect on this cuboid.</p>
     * <p>To avoid the overhead of making a copy use {@link #getByRef(int, int, int)} instead.</p>
     *
     * <p>Never throws IndexOutOfBoundsException. XYZ are always wrapped into bounds.</p>
     * @return the element at the specified position in this cuboid.
     */
    public E get(int x, int y, int z) {
        return get(indexOf(x, y, z));
    }

    /**
     * Returns the palette value at the specified position in this cuboid.
     * <p><b>WARNING if the returned value is modified it modifies every value which references the same palette entry!</b></p>
     *
     * <p>Never throws IndexOutOfBoundsException. XYZ are always wrapped into bounds.</p>
     * @return the element at the specified position in this cuboid.
     */
    public E getByRef(int x, int y, int z) {
        return getByRef(indexOf(x, y, z));
    }

    /**
     * Removes nulls from the palette and remaps value references as-needed.
     * @return true if any modifications were made
     */
    protected boolean optimizePalette() {
        // 1. identify unused palette id's & remove them from palette
        Set<Integer> seenIds = new HashSet<>();
        for (int id : data) {
            seenIds.add(id);
        }
        if (seenIds.stream().mapToInt(v -> v).max().getAsInt() >= palette.size()) {
            throw new IllegalStateException("data[] contained an out of bounds palette id!");
        }
        for (int i = 0; i < palette.size(); i++) {
            if (!seenIds.contains(i))
                palette.set(i, null);
        }

        // 2. calculate palette defragmentation
        int cursor = 0;
        Map<Integer, Integer> remapping = new HashMap<>();
        for (int i = 0; i < palette.size(); i++) {
            if (palette.get(i) != null) {
                if (i != cursor) remapping.put(i, cursor);
                cursor++;
            }
        }

        // 3. remove nulls from palette
        palette.removeAll(Collections.singletonList(null));

        // 4. perform id remapping
        if (remapping.isEmpty()) return false;
        for (int i = 0; i < data.length; i++) {
            int remappedPaletteIndex = remapping.getOrDefault(data[i], -1);
            if (remappedPaletteIndex >= 0) {
                data[i] = remappedPaletteIndex;
            }
        }
        return true;
    }


    @Override
    @SuppressWarnings("unchecked")
    public PalettizedCuboid<E> clone() {
        optimizePalette();
        PalettizedCuboid<E> clone = new PalettizedCuboid<>(cubeEdgeLength(), null, paletteEntryClass, true);
        for (E e : this.palette) {
            clone.palette.add((E) e.clone());
        }
        System.arraycopy(this.data, 0, clone.data, 0, data.length);
        return clone;
    }

    /** Serializes this cuboid to a {@link CompoundTag} assuming the latest data version (fine if only working with &gt;= JAVA_1_16_20W17A). */
    public CompoundTag toCompoundTag() {
        return toCompoundTag(DataVersion.latest().id());
    }

    /** Serializes this cuboid to a {@link CompoundTag} */
    public CompoundTag toCompoundTag(int dataVersion) {
        optimizePalette();
        CompoundTag rootTag = new CompoundTag(2);
        ListTag<E> paletteListTag = new ListTag<>(paletteEntryClass, palette.size());
        paletteListTag.addAll(palette);
        rootTag.put("palette", paletteListTag);

        if (palette.size() > 1) {
            final int bitsPerValue = calculatePowerOfTwoExponent(palette.size(), false);
            final long[] longs;
            if (dataVersion >= JAVA_1_16_20W17A.id()) {
                // This bit packing is less than 100% efficient as there may be "unused" bits in every long.
                // For example, if bitsPerValue = 5 then there will be 12 indices per long using only 60 of 64 bits.
                final int indicesPerLong = (int) (64D / bitsPerValue);
                longs = new long[(int) Math.ceil((double) data.length / indicesPerLong)];
                for (int ll = 0, i = 0; ll < longs.length; ll++) {
                    long currentLong = 0;
                    for (int j = 0; j < indicesPerLong && i < data.length; j++, i++) {
                        currentLong |= (long) data[i] << (j * bitsPerValue);
                    }
                    longs[ll] = currentLong;
                }
            } else {
                // TODO - it's a hot tight packing mess! see TerrainSectionBase#setPaletteIndex
                throw new UnsupportedOperationException("currently only support dataVersion >= JAVA_1_16_20W17A");
            }

            rootTag.put("data", new LongArrayTag(longs));
        }
        return rootTag;
    }

    public static <T extends Tag<?>> PalettizedCuboid<T> fromCompoundTag(CompoundTag tag, int expectedCubeEdgeLength) {
        return fromCompoundTag(tag, expectedCubeEdgeLength, DataVersion.latest().id());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Tag<?>> PalettizedCuboid<T> fromCompoundTag(CompoundTag tag, int expectedCubeEdgeLength, int dataVersion) {
        ListTag<T> paletteListTag = tag.getListTagAutoCast("palette");
        if (paletteListTag == null) throw new IllegalArgumentException("Did not find 'palette' ListTag");
        if (paletteListTag.isEmpty()) throw new IllegalArgumentException("'palette' ListTag exists but it was empty!");
        LongArrayTag dataTag = tag.getLongArrayTag("data");
        if ((dataTag == null || dataTag.getValue().length == 0) && paletteListTag.size() > 1)
            throw new IllegalArgumentException("Did not find 'data' LongArrayTag when expected");

        if (dataVersion >= JAVA_1_16_20W17A.id()) {
            if (dataTag == null) {
                return new PalettizedCuboid<>(expectedCubeEdgeLength, paletteListTag.get(0));
            }
            final int bitsPerValue = calculatePowerOfTwoExponent(paletteListTag.size(), false);
            final int indicesPerLong = (int) (64D / bitsPerValue);
            final int bitMask = calculateBitMask(bitsPerValue);
            PalettizedCuboid<T> palettizedCuboid = new PalettizedCuboid<>(expectedCubeEdgeLength, null, (Class<T>) paletteListTag.getTypeClass(), true);
            for (T t : paletteListTag) {
                palettizedCuboid.palette.add(t);
            }
            final int size = palettizedCuboid.size();
            final long[] longs = dataTag.getValue();
            if (longs.length != Math.ceil((double) size / indicesPerLong))
                throw new IllegalArgumentException("Incorrect data size! Expected " + (size / indicesPerLong) + " but was " + longs.length);
            for (int ll = 0, i = 0; ll < longs.length; ll++) {
                long currentLong = longs[ll];
                for (int j = 0; j < indicesPerLong && i < size; j++, i++) {
                    palettizedCuboid.data[i] = (int) (currentLong & bitMask);
                    currentLong >>>= bitsPerValue;
                }
            }
            return palettizedCuboid;
        } else {
            // TODO - it's a hot tight packing mess! see TerrainSectionBase#setPaletteIndex
            throw new UnsupportedOperationException("currently only support dataVersion >= JAVA_1_16_20W17A");
        }
    }

    @Override
    public PalettizedCuboidIterator iterator() {
        return new PalettizedCuboidIterator();
    }

    public PalettizedCuboidByRefIterator iteratorByRef() {
        return new PalettizedCuboidByRefIterator();
    }

    public Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public void forEachByRef(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        PalettizedCuboidByRefIterator iter = iteratorByRef();
        while (iter.hasNext()) {
            action.accept(iter.next());
        }
    }

    public Spliterator<E> spliteratorByRef() {
        return Spliterators.spliteratorUnknownSize(iteratorByRef(), 0);
    }

    public Stream<E> streamByRef() {
        return StreamSupport.stream(spliteratorByRef(), false);
    }

    public class PalettizedCuboidIterator implements Iterator<E> {
        private int currentIndex = -1;
        @Override
        public boolean hasNext() {
            return currentIndex < data.length - 1;
        }

        /**
         * Gets a copy of the palette value at the specified position in this cuboid.
         * <p>Modifying the returned value can be done safely, it will have no effect on this cuboid.</p>
         * <p>To avoid the overhead of making a copy use {@link #nextByRef()} instead.</p>
         */
        @Override
        public E next() {
            if (!hasNext()) throw new NoSuchElementException();
            return get(++currentIndex);
        }

        /**
         * Gets the next palette value at the specified position in this cuboid BY REFERENCE.
         * <p><b>WARNING if the returned value is modified it modifies every value which references the same palette entry!</b></p>
         */
        public E nextByRef() {
            if (!hasNext()) throw new NoSuchElementException();
            return getByRef(++currentIndex);
        }

        public E current() {
            if (currentIndex < 0) throw new NoSuchElementException();
            return get(currentIndex);
        }

        public E currentByRef() {
            if (currentIndex < 0) throw new NoSuchElementException();
            return getByRef(currentIndex);
        }

        public int currentIndex() {
            return currentIndex;
        }

        public int currentX() {
            return currentIndex & cordBitMask;
        }

        public int currentY() {
            return (currentIndex >> yShift) & cordBitMask;
        }

        public int currentZ() {
            return (currentIndex >> zShift) & cordBitMask;
        }
    }

    public class PalettizedCuboidByRefIterator extends PalettizedCuboidIterator {
        /**
         * Gets a copy of the palette value at the specified position in this cuboid.
         * <p>Modifying the returned value can be done safely, it will have no effect on this cuboid.</p>
         * <p>To avoid the overhead of making a copy use {@link #next()} instead.</p>
         */
        public E nextByValue() {
            return super.next();
        }

        /**
         * Gets the next palette value at the specified position in this cuboid BY REFERENCE.
         * <p><b>WARNING if the returned value is modified it modifies every value which references the same palette entry!</b></p>
         */
        @Override
        public E next() {
            return super.nextByRef();
        }
    }
}
