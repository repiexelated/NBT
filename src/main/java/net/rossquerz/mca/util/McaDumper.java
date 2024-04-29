package net.rossquerz.mca.util;

import net.rossquerz.mca.ChunkBase;
import net.rossquerz.mca.io.LoadFlags;
import net.rossquerz.mca.io.McaFileChunkIterator;
import net.rossquerz.nbt.io.TextNbtHelpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class McaDumper {
    private McaDumper() {}

    /**
     * @param mcaFile MCA file to dump the chunk data for.
     * @param outputRoot Root directory to dump into. The output will be written to
     *                   "&lt;mca type&gt;/rX.Z/&lt;chunk index&gt;.X.Z.snbt;"
     *                   Example: "region/r0.0/0042.168.-475.snbt"
     * @return The path containing the dumped chunk text nbt (.snbt) files.
     * @throws IOException
     */
    public static Path dumpChunksAsTextNbt(File mcaFile, Path outputRoot) throws IOException {
        try (McaFileChunkIterator<?> iter = McaFileChunkIterator.iterate(mcaFile, LoadFlags.RAW)) {
            File dir = Paths.get(
                    outputRoot.toString(),
                    mcaFile.getAbsoluteFile().getParentFile().getName(),
                    iter.regionXZ().toString("r%d.%d")).toFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            final String dirName = dir.getPath();
            while (iter.hasNext()) {
                ChunkBase chunk = iter.next();
                TextNbtHelpers.writeTextNbtFile(Paths.get(dirName, String.format("%04d", iter.currentIndex()) +
                        "." + iter.currentAbsoluteXZ().toString("%d.%d") + ".snbt"), chunk.getHandle(),
                        /*pretty print*/ true, /*sorted*/ true);

            }
            return dir.toPath();
        }
    }
}
