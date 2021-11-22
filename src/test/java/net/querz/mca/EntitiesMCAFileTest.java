package net.querz.mca;

import net.querz.mca.entities.EntityBase;
import net.querz.mca.entities.EntityBaseImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class EntitiesMCAFileTest extends MCAFileBaseTest {

    /**
     * Validates and demonstrates function of reading pre MC 1.17 region mca file using the {@link EntitiesMCAFile}
     * class.
     */
    public void testLoadingOldRegionMcaAsEntityMca() throws IOException {
        EntitiesMCAFile entitiesMCAFile = MCAUtil.readEntities(copyResourceToTmp("1_16_5/region/r.0.-1.mca"));
        assertNotNull(entitiesMCAFile);
        // Shorthand way to just grab the first non-null chunk.
        // The resource test mca files only have 1 chunk to minimize file size.
        EntitiesChunk chunk = entitiesMCAFile.stream().filter(Objects::nonNull).findFirst().orElse(null);
        assertNotNull(chunk);

        final int originalEntityCount = chunk.getEntities().size();
        assertEquals(originalEntityCount, chunk.getEntities().size());

        // Remove the iron_golem via the entities chunk
        assertTrue(chunk.getEntities().removeIf(e -> e.getId().equals("minecraft:iron_golem")));
        assertEquals(originalEntityCount - 1, chunk.getEntities().size());

        // When we apply the changes to the entities chunk, it updates the NBT data also held by the region chunk.
        chunk.updateHandle();
        assertEquals(originalEntityCount - 1, chunk.getEntities().size());

        // This bit just gets the id's of the remaining entities from the nbt tag
        List<String> finalEntityIds = chunk.getEntitiesTag().stream()
                .map(et -> et.getString("id"))
                .collect(Collectors.toList());

        // verify that the region entities tag no longer contains an iron golemn
        assertFalse(finalEntityIds.contains("minecraft:iron_golem"));

        // print them if you want to see them
        // System.out.println(String.join(", ", finalEntityIds));
    }

    public void testMcaReadWriteParity_1_17_1() {
        validateReadWriteParity(DataVersion.JAVA_1_17_1, "1_17_1/entities/r.-3.-2.mca", EntitiesMCAFile.class);
    }

    public void testMcaReadWriteParity_1_18_PRE1() {
        validateReadWriteParity(DataVersion.JAVA_1_18_PRE1, "1_18_PRE1/entities/r.-2.-3.mca", EntitiesMCAFile.class);
    }

    public void testReadEntities_1_17_1() throws IOException {
        EntitiesMCAFile mca = MCAUtil.readAuto(copyResourceToTmp("1_17_1/entities/r.-3.-2.mca"));
        assertNotNull(mca);
        EntitiesChunk chunk = mca.stream().filter(Objects::nonNull).findFirst().orElse(null);
        assertNotNull(chunk);
        List<EntityBase> entities = chunk.getEntities();
        assertNotNull(entities);
        assertFalse(entities.isEmpty());

        // mca specific checks (will need to be changed if mca file changes in meaningful ways)
        assertEquals(1, entities.size());
        Map<String, List<EntityBase>> entitiesByType = chunk.stream().collect(Collectors.groupingBy(EntityBase::getId));
        assertEquals(1, entitiesByType.size());
        assertTrue(entitiesByType.containsKey("minecraft:villager"));
        assertEquals(1, entitiesByType.get("minecraft:villager").size());
    }

    public void testReadEntities_1_18_PRE1() throws IOException {
        EntitiesMCAFile mca = MCAUtil.readAuto(copyResourceToTmp("1_18_PRE1/entities/r.-2.-3.mca"));
        assertNotNull(mca);
        EntitiesChunk chunk = mca.stream().filter(Objects::nonNull).findFirst().orElse(null);
        assertNotNull(chunk);
        List<EntityBase> entities = chunk.getEntities();
        assertNotNull(entities);
        assertFalse(entities.isEmpty());

        // mca specific checks (will need to be changed if mca file changes in meaningful ways)
        assertEquals(7, entities.size());
        Map<String, List<EntityBase>> entitiesByType = chunk.stream().collect(Collectors.groupingBy(EntityBase::getId));
        assertEquals(3, entitiesByType.size());
        assertTrue(entitiesByType.containsKey("minecraft:villager"));
        assertEquals(4, entitiesByType.get("minecraft:villager").size());
        assertTrue(entitiesByType.containsKey("minecraft:chicken"));
        assertEquals(2, entitiesByType.get("minecraft:chicken").size());
        assertTrue(entitiesByType.containsKey("minecraft:cat"));
        assertEquals(1, entitiesByType.get("minecraft:cat").size());
    }
}
