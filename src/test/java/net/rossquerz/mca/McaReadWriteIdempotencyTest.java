package net.rossquerz.mca;

import org.junit.Ignore;

public class McaReadWriteIdempotencyTest extends McaFileBaseTest {

    public void testMcaReadWriteParity_1_9_4() {
        validateReadWriteParity(DataVersion.JAVA_1_9_4, "1_9_4/region/r.2.-1.mca", McaRegionFile.class);
    }

    public void testMcaReadWriteParity_1_12_2() {
        validateReadWriteParity(DataVersion.JAVA_1_12_2, "1_12_2/region/r.0.0.mca", McaRegionFile.class);
    }

    public void testMcaReadWriteParity_1_13_0() {
        validateReadWriteParity(DataVersion.JAVA_1_13_0, "1_13_0/region/r.0.0.mca", McaRegionFile.class);
    }

    @Ignore("This file appears to be a partially upgraded world")
    public void ignore_testMcaReadWriteParity_1_13_1() {
        validateReadWriteParity(DataVersion.JAVA_1_13_1, "1_13_1/region/r.2.2.mca", McaRegionFile.class);
    }

    public void testMcaReadWriteParity_1_13_2() {
        validateReadWriteParity(DataVersion.JAVA_1_13_2, "1_13_2/region/r.-2.-2.mca", McaRegionFile.class);
    }

    public void testMcaReadWriteParity_1_14_4() {
        validateReadWriteParity(DataVersion.JAVA_1_14_4, "1_14_4/region/r.-1.0.mca", McaRegionFile.class);
    }

    public void testMcaReadWriteParity_1_15_2() {
        validateReadWriteParity(DataVersion.JAVA_1_15_2, "1_15_2/region/r.-1.0.mca", McaRegionFile.class);
    }

    public void testMcaReadWriteParity_1_16_5() {
        validateReadWriteParity(DataVersion.JAVA_1_16_5, "1_16_5/region/r.0.-1.mca", McaRegionFile.class);
    }

    public void testMcaReadWriteParity_1_17_1() {
        validateReadWriteParity(DataVersion.JAVA_1_17_1, "1_17_1/region/r.-3.-2.mca", McaRegionFile.class);
    }

    public void testMcaReadWriteParity_1_18_PRE1() {
        validateReadWriteParity(DataVersion.JAVA_1_18_PRE1, "1_18_PRE1/region/r.-2.-3.mca", McaRegionFile.class);
    }

    public void testMcaReadWriteParity_1_18_1() {
        validateReadWriteParity(DataVersion.JAVA_1_18_1, "1_18_1/region/r.0.-2.mca", McaRegionFile.class);
        validateReadWriteParity(DataVersion.JAVA_1_18_1, "1_18_1/region/r.8.1.mca", McaRegionFile.class);
    }

    public void testMcaReadWriteParity_1_20_4() {
        validateReadWriteParity(DataVersion.JAVA_1_20_4, "1_20_4/region/r.0.0.mca", McaRegionFile.class);
    }
}
