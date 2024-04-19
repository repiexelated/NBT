package net.rossquerz.mca;

import net.rossquerz.nbt.io.TextNbtHelpers;
import net.rossquerz.nbt.tag.CompoundTag;
import net.rossquerz.mca.util.IntPointXZ;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// TODO: implement abstract test pattern for McaFileBase & refactor MCAFileTest like mad
public class McaFileBaseTest extends McaTestCase {

    // TODO: scan resources folder and auto-run this test for each mca file and type so we don't need to update tests every time we add a new test region file.
    /**
     * Reads an mca file, writes it, reads it back in, verifies that the NBT of the first non-empty chunk is identical
     * between the reads and that the data versions are correct and that McaFileHelpers.readAuto returned the correct type.
     */
    protected <CT extends ChunkBase, FT extends McaFileBase<CT>> void validateReadWriteParity(DataVersion expectedDataVersion, String mcaResourcePath, Class<FT> clazz) {
        FT mcaA = assertThrowsNoException(() -> McaFileHelpers.readAuto(copyResourceToTmp(mcaResourcePath)));
        assertTrue(clazz.isInstance(mcaA));
        List<CT> chunksIn = mcaA.stream().filter(Objects::nonNull).collect(Collectors.toList());
        assertFalse(chunksIn.isEmpty());
        assertEquals(expectedDataVersion.id(), mcaA.getDefaultChunkDataVersion());

        // ensure that we are reading and writing every tag - at least at the chunk/section level
        final List<CompoundTag> originalChunkData = new ArrayList<>();
        for (CT chunk : chunksIn) {
            assertEquals(expectedDataVersion, chunk.getDataVersionEnum());
            originalChunkData.add(chunk.data.clone());
            if (chunk instanceof SectionedChunkBase) {
                for (SectionBase<?> section : (SectionedChunkBase<?>) chunk) {
                    section.data.clear();
                }
            }
            chunk.data.clear();
        }

        File tmpFile = super.getNewTmpFile(Paths.get("out", mcaResourcePath).toString());
        assertThrowsNoException(() -> McaFileHelpers.write(mcaA, tmpFile));
        // writing should have rebuilt the data tag fully and correctly
        for (int i = 0; i < chunksIn.size(); i++) {
            try {
                TextNbtHelpers.writeTextNbtFile(Paths.get("TESTDBG", mcaResourcePath + "." + i + ".original.snbt"), originalChunkData.get(i));
                if (!originalChunkData.get(i).equals(chunksIn.get(i).data)) {
                    TextNbtHelpers.writeTextNbtFile(Paths.get("TESTDBG", mcaResourcePath + "." + i + ".regurgitated.snbt"), chunksIn.get(i).data);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                fail(ex.getMessage());
            }

            assertEquals(originalChunkData.get(i), chunksIn.get(i).data);
        }

        FT mcaB = assertThrowsNoException(() -> McaFileHelpers.readAuto(tmpFile));
        List<CT> chunksAsReread = mcaB.stream().filter(Objects::nonNull).collect(Collectors.toList());
        assertTrue(clazz.isInstance(mcaB));
        assertFalse(chunksAsReread.isEmpty());
        assertEquals(chunksIn.size(), chunksAsReread.size());
        assertEquals(expectedDataVersion.id(), mcaB.getDefaultChunkDataVersion());

        for (int i = 0; i < chunksAsReread.size(); i++) {
            CT chunkA = chunksIn.get(i);
            CT chunkB = chunksAsReread.get(i);
            assertEquals(chunkA.getDataVersionEnum(), chunkB.getDataVersionEnum());
            assertEquals(originalChunkData.get(i), chunkB.data);
        }
    }

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
