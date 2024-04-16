package net.querz.mca;

import org.junit.Ignore;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.querz.util.JsonPrettyPrinter.prettyPrintJson;

public class TerrainMCAFileTest extends MCAFileBaseTest {


    public void testFoo() {
        MCAFile mca = assertThrowsNoException(() -> MCAUtil.readAuto(copyResourceToTmp("1_18_1/region/r.0.-2.mca")));
        List<ChunkBase> chunksIn = mca.stream().filter(Objects::nonNull).collect(Collectors.toList());
        System.out.println("Chunk info from " + mca.createRegionName());
        for (ChunkBase chunk : chunksIn) {
            System.out.printf("%d -> %s @ %d %d%n", chunk.dataVersion, DataVersion.bestFor(chunk.dataVersion), chunk.chunkX, chunk.chunkZ);
//            System.out.println(prettyPrintJson(chunk.data.toString()));
        }
    }

    public void testMcaReadWriteParity_1_9_4() {
        validateReadWriteParity(DataVersion.JAVA_1_9_4, "1_9_4/region/r.2.-1.mca", MCAFile.class);
    }

    public void testMcaReadWriteParity_1_12_2() {
        validateReadWriteParity(DataVersion.JAVA_1_12_2, "1_12_2/region/r.0.0.mca", MCAFile.class);
    }

    public void testMcaReadWriteParity_1_13_0() {
        validateReadWriteParity(DataVersion.JAVA_1_13_0, "1_13_0/region/r.0.0.mca", MCAFile.class);
    }

    @Ignore("This file appears to be a partially upgraded world")
    public void ignore_testMcaReadWriteParity_1_13_1() {
        validateReadWriteParity(DataVersion.JAVA_1_13_1, "1_13_1/region/r.2.2.mca", MCAFile.class);
    }

    public void testMcaReadWriteParity_1_13_2() {
        validateReadWriteParity(DataVersion.JAVA_1_13_2, "1_13_2/region/r.-2.-2.mca", MCAFile.class);
    }

    public void testMcaReadWriteParity_1_14_4() {
        validateReadWriteParity(DataVersion.JAVA_1_14_4, "1_14_4/region/r.-1.0.mca", MCAFile.class);
    }

    public void testMcaReadWriteParity_1_15_2() {
        validateReadWriteParity(DataVersion.JAVA_1_15_2, "1_15_2/region/r.-1.0.mca", MCAFile.class);
    }

    public void testMcaReadWriteParity_1_16_5() {
        validateReadWriteParity(DataVersion.JAVA_1_16_5, "1_16_5/region/r.0.-1.mca", MCAFile.class);
    }

    public void testMcaReadWriteParity_1_17_1() {
        validateReadWriteParity(DataVersion.JAVA_1_17_1, "1_17_1/region/r.-3.-2.mca", MCAFile.class);
    }

    public void testMcaReadWriteParity_1_18_PRE1() {
        validateReadWriteParity(DataVersion.JAVA_1_18_PRE1, "1_18_PRE1/region/r.-2.-3.mca", MCAFile.class);
    }

    public void testMcaReadWriteParity_1_18_1() {
        validateReadWriteParity(DataVersion.JAVA_1_18_1, "1_18_1/region/r.0.-2.mca", MCAFile.class);
        validateReadWriteParity(DataVersion.JAVA_1_18_1, "1_18_1/region/r.8.1.mca", MCAFile.class);
    }

    public void testMcaReadWriteParity_1_20_4() {
        validateReadWriteParity(DataVersion.JAVA_1_20_4, "1_20_4/region/r.0.0.mca", MCAFile.class);
    }
}
