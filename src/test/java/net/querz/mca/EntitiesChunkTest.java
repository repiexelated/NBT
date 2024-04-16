package net.querz.mca;

import net.querz.mca.entities.EntityBase;
import net.querz.mca.entities.EntityBaseImpl;
import net.querz.nbt.tag.CompoundTag;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.querz.mca.DataVersion.*;
import static net.querz.mca.DataVersion.JAVA_1_16_2;

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

