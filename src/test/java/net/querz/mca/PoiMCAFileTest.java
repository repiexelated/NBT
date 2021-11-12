package net.querz.mca;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PoiMCAFileTest extends MCATestCase {

    public void testPoiMca_1_17_1() {
        PoiMCAFile mca = assertThrowsNoException(() -> MCAUtil.readAuto(copyResourceToTmp("1_17_1/poi/r.-3.-2.mca")));
        PoiChunk chunk = mca.stream().filter(Objects::nonNull).findFirst().orElse(null);
        assertNotNull(chunk);
        assertEquals(DataVersion.JAVA_1_17_1, chunk.getDataVersionEnum());
        assertTrue(chunk.isPoiSectionValid(3));
        assertTrue(chunk.isPoiSectionValid(4));

        assertFalse(chunk.isEmpty());
        Map<String, List<PoiRecord>> recordsByType = chunk.stream().collect(Collectors.groupingBy(PoiRecord::getType));
        assertTrue(recordsByType.containsKey("minecraft:home"));
        assertTrue(recordsByType.containsKey("minecraft:cartographer"));
        assertTrue(recordsByType.containsKey("minecraft:nether_portal"));
        assertEquals(1, recordsByType.get("minecraft:home").size());
        assertEquals(new PoiRecord(-1032, 63, -670, "minecraft:home"), recordsByType.get("minecraft:home").get(0));
        // it'd be better if we had a bell in this chunk to test a non-zero value here
        assertEquals(0, recordsByType.get("minecraft:home").get(0).getFreeTickets());

        File tmpFile = super.getNewTmpFile("/poi/out.r.-3.-2.mca");
        assertThrowsNoException(() -> MCAUtil.write(mca, tmpFile));

        PoiMCAFile mca2 = assertThrowsNoException(() -> MCAUtil.readAuto(tmpFile));
        PoiChunk chunk2 = mca2.stream().filter(Objects::nonNull).findFirst().orElse(null);
        assertNotNull(chunk2);

        assertEquals(chunk.data, chunk2.data);
    }
}
