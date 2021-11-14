package net.querz.mca;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

// TODO: implement abstract test pattern for MCAFileBase & refactor MCAFileTest like mad
public class MCAFileBaseTest extends MCATestCase {

    // TODO: scan resources folder and auto-run this test for each mca file and type so we don't need to update tests every time we add a new test region file.
    /**
     * Reads an mca file, writes it, reads it back in, verifies that the NBT of the first non-empty chunk is identical
     * between the reads and that the data versions are correct and that MCAUtil.readAuto returned the correct type.
     */
    protected <CT extends ChunkBase, FT extends MCAFileBase<CT>> void validateReadWriteParity(DataVersion expectedDataVersion, String mcaResourcePath, Class<FT> clazz) {
        FT mcaA = assertThrowsNoException(() -> MCAUtil.readAuto(copyResourceToTmp(mcaResourcePath)));
        assertTrue(clazz.isInstance(mcaA));
        CT chunkA = mcaA.stream().filter(Objects::nonNull).findFirst().orElse(null);
        assertNotNull(chunkA);
        assertEquals(expectedDataVersion.id(), mcaA.getDefaultChunkDataVersion());
        assertEquals(expectedDataVersion, chunkA.getDataVersionEnum());

        File tmpFile = super.getNewTmpFile(Paths.get("out", mcaResourcePath).toString());
        assertThrowsNoException(() -> MCAUtil.write(mcaA, tmpFile));

        FT mcaB = assertThrowsNoException(() -> MCAUtil.readAuto(tmpFile));
        CT chunkB = mcaB.stream().filter(Objects::nonNull).findFirst().orElse(null);
        assertTrue(clazz.isInstance(mcaB));
        assertNotNull(chunkB);
        assertEquals(expectedDataVersion.id(), mcaB.getDefaultChunkDataVersion());
        assertEquals(expectedDataVersion, chunkB.getDataVersionEnum());

        assertEquals(chunkA.getLastMCAUpdate(), chunkB.getLastMCAUpdate());
        assertEquals(chunkA.data, chunkB.data);
    }

    public void testNullTest() { }
}
