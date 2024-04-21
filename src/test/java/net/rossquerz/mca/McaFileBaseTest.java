package net.rossquerz.mca;

import net.rossquerz.mca.util.IntPointXZ;

// TODO: implement abstract test pattern for McaFileBase & refactor MCAFileTest like mad
public class McaFileBaseTest extends McaTestCase {
    public void testGetRelativeChunkXZ() {
        // few enough iterations to just be lazy and do an exhaustive test
        for (int i = 0; i < 1024; i++) {
            IntPointXZ xz = McaFileBase.getRelativeChunkXZ(i);
            assertEquals(i, McaFileBase.getChunkIndex(xz.getX(), xz.getZ()));
        }
        assertThrowsException(() -> McaFileBase.getRelativeChunkXZ(-1), IndexOutOfBoundsException.class);
        assertThrowsException(() -> McaFileBase.getRelativeChunkXZ(1024), IndexOutOfBoundsException.class);
    }
}
