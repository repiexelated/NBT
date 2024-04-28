package net.rossquerz.mca.io;

import junit.framework.TestCase;
import net.rossquerz.mca.ChunkBase;
import net.rossquerz.mca.McaRegionFile;
import net.rossquerz.nbt.io.TextNbtHelpers;

import java.io.IOException;
import java.nio.file.Paths;

public class RegionFileRelocatorTest extends TestCase {

    public void testFoo() throws IOException {
////        String inRoot = "F:\\MinorArchive\\PluginDevServer\\plugins\\DuelEternal\\duel-templates\\drop-new-templates-here\\Desert.zip";
//        String inRoot = "F:\\MinorArchive\\PluginDevServer\\plugins\\DuelEternal\\duel-templates\\desert";
//        String outRoot = "F:\\MinorArchive\\PluginDevServer\\world";
//
//        RegionFileRelocator relocator = new RegionFileRelocator()
//                .sourceRoot(inRoot)
//                .destinationRoot(outRoot);
////        assertTrue(relocator.relocate("r.-2.0.mca", "r.-2.0.mca"));
//        assertTrue(relocator.relocate("r.-2.0.mca", "r.10.10.mca"));

//        McaRegionFile mca = McaFileHelpers.readAuto("F:\\MinorArchive\\PluginDevServer\\world\\region\\r.10.10.mca");
//        for (int i = 0; i < 1024; i++) {
//            ChunkBase chunk = mca.getChunk(i);
//            if (chunk != null)
//                TextNbtHelpers.writeTextNbtFile(
//                        Paths.get("TESTDBG", "wip", "OUT.r.10.10", "region", "i" + String.format("%04d", i) + "." + chunk.getChunkXZ().toString("x%dz%d") + ".snbt"),
//                        chunk.getHandle(), /*pretty print*/ true, /*sorted*/ true);
//        }

//        McaRegionFile mca = McaFileHelpers.readAuto(inRoot + "\\region\\r.-2.0.mca");
//        for (int i = 0; i < 1024; i++) {
//            ChunkBase chunk = mca.getChunk(i);
//            if (chunk != null)
//                TextNbtHelpers.writeTextNbtFile(
//                        Paths.get("TESTDBG", "wip", "IN.r.-2.0", "region", "i" + String.format("%04d", i) + "." + chunk.getChunkXZ().toString("x%dz%d") + ".snbt"),
//                        chunk.getHandle(), /*pretty print*/ true, /*sorted*/ true);
//        }
    }
}
