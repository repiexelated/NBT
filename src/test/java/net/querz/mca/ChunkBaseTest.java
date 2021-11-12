package net.querz.mca;

import net.querz.NBTTestCase;
import net.querz.nbt.tag.CompoundTag;
import static net.querz.mca.DataVersion.*;

/**
 * All implementors of {@link ChunkBaseTest} should create a test which inherits this one and add
 * tests to cover any additional functionality added by that concretion.
 */
public abstract class ChunkBaseTest<T extends ChunkBase> extends NBTTestCase {
    protected abstract T createChunk();
    protected abstract T createChunk(CompoundTag tag);
    protected abstract T createChunk(CompoundTag tag, long loadData);

    public void testChunkBase_defaultConstructor() {
        T chunk = createChunk();
        int now = (int)(System.currentTimeMillis() / 1000);
        assertEquals(DataVersion.latest().id(), chunk.getDataVersion());
        assertEquals(DataVersion.latest(), chunk.getDataVersionEnum());
        assertTrue(Math.abs(now - chunk.getLastMCAUpdate()) <= 1);
    }

    public void testLastMcaUpdated() {
        T chunk = createChunk();
        chunk.setLastMCAUpdate(1747522);
        assertEquals(1747522, chunk.getLastMCAUpdate());
    }

    public void testDataVersion() {
        T chunk = createChunk(createTag(JAVA_1_16_0.id()));
        assertEquals(JAVA_1_16_0.id(), chunk.getDataVersion());
        assertEquals(JAVA_1_16_0, chunk.getDataVersionEnum());
        chunk.setDataVersion(JAVA_1_16_1.id());
        assertEquals(JAVA_1_16_1.id(), chunk.getDataVersion());
        assertEquals(JAVA_1_16_1, chunk.getDataVersionEnum());
        chunk.setDataVersion(JAVA_1_16_2);
        assertEquals(JAVA_1_16_2.id(), chunk.getDataVersion());
        assertEquals(JAVA_1_16_2, chunk.getDataVersionEnum());
    }

    /**
     * @param dataVersion set as "DataVersion" in returned tag IFF GT 0
     */
    protected CompoundTag createTag(int dataVersion) {
        CompoundTag tag = new CompoundTag();
        if (dataVersion > 0) tag.putInt("DataVersion", dataVersion);
        return tag;
    }

    protected abstract void validateAllDataConstructor();

    final public void testConstructor_allData() {
        CompoundTag tag = createTag(DataVersion.JAVA_1_17_1.id());
        assertNotNull(tag);
        T chunk = createChunk(tag, LoadFlags.ALL_DATA);
        assertEquals(DataVersion.JAVA_1_17_1.id(), chunk.getDataVersion());
        assertEquals(DataVersion.JAVA_1_17_1, chunk.getDataVersionEnum());
        assertFalse(chunk.partial);
        assertFalse(chunk.raw);
        assertSame(tag, chunk.getHandle());
        validateAllDataConstructor();
    }

    final public void testConstructor_raw() {
        CompoundTag tag = createTag(DataVersion.JAVA_1_16_5.id());
        assertNotNull(tag);
        T chunk = createChunk(tag, LoadFlags.RAW);
        assertEquals(DataVersion.JAVA_1_16_5.id(), chunk.getDataVersion());
        assertEquals(DataVersion.JAVA_1_16_5, chunk.getDataVersionEnum());
        assertFalse(chunk.partial);
        assertTrue(chunk.raw);
        assertSame(tag, chunk.getHandle());
        assertThrowsException(chunk::checkRaw, UnsupportedOperationException.class);
    }

    public void testConstructor_allData_throwsIfDataVersionNotFound() {
        CompoundTag tag = createTag(-1);
        assertNotNull(tag);
        assertThrowsException(() -> createChunk(tag, LoadFlags.ALL_DATA), IllegalArgumentException.class);
    }

    public void testConstructor_raw_noThrowIfDataVersionNotFound() {
        CompoundTag tag = createTag(-1);
        assertNotNull(tag);
        assertThrowsNoException(() -> createChunk(tag, LoadFlags.RAW));
    }
}
