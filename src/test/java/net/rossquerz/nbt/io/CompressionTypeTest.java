package net.rossquerz.nbt.io;

import junit.framework.TestCase;

public class CompressionTypeTest extends TestCase {

	public void testGetFromID() {
		assertEquals(CompressionType.NONE, CompressionType.getFromID(CompressionType.NONE.getID()));
		assertEquals(CompressionType.GZIP, CompressionType.getFromID(CompressionType.GZIP.getID()));
		assertEquals(CompressionType.ZLIB, CompressionType.getFromID(CompressionType.ZLIB.getID()));
		assertNull(CompressionType.getFromID((byte) -1));
	}
}
