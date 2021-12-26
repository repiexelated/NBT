package net.querz.mca;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MCAUtilTest extends MCATestCase {

	public void testLocationConversion() {
		assertEquals(0, MCAUtil.blockToChunk(0));
		assertEquals(0, MCAUtil.blockToChunk(15));
		assertEquals(1, MCAUtil.blockToChunk(16));
		assertEquals(-1, MCAUtil.blockToChunk(-1));
		assertEquals(-1, MCAUtil.blockToChunk(-16));
		assertEquals(-2, MCAUtil.blockToChunk(-17));

		assertEquals(0, MCAUtil.blockToRegion(0));
		assertEquals(0, MCAUtil.blockToRegion(511));
		assertEquals(1, MCAUtil.blockToRegion(512));
		assertEquals(-1, MCAUtil.blockToRegion(-1));
		assertEquals(-1, MCAUtil.blockToRegion(-512));
		assertEquals(-2, MCAUtil.blockToRegion(-513));

		assertEquals(0, MCAUtil.chunkToRegion(0));
		assertEquals(0, MCAUtil.chunkToRegion(31));
		assertEquals(1, MCAUtil.chunkToRegion(32));
		assertEquals(-1, MCAUtil.chunkToRegion(-1));
		assertEquals(-1, MCAUtil.chunkToRegion(-32));
		assertEquals(-2, MCAUtil.chunkToRegion(-33));

		assertEquals(0, MCAUtil.regionToChunk(0));
		assertEquals(32, MCAUtil.regionToChunk(1));
		assertEquals(-32, MCAUtil.regionToChunk(-1));
		assertEquals(-64, MCAUtil.regionToChunk(-2));

		assertEquals(0, MCAUtil.regionToBlock(0));
		assertEquals(512, MCAUtil.regionToBlock(1));
		assertEquals(-512, MCAUtil.regionToBlock(-1));
		assertEquals(-1024, MCAUtil.regionToBlock(-2));

		assertEquals(0, MCAUtil.chunkToBlock(0));
		assertEquals(16, MCAUtil.chunkToBlock(1));
		assertEquals(-16, MCAUtil.chunkToBlock(-1));
		assertEquals(-32, MCAUtil.chunkToBlock(-2));
	}

	public void testBlockAbsoluteToChunkRelative_int() {
		assertEquals(0, MCAUtil.blockAbsoluteToChunkRelative(0));
		assertEquals(0, MCAUtil.blockAbsoluteToChunkRelative(16 * 81));
		assertEquals(15, MCAUtil.blockAbsoluteToChunkRelative(16 * 81 - 1));
		assertEquals(0, MCAUtil.blockAbsoluteToChunkRelative(-16 * 81));
		assertEquals(15, MCAUtil.blockAbsoluteToChunkRelative(-16 * 81 - 1));
		assertEquals(1, MCAUtil.blockAbsoluteToChunkRelative(-16 * 81 + 1));
	}

	public void testBlockAbsoluteToChunkRelative_double() {
		assertEquals(0.0, MCAUtil.blockAbsoluteToChunkRelative(0.0), 1e-10);
		assertEquals(16 - 1e-6, MCAUtil.blockAbsoluteToChunkRelative(-1e-6), 1e-10);
		assertEquals(3.4567, MCAUtil.blockAbsoluteToChunkRelative(16 * 81 + 3.4567), 1e-10);
		assertEquals(16 - 3.4567, MCAUtil.blockAbsoluteToChunkRelative(16 * 81 - 3.4567), 1e-10);
		assertEquals(16 - 3.4567, MCAUtil.blockAbsoluteToChunkRelative(-16 * 81 - 3.4567), 1e-10);
		assertEquals(3.4567, MCAUtil.blockAbsoluteToChunkRelative(-16 * 81 + 3.4567), 1e-10);
	}

	public void testCreateNameFromLocation() {
		assertEquals("r.0.0.mca", MCAUtil.createNameFromBlockLocation(0, 0));
		assertEquals("r.0.0.mca", MCAUtil.createNameFromBlockLocation(511, 511));
		assertEquals("r.1.0.mca", MCAUtil.createNameFromBlockLocation(512, 511));
		assertEquals("r.0.-1.mca", MCAUtil.createNameFromBlockLocation(511, -1));
		assertEquals("r.0.-1.mca", MCAUtil.createNameFromBlockLocation(511, -512));
		assertEquals("r.0.-2.mca", MCAUtil.createNameFromBlockLocation(511, -513));
		assertEquals("r.0.1.mca", MCAUtil.createNameFromBlockLocation(511, 512));
		assertEquals("r.-1.0.mca", MCAUtil.createNameFromBlockLocation(-1, 511));
		assertEquals("r.-1.0.mca", MCAUtil.createNameFromBlockLocation(-512, 511));
		assertEquals("r.-2.0.mca", MCAUtil.createNameFromBlockLocation(-513, 511));
		
		assertEquals("r.0.0.mca", MCAUtil.createNameFromChunkLocation(0, 0));
		assertEquals("r.0.0.mca", MCAUtil.createNameFromChunkLocation(31, 31));
		assertEquals("r.1.0.mca", MCAUtil.createNameFromChunkLocation(32, 31));
		assertEquals("r.0.-1.mca", MCAUtil.createNameFromChunkLocation(31, -1));
		assertEquals("r.0.-1.mca", MCAUtil.createNameFromChunkLocation(31, -32));
		assertEquals("r.0.-2.mca", MCAUtil.createNameFromChunkLocation(31, -33));
		assertEquals("r.0.1.mca", MCAUtil.createNameFromChunkLocation(31, 32));
		assertEquals("r.-1.0.mca", MCAUtil.createNameFromChunkLocation(-1, 31));
		assertEquals("r.-1.0.mca", MCAUtil.createNameFromChunkLocation(-32, 31));
		assertEquals("r.-2.0.mca", MCAUtil.createNameFromChunkLocation(-33, 31));
		
		assertEquals("r.0.0.mca", MCAUtil.createNameFromRegionLocation(0, 0));
		assertEquals("r.1.0.mca", MCAUtil.createNameFromRegionLocation(1, 0));
		assertEquals("r.0.-1.mca", MCAUtil.createNameFromRegionLocation(0, -1));
		assertEquals("r.0.-2.mca", MCAUtil.createNameFromRegionLocation(0, -2));
		assertEquals("r.0.1.mca", MCAUtil.createNameFromRegionLocation(0, 1));
		assertEquals("r.-1.0.mca", MCAUtil.createNameFromRegionLocation(-1, 0));
		assertEquals("r.-2.0.mca", MCAUtil.createNameFromRegionLocation(-2, 0));
	}

	public void testMakeMyCoverageGreatAgain() {
		assertThrowsException(() -> MCAUtil.read((String) null), NullPointerException.class);
		assertThrowsException(() -> MCAUtil.write(null, (String) null), NullPointerException.class);
		assertThrowsException(() -> MCAUtil.write(null, (File) null), NullPointerException.class);
		assertThrowsException(() -> MCAUtil.write(null, (String) null, false), NullPointerException.class);
		assertThrowsException(() -> MCAUtil.read("r.a.b.mca"), IllegalArgumentException.class);
		assertThrowsException(() -> new MCAFile(0, 0).serialize(null), IllegalArgumentException.class);

		// test overwriting file
		MCAFile m = new MCAFile(0, 0);
		m.setChunk(0, Chunk.newChunk());
		File target = getNewTmpFile("r.0.0.mca");
		assertThrowsNoException(() -> MCAUtil.write(m, target, false));
		assertThrowsNoException(() -> MCAUtil.write(m, target, false));
	}

	public void testAutoCreateMcaFile() {
		MCAFileBase<?> mcaFile = MCAUtil.autoMCAFile(Paths.get("region", "r.1.2.mca"));
		assertTrue(mcaFile instanceof MCAFile);
		assertEquals(1, mcaFile.getRegionX());
		assertEquals(2, mcaFile.getRegionZ());

		mcaFile = MCAUtil.autoMCAFile(Paths.get("poi", "r.3.-4.mca"));
		assertTrue(mcaFile instanceof PoiMCAFile);
		assertEquals(3, mcaFile.getRegionX());
		assertEquals(-4, mcaFile.getRegionZ());

		mcaFile = MCAUtil.autoMCAFile(Paths.get("entities", "r.-5.6.mca"));
		assertTrue(mcaFile instanceof EntitiesMCAFile);
		assertEquals(-5, mcaFile.getRegionX());
		assertEquals(6, mcaFile.getRegionZ());
	}
}
