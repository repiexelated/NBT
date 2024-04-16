package net.querz.mca;

import net.querz.NBTTestCase;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.LongArrayTag;
import net.querz.nbt.tag.StringTag;
import net.querz.util.JsonPrettyPrinter;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

public class PalettizedCuboidTest extends NBTTestCase {

    public void testCalculateBitMask_throwsIllegalArgumentExceptionAppropriately() {
        assertThrowsException(() -> PalettizedCuboid.calculateBitMask(-1), IllegalArgumentException.class);
        assertThrowsException(() -> PalettizedCuboid.calculateBitMask(32), IllegalArgumentException.class);
    }

    public void testCalculateBitMask_typicalUsage() {
        assertEquals(0x1, PalettizedCuboid.calculateBitMask(1));
        assertEquals(0x3, PalettizedCuboid.calculateBitMask(2));
        assertEquals(0x7, PalettizedCuboid.calculateBitMask(3));
        assertEquals(0xf, PalettizedCuboid.calculateBitMask(4));
        assertEquals(0x1f, PalettizedCuboid.calculateBitMask(5));
        assertEquals(0x3f, PalettizedCuboid.calculateBitMask(6));
        assertEquals(0x7f, PalettizedCuboid.calculateBitMask(7));
        assertEquals(0xff, PalettizedCuboid.calculateBitMask(8));
        assertEquals(0xffff, PalettizedCuboid.calculateBitMask(16));
        assertEquals(0x7fffffff, PalettizedCuboid.calculateBitMask(31));
    }

    public void testCalculatePowerOfTwoExponent() {
        assertEquals(0, PalettizedCuboid.calculatePowerOfTwoExponent(0, true));
        assertEquals(0, PalettizedCuboid.calculatePowerOfTwoExponent(1, true));
        assertEquals(1, PalettizedCuboid.calculatePowerOfTwoExponent(2, true));
        assertEquals(2, PalettizedCuboid.calculatePowerOfTwoExponent(4, true));
        assertEquals(3, PalettizedCuboid.calculatePowerOfTwoExponent(8, true));
        assertEquals(4, PalettizedCuboid.calculatePowerOfTwoExponent(16, true));

        assertThrowsException(() -> PalettizedCuboid.calculatePowerOfTwoExponent(7, true), IllegalArgumentException.class);

        assertEquals(0, PalettizedCuboid.calculatePowerOfTwoExponent(0, false));
        assertEquals(0, PalettizedCuboid.calculatePowerOfTwoExponent(1, false));
        assertEquals(1, PalettizedCuboid.calculatePowerOfTwoExponent(2, false));
        assertEquals(2, PalettizedCuboid.calculatePowerOfTwoExponent(4, false));
        assertEquals(3, PalettizedCuboid.calculatePowerOfTwoExponent(8, false));
        assertEquals(4, PalettizedCuboid.calculatePowerOfTwoExponent(16, false));

        assertEquals(2, PalettizedCuboid.calculatePowerOfTwoExponent(3, false));
        assertEquals(3, PalettizedCuboid.calculatePowerOfTwoExponent(5, false));
        assertEquals(3, PalettizedCuboid.calculatePowerOfTwoExponent(7, false));
        assertEquals(4, PalettizedCuboid.calculatePowerOfTwoExponent(9, false));
        assertEquals(4, PalettizedCuboid.calculatePowerOfTwoExponent(15, false));
        assertEquals(5, PalettizedCuboid.calculatePowerOfTwoExponent(17, false));
        assertEquals(6, PalettizedCuboid.calculatePowerOfTwoExponent(60, false));
        assertEquals(7, PalettizedCuboid.calculatePowerOfTwoExponent(66, false));
    }

    public void testCtor_initFilledCuboid() {
        StringTag fillTag = new StringTag("void");
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(4, fillTag);
        assertEquals(4 * 4 * 4, cuboid.size());
        assertEquals(4, cuboid.cubeEdgeLength());
        assertEquals(1, cuboid.paletteSize());
        assertTrue(Arrays.stream(cuboid.data).allMatch(d -> d == 0));
        assertNotSame(fillTag, cuboid.palette.get(0));
        assertEquals(fillTag, cuboid.palette.get(0));
    }

    public void testCtr_throwsAppropriatelyWhenGivenNullFill() {
        assertThrowsException(() -> new PalettizedCuboid<>(4, null), NullPointerException.class);
        assertThrowsNoException(() -> new PalettizedCuboid<>(4, null, StringTag.class, true));
    }

    public void testCtor_initFromValueArray_happyCase() {
        StringTag bedrockTag = new StringTag("bedrock");
        StringTag airTag = new StringTag("air");
        StringTag stoneTag = new StringTag("stone");
        StringTag[] tags = new StringTag[2 * 2 * 2];
        Arrays.fill(tags, airTag);
        tags[0] = bedrockTag;
        tags[6] = stoneTag;
        tags[7] = stoneTag;
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(tags);

        assertEquals(tags.length, cuboid.size());
        assertEquals(2, cuboid.cubeEdgeLength());
        assertEquals(3, cuboid.paletteSize());
        assertEquals(bedrockTag, cuboid.palette.get(0));
        assertNotSame(bedrockTag, cuboid.palette.get(0));
        assertEquals(airTag, cuboid.palette.get(1));
        assertNotSame(airTag, cuboid.palette.get(1));
        assertEquals(stoneTag, cuboid.palette.get(2));
        assertNotSame(stoneTag, cuboid.palette.get(2));
        assertEquals(1, Arrays.stream(cuboid.data).filter(d -> d == 0).count()); // bedrock
        assertEquals(5, Arrays.stream(cuboid.data).filter(d -> d == 1).count()); // air
        assertEquals(2, Arrays.stream(cuboid.data).filter(d -> d == 2).count()); // stone
    }

    public void testCtor_initFromValueArray_notACubicLengthThrows() {
        StringTag airTag = new StringTag("air");
        StringTag[] tags = new StringTag[2 * 2 * 2 + 1];
        Arrays.fill(tags, airTag);
        assertThrowsException(() -> new PalettizedCuboid<>(tags),
                IllegalArgumentException.class,
                s -> s.equals("the cube root of 9 is not an integer!"));
    }

    public void testCtor_initFromValueArray_notAPowerOfTwoCubicLengthThrows() {
        StringTag airTag = new StringTag("air");
        StringTag[] tags = new StringTag[3 * 3 * 3];
        Arrays.fill(tags, airTag);
        assertThrowsException(() -> new PalettizedCuboid<>(tags),
                IllegalArgumentException.class,
                s -> s.equals("3 isn't a power of two!"));
    }

    public void testCtor_initFromValueArray_throwsWhenArrayContainsNulls() {
        StringTag airTag = new StringTag("air");
        StringTag[] tags = new StringTag[2 * 2 * 2];
        Arrays.fill(tags, airTag);
        tags[7] = null;
        assertThrowsException(() -> new PalettizedCuboid<>(tags),
                NullPointerException.class,
                s -> s.equals("values must not contain nulls!"));

    }

    public void testContains() {
        StringTag airTag = new StringTag("air");
        StringTag[] tags = new StringTag[2 * 2 * 2];
        Arrays.fill(tags, airTag);
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(tags);

        assertTrue(cuboid.contains(airTag));
        assertFalse(cuboid.contains(new StringTag("grass")));
    }

    public void testCountIf() {
        StringTag bedrockTag = new StringTag("bedrock");
        StringTag airTag = new StringTag("air");
        StringTag stoneTag = new StringTag("stone");
        StringTag[] tags = new StringTag[2 * 2 * 2];
        Arrays.fill(tags, airTag);
        tags[0] = bedrockTag;
        tags[6] = stoneTag;
        tags[7] = stoneTag;
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(tags);

        assertEquals(5, cuboid.countIf(airTag::equals));
        assertEquals(0, cuboid.countIf(new StringTag("lava")::equals));
    }

    public void testToArray() {
        StringTag bedrockTag = new StringTag("bedrock");
        StringTag airTag = new StringTag("air");
        StringTag stoneTag = new StringTag("stone");
        StringTag[] tags = new StringTag[2 * 2 * 2];
        Arrays.fill(tags, airTag);
        tags[0] = bedrockTag;
        tags[6] = stoneTag;
        tags[7] = stoneTag;
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(tags);

        StringTag[] arr = cuboid.toArray();
        assertEquals(cuboid.size(), arr.length);
        assertNotSame(cuboid.palette.get(0), arr[0]);
        assertEquals(cuboid.palette.get(0), arr[0]);
        assertNotSame(bedrockTag, arr[0]);

        assertEquals(bedrockTag, arr[0]);
        assertEquals(airTag, arr[1]);
        assertEquals(airTag, arr[2]);
        assertEquals(airTag, arr[3]);
        assertEquals(airTag, arr[4]);
        assertEquals(airTag, arr[5]);
        assertEquals(stoneTag, arr[6]);
        assertEquals(stoneTag, arr[7]);
        assertNotSame(arr[6], arr[7]);
    }

    public void testToArrayByRef() {
        StringTag bedrockTag = new StringTag("bedrock");
        StringTag airTag = new StringTag("air");
        StringTag stoneTag = new StringTag("stone");
        StringTag[] tags = new StringTag[2 * 2 * 2];
        Arrays.fill(tags, airTag);
        tags[0] = bedrockTag;
        tags[6] = stoneTag;
        tags[7] = stoneTag;
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(tags);

        StringTag[] arr = cuboid.toArrayByRef();
        assertEquals(cuboid.size(), arr.length);
        assertSame(cuboid.palette.get(0), arr[0]);
        assertNotSame(bedrockTag, arr[0]);

        assertEquals(bedrockTag, arr[0]);
        assertEquals(airTag, arr[1]);
        assertEquals(airTag, arr[2]);
        assertEquals(airTag, arr[3]);
        assertEquals(airTag, arr[4]);
        assertEquals(airTag, arr[5]);
        assertEquals(stoneTag, arr[6]);
        assertEquals(stoneTag, arr[7]);
        assertSame(arr[6], arr[7]);
    }

    public void testReplace() {
        StringTag bedrockTag = new StringTag("bedrock");
        StringTag airTag = new StringTag("air");
        StringTag stoneTag = new StringTag("stone");
        StringTag[] tags = new StringTag[2 * 2 * 2];
        Arrays.fill(tags, airTag);
        tags[0] = bedrockTag;
        tags[6] = stoneTag;
        tags[7] = stoneTag;
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(tags);
        assertEquals(2, cuboid.data[7]);  // validate test assumption

        assertTrue(cuboid.replace(stoneTag, bedrockTag));
        assertNotSame(bedrockTag, cuboid.getByRef(7));
        assertEquals(bedrockTag, cuboid.getByRef(7));
        assertEquals(3, cuboid.countIf(bedrockTag::equals));

        assertEquals(3, cuboid.palette.size());
        assertEquals(1, cuboid.palette.stream().filter(bedrockTag::equals).count());
        assertTrue(cuboid.palette.contains(bedrockTag));
        assertTrue(cuboid.palette.contains(airTag));
        assertFalse(cuboid.palette.contains(stoneTag));
        assertEquals(0, cuboid.data[7]);  // validate data remapped to exiting palette index

        assertFalse(cuboid.replace(stoneTag, bedrockTag));  // check not found case
        assertFalse(cuboid.replace(airTag, airTag));  // check old == new case reports no changes
    }

    public void testReplaceAll() {
        StringTag bedrockTag = new StringTag("bedrock");
        StringTag airTag = new StringTag("air");
        StringTag stoneTag = new StringTag("stone");
        StringTag lavaTag = new StringTag("lava");
        StringTag[] tags = new StringTag[2 * 2 * 2];
        Arrays.fill(tags, airTag);
        tags[0] = bedrockTag;
        tags[6] = stoneTag;
        tags[7] = stoneTag;
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(tags);
        assertEquals(2, cuboid.data[7]);  // validate test assumption

        assertTrue(cuboid.replaceAll(new StringTag[] {stoneTag, bedrockTag}, lavaTag));
        assertNotSame(lavaTag, cuboid.getByRef(7));
        assertEquals(lavaTag, cuboid.getByRef(7));
        assertEquals(3, cuboid.countIf(lavaTag::equals));

        assertEquals(4, cuboid.palette.size());
        assertFalse(cuboid.palette.contains(bedrockTag));
        assertTrue(cuboid.palette.contains(airTag));
        assertFalse(cuboid.palette.contains(stoneTag));
        assertTrue(cuboid.palette.contains(lavaTag));
        assertEquals(3, cuboid.data[7]);  // validate data remapped to exiting palette index

        assertFalse(cuboid.replace(stoneTag, bedrockTag));  // check not found case
        assertFalse(cuboid.replaceAll(new StringTag[] {airTag}, airTag));  // check old == new case reports no changes
    }

    public void testReplaceIf() {
        StringTag bedrockTag = new StringTag("bedrock");
        StringTag airTag = new StringTag("air");
        StringTag stoneTag = new StringTag("stone");
        StringTag lavaTag = new StringTag("lava");
        StringTag[] tags = new StringTag[2 * 2 * 2];
        Arrays.fill(tags, airTag);
        tags[0] = bedrockTag;
        tags[6] = stoneTag;
        tags[7] = stoneTag;
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(tags);
        assertEquals(2, cuboid.data[7]);  // validate test assumption

        assertTrue(cuboid.replaceIf(e -> e.getValue().contains("o"), lavaTag));
        assertNotSame(lavaTag, cuboid.getByRef(7));
        assertEquals(lavaTag, cuboid.getByRef(7));
        assertEquals(3, cuboid.countIf(lavaTag::equals));

        assertEquals(4, cuboid.palette.size());
        assertFalse(cuboid.palette.contains(bedrockTag));
        assertTrue(cuboid.palette.contains(airTag));
        assertFalse(cuboid.palette.contains(stoneTag));
        assertTrue(cuboid.palette.contains(lavaTag));
        assertEquals(3, cuboid.data[7]);  // validate data remapped to exiting palette index

        assertFalse(cuboid.replace(stoneTag, bedrockTag));  // check not found case
        assertFalse(cuboid.replaceAll(new StringTag[] {airTag}, airTag));  // check old == new case reports no changes
    }

    public void testRetainAll() {
        StringTag bedrockTag = new StringTag("bedrock");
        StringTag airTag = new StringTag("air");
        StringTag stoneTag = new StringTag("stone");
        StringTag lavaTag = new StringTag("lava");
        StringTag[] tags = new StringTag[2 * 2 * 2];
        Arrays.fill(tags, airTag);
        tags[0] = bedrockTag;
        tags[6] = stoneTag;
        tags[7] = stoneTag;
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(tags);
        assertEquals(2, cuboid.data[7]);  // validate test assumption

        assertTrue(cuboid.retainAll(new StringTag[] {airTag}, lavaTag));
        assertNotSame(lavaTag, cuboid.getByRef(7));
        assertEquals(lavaTag, cuboid.getByRef(7));
        assertEquals(3, cuboid.countIf(lavaTag::equals));

        assertEquals(4, cuboid.palette.size());
        assertFalse(cuboid.palette.contains(bedrockTag));
        assertTrue(cuboid.palette.contains(airTag));
        assertFalse(cuboid.palette.contains(stoneTag));
        assertTrue(cuboid.palette.contains(lavaTag));
        assertEquals(3, cuboid.data[7]);  // validate data remapped to exiting palette index

        assertFalse(cuboid.replace(stoneTag, bedrockTag));  // check not found case
        assertFalse(cuboid.replaceAll(new StringTag[] {airTag}, airTag));  // check old == new case reports no changes
    }

    public void testFill() {
        StringTag bedrockTag = new StringTag("bedrock");
        StringTag airTag = new StringTag("air");
        StringTag stoneTag = new StringTag("stone");
        StringTag lavaTag = new StringTag("lava");
        StringTag[] tags = new StringTag[2 * 2 * 2];
        Arrays.fill(tags, airTag);
        tags[0] = bedrockTag;
        tags[6] = stoneTag;
        tags[7] = stoneTag;
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(tags);
        cuboid.fill(lavaTag);
        assertEquals(8, cuboid.countIf(lavaTag::equals));
        assertEquals(1, cuboid.paletteSize());
        assertNotSame(lavaTag, cuboid.palette.get(0));
        assertEquals(lavaTag, cuboid.palette.get(0));
    }

    public void testIndexOfXYZ() {
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(
                16, new StringTag("air"));
        assertEquals(4096, cuboid.size());
        assertEquals(0, cuboid.indexOf(0, 0, 0));
        assertEquals(4095, cuboid.indexOf(15, 15, 15));
        assertEquals(0, cuboid.indexOf(16, 16, 16));

        assertEquals(834, cuboid.indexOf(2, 3, 4));
        assertEquals(cuboid.indexOf(2, 3, 4), cuboid.indexOf(16 + 2, 16 + 3, 16 + 4));
    }

    public void testGet() {
        StringTag airTag = new StringTag("air");
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(2, airTag);
        assertEquals(airTag, cuboid.get(0));
        assertNotSame(airTag, cuboid.get(0));
        assertNotSame(cuboid.palette.get(0), cuboid.get(0));
    }

    public void testGetByRef() {
        StringTag airTag = new StringTag("air");
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(2, airTag);
        assertEquals(airTag, cuboid.getByRef(0));
        assertNotSame(airTag, cuboid.getByRef(0));
        assertSame(cuboid.palette.get(0), cuboid.getByRef(10, 11, 12));
    }

    public void testSet() {
        StringTag airTag = new StringTag("air");
        StringTag stoneTag = new StringTag("stone");
        StringTag lavaTag = new StringTag("lava");
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(2, airTag);
        cuboid.set(0, 0, 0, lavaTag);
        cuboid.set(0, 1, 0, lavaTag);
        assertEquals(6, cuboid.countIf(airTag::equals));
        assertEquals(lavaTag, cuboid.get(0, 0, 0));
        assertEquals(lavaTag, cuboid.get(0, 1, 0));
        assertNotSame(lavaTag, cuboid.get(0, 0, 0));
        assertNotSame(lavaTag, cuboid.get(0, 1, 0));

        cuboid.set(0, stoneTag);
        assertEquals(stoneTag, cuboid.get(0, 0, 0));
        // note that using set to replace all of one palette key will NOT remove that key from the palette
        // optimizePalette will clean those up - tested in #testOptimizePalette
    }

    public void testOptimizePalette() {
        StringTag airTag = new StringTag("air");
        StringTag stoneTag = new StringTag("stone");
        StringTag lavaTag = new StringTag("lava");
        StringTag bedrockTag = new StringTag("bedrock");
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(2, airTag);
        cuboid.set(1, 0, 0, stoneTag);
        cuboid.set(1, 1, 0, stoneTag);
        cuboid.set(1, 1, 1, lavaTag);
        cuboid.set(0, 0, 0, bedrockTag);

        assertArrayEquals(new int[] {3, 1, 0, 0, 0, 1, 0, 2}, cuboid.data);

        // test what should be a no-op
        int[] originalData = new int[cuboid.size()];
        System.arraycopy(cuboid.data, 0, originalData, 0, cuboid.size());
        StringTag[] originalPalette = new StringTag[cuboid.palette.size()];
        for (int i = 0; i < originalPalette.length; i++) {
            originalPalette[i] = cuboid.palette.get(i).clone();
        }
        cuboid.optimizePalette();
        assertArrayEquals(originalData, cuboid.data);
        assertArrayEquals(originalPalette, cuboid.palette.toArray());
        assertEquals(bedrockTag, cuboid.get(0));

        // start mutating
        cuboid.replace(stoneTag, airTag);
        cuboid.set(1, 1, 1, airTag);
        assertEquals(4, cuboid.paletteSize());

        cuboid.optimizePalette();  // id, null, null, id
        assertEquals(2, cuboid.paletteSize());
        assertEquals(airTag, cuboid.palette.get(0));
        assertEquals(bedrockTag, cuboid.palette.get(1));
        assertArrayEquals(new int[] {1, 0, 0, 0, 0, 0, 0, 0}, cuboid.data);

        // test trailing null in palette
        cuboid.set(0, 0, 0, airTag);
        cuboid.optimizePalette();  // id, null
        assertEquals(1, cuboid.paletteSize());
        assertEquals(airTag, cuboid.palette.get(0));
        assertArrayEquals(new int[] {0, 0, 0, 0, 0, 0, 0, 0}, cuboid.data);
    }

    public void testClone() {
        StringTag airTag = new StringTag("air");
        StringTag stoneTag = new StringTag("stone");
        StringTag lavaTag = new StringTag("lava");
        StringTag bedrockTag = new StringTag("bedrock");
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(2, airTag);
        cuboid.set(1, 0, 0, stoneTag);
        cuboid.set(1, 1, 0, stoneTag);
        cuboid.set(1, 1, 1, lavaTag);
        cuboid.set(0, 0, 0, bedrockTag);

        assertEquals(bedrockTag, cuboid.get(0));
        PalettizedCuboid<StringTag> cuboid2 = cuboid.clone();

        assertNotSame(cuboid, cuboid2);
        assertEquals(cuboid.size(), cuboid2.size());
        assertEquals(cuboid.cubeEdgeLength(), cuboid2.cubeEdgeLength());
        assertEquals(cuboid.paletteSize(), cuboid2.paletteSize());

        assertArrayEquals(cuboid.data, cuboid2.data);
        assertEquals(bedrockTag, cuboid.get(0));
        assertEquals(bedrockTag, cuboid2.get(0));

        assertEquals(cuboid.palette.get(0), cuboid2.palette.get(0));
        assertNotSame(cuboid.palette.get(0), cuboid2.palette.get(0));
    }

    public void testCompoundTag_toAndFrom_moreThanOnePaletteIdInUse() {
        PalettizedCuboid<StringTag> cuboid = new PalettizedCuboid<>(4, new StringTag("desert"));
        cuboid.set(0, 0, 0, new StringTag("beach"));
        cuboid.set(0, 1, 0, new StringTag("beach"));
        cuboid.set(1, 1, 0, new StringTag("flower_forest"));
        cuboid.set(1, 0, 1, new StringTag("beach"));
        cuboid.set(0, 2, 2, new StringTag("flower_forest"));
        cuboid.set(2, 3, 3, new StringTag("river"));
        cuboid.set(3, 3, 2, new StringTag("jumaji"));
        cuboid.set(3, 3, 3, new StringTag("jumaji"));

        CompoundTag serializedTag = cuboid.toCompoundTag();
        assertNotNull(serializedTag);
        assertTrue(serializedTag.containsKey("palette"));
        assertTrue(serializedTag.containsKey("data"));
        assertTrue(serializedTag.get("palette") instanceof ListTag);
        assertTrue(serializedTag.get("data") instanceof LongArrayTag);

        System.out.println(JsonPrettyPrinter.prettyPrintJson(serializedTag.toString()));
        PalettizedCuboid<StringTag> cuboid2 = PalettizedCuboid.fromCompoundTag(serializedTag, 4);
        assertNotNull(cuboid2);
        assertEquals(cuboid.size(), cuboid2.size());
        assertEquals(cuboid.cubeEdgeLength(), cuboid2.cubeEdgeLength());
        assertArrayEquals(cuboid.data, cuboid2.data);
        assertEquals(cuboid.paletteSize(), cuboid2.paletteSize());
        for (int i = 0; i < cuboid.palette.size(); i++) {
            assertEquals(cuboid.palette.get(i), cuboid2.palette.get(i));
        }
    }
}
