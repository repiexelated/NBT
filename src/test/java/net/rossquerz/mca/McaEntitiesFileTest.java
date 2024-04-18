package net.rossquerz.mca;

import net.rossquerz.mca.entities.EntityBase;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class McaEntitiesFileTest extends McaFileBaseTest {

    public void testMcaReadWriteParity_1_17_1() {
        validateReadWriteParity(DataVersion.JAVA_1_17_1, "1_17_1/entities/r.-3.-2.mca", McaEntitiesFile.class);
    }

    public void testMcaReadWriteParity_1_18_PRE1() {
        validateReadWriteParity(DataVersion.JAVA_1_18_PRE1, "1_18_PRE1/entities/r.-2.-3.mca", McaEntitiesFile.class);
    }

    public void testMcaReadWriteParity_1_18_1() {
        validateReadWriteParity(DataVersion.JAVA_1_18_1, "1_18_1/entities/r.0.-2.mca", McaEntitiesFile.class);
        validateReadWriteParity(DataVersion.JAVA_1_18_1, "1_18_1/entities/r.8.1.mca", McaEntitiesFile.class);
    }

    public void testReadEntities_1_17_1() throws IOException {
        McaEntitiesFile mca = McaFileHelpers.readAuto(copyResourceToTmp("1_17_1/entities/r.-3.-2.mca"));
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
        McaEntitiesFile mca = McaFileHelpers.readAuto(copyResourceToTmp("1_18_PRE1/entities/r.-2.-3.mca"));
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
