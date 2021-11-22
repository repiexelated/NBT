package net.querz.mca;

import net.querz.mca.entities.EntityBase;
import net.querz.mca.entities.EntityBaseImpl;
import net.querz.nbt.tag.CompoundTag;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EntitiesChunkTest extends EntitiesChunkBaseTest<EntityBase, EntitiesChunk> {

    /**
     * Validates and demonstrates function of manipulating entities from a pre MC 1.17 region mca file using
     * the {@link EntitiesChunk}. Prefer using {@link MCAUtil#readEntities(File)} over this strategy
     * or to simply use {@link net.querz.mca.entities.EntityFactory} directly.
     */
    public void testFromRegionChunk() throws IOException {
        // This first part is just getting to a region chunk - skip to "BEGIN" below
        MCAFile regionMcaFile = MCAUtil.read(copyResourceToTmp("1_16_5/region/r.0.-1.mca"));
        assertNotNull(regionMcaFile);
        // Shorthand way to just grab the first non-null chunk.
        // The resource test mca files only have 1 chunk to minimize file size.
        Chunk regionChunk = regionMcaFile.stream().filter(Objects::nonNull).findFirst().orElse(null);
        assertNotNull(regionChunk);

        // BEGIN
        EntitiesChunk entitiesChunk = new EntitiesChunk(regionChunk.getHandle());
        assertNotNull(entitiesChunk);

        final int originalEntityCount = regionChunk.getEntities().size();
        assertEquals(originalEntityCount, entitiesChunk.getEntities().size());

        // Remove the iron_golem via the entities chunk

        assertTrue(entitiesChunk.getEntities().removeIf(e -> e.getId().equals("minecraft:iron_golem")));
        assertEquals(originalEntityCount - 1, entitiesChunk.getEntities().size());

        // When we apply the changes to the entities chunk, it updates the NBT data also held by the region chunk.
        entitiesChunk.updateHandle();
        assertEquals(originalEntityCount - 1, regionChunk.getEntities().size());

        // This bit just gets the id's of the remaining entities
        List<String> finalEntityIds = regionChunk.getEntities().stream()
                .map(et -> et.getString("id"))
                .collect(Collectors.toList());

        // verify that the region entities tag no longer contains an iron golemn
        assertFalse(finalEntityIds.contains("minecraft:iron_golem"));

        // print them if you want to see them
        // System.out.println(String.join(", ", finalEntityIds));
    }

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

    public void testSetDataVersion_cannotCross_JAVA_1_17_20W45A() {
        EntitiesChunk chunk = createChunk(DataVersion.JAVA_1_16_5);
        assertThrowsException(() -> chunk.setDataVersion(DataVersion.JAVA_1_17_20W45A.id()), UnsupportedVersionChangeException.class);
        assertThrowsNoException(() -> chunk.setDataVersion(DataVersion.JAVA_1_16_0.id()));

        EntitiesChunk chunk2 = createChunk(DataVersion.JAVA_1_17_20W45A);
        assertThrowsNoException(() -> chunk2.setDataVersion(DataVersion.JAVA_1_17_0.id()));
    }
}

