package net.rossquerz.mca;

import net.rossquerz.mca.entities.EntityBase;
import net.rossquerz.mca.entities.EntityBaseImpl;
import net.rossquerz.nbt.tag.CompoundTag;

import static net.rossquerz.mca.DataVersion.*;

public class EntitiesChunkTest extends EntitiesChunkBaseTest<EntityBase, EntitiesChunk> {

    @Override
    protected EntitiesChunk createChunk(DataVersion dataVersion) {
        return new EntitiesChunk(dataVersion.id());
    }

    @Override
    protected EntitiesChunk createChunk(CompoundTag tag) {
        return new EntitiesChunk(tag);
    }

    @Override
    protected EntitiesChunk createChunk(CompoundTag tag, long loadData) {
        return new EntitiesChunk(tag, loadData);
    }

    @Override
    protected EntityBase createEntity(int dataVersion, String id, double x, double y, double z, float yaw, float pitch) {
        return new EntityBaseImpl(dataVersion, id, x, y, z, yaw, pitch);
    }

    @Override
    public void testDataVersion() {
        EntitiesChunk chunk = createChunk(createTag(JAVA_1_17_0.id(), 0, 0));
        assertEquals(JAVA_1_17_0.id(), chunk.getDataVersion());
        assertEquals(JAVA_1_17_0, chunk.getDataVersionEnum());
        chunk.setDataVersion(JAVA_1_17_1.id());
        assertEquals(JAVA_1_17_1.id(), chunk.getDataVersion());
        assertEquals(JAVA_1_17_1, chunk.getDataVersionEnum());
    }

    public void testSetDataVersion_cannotCross_JAVA_1_17_20W45A() {
        EntitiesChunk chunk = createChunk(DataVersion.JAVA_1_16_5);
        assertThrowsException(() -> chunk.setDataVersion(DataVersion.JAVA_1_17_20W45A.id()), UnsupportedVersionChangeException.class);
        assertThrowsNoException(() -> chunk.setDataVersion(DataVersion.JAVA_1_16_0.id()));

        EntitiesChunk chunk2 = createChunk(DataVersion.JAVA_1_17_20W45A);
        assertThrowsNoException(() -> chunk2.setDataVersion(DataVersion.JAVA_1_17_0.id()));
    }
}

