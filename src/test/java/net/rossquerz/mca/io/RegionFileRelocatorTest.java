package net.rossquerz.mca.io;

import junit.framework.TestCase;
import net.rossquerz.mca.ChunkBase;
import net.rossquerz.mca.McaRegionFile;
import net.rossquerz.nbt.io.TextNbtHelpers;

import java.io.IOException;
import java.nio.file.Paths;

import static net.rossquerz.mca.io.MoveChunkFlags.*;

public class RegionFileRelocatorTest extends TestCase {

    public void testFoo() throws IOException {
////        String inRoot = "F:\\MinorArchive\\PluginDevServer\\plugins\\DuelEternal\\duel-templates\\drop-new-templates-here\\Desert.zip";
//        String inRoot = "F:\\MinorArchive\\PluginDevServer\\plugins\\DuelEternal\\duel-templates\\drop-new-templates-here\\Plains.zip";
////        String inRoot = "F:\\MinorArchive\\PluginDevServer\\plugins\\DuelEternal\\duel-templates\\drop-new-templates-here\\Savannah.zip";
////        String inRoot = "F:\\MinorArchive\\PluginDevServer\\plugins\\DuelEternal\\duel-templates\\drop-new-templates-here\\end_island.zip";
//        String outRoot = "F:\\MinorArchive\\PluginDevServer\\world";
////
//        RegionFileRelocator relocator = new RegionFileRelocator()
//                .sourceRoot(inRoot)
//                .destinationRoot(outRoot)
//                .addMoveChunkFlags(DISCARD_UPGRADE_DATA);
//        relocator.relocate("r.0.0.mca", "r.20.20.mca");
//        relocator.relocateAll(40, 40);


////        assertTrue(relocator.relocate("r.-2.0.mca", "r.-2.0.mca"));
//        assertTrue(relocator.relocate("r.-2.0.mca", "r.10.10.mca"));

//        McaRegionFile mca = McaFileHelpers.readAuto("F:\\MinorArchive\\PluginDevServer\\world\\region\\r.20.20.mca");
//        for (int i = 0; i < 1024; i++) {
//            ChunkBase chunk = mca.getChunk(i);
//            if (chunk != null)
//                TextNbtHelpers.writeTextNbtFile(
//                        Paths.get("TESTDBG", "wip", "OUT.PLAINS.r.20.20", "region", "i" + String.format("%04d", i) + "." + chunk.getChunkXZ().toString("x%dz%d") + ".snbt"),
//                        chunk.getHandle(), /*pretty print*/ true, /*sorted*/ true);
//        }

//        McaRegionFile mca = McaFileHelpers.readAuto("F:\\MinorArchive\\PluginDevServer\\plugins\\DuelEternal\\duel-templates\\drop-new-templates-here\\region\\r.0.0.mca");
//        for (int i = 0; i < 1024; i++) {
//            ChunkBase chunk = mca.getChunk(i);
//            if (chunk != null)
//                TextNbtHelpers.writeTextNbtFile(
//                        Paths.get("TESTDBG", "wip", "IN.PLAINS.r.0.0", "region", "i" + String.format("%04d", i) + "." + chunk.getChunkXZ().toString("x%dz%d") + ".snbt"),
//                        chunk.getHandle(), /*pretty print*/ true, /*sorted*/ true);
//        }
    }
}
