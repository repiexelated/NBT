package net.querz.mca.util;

import junit.framework.TestCase;
import net.querz.util.BlockAlignedBoundingRectangle;
import net.querz.util.IntPointXZ;

public class BlockAlignedBoundingRectangleTest extends TestCase {

    public void testMinMaxXZ() {
        BlockAlignedBoundingRectangle cbr = new BlockAlignedBoundingRectangle(16, -16, 16);
        assertEquals(16, cbr.getMinX());
        assertEquals(32, cbr.getMaxX());
        assertEquals(-16, cbr.getMinZ());
        assertEquals(0, cbr.getMaxZ());
    }

    public void testContains_int() {
        BlockAlignedBoundingRectangle cbr = new BlockAlignedBoundingRectangle(16, -16, 16);
        assertTrue(cbr.contains(16 + 8, -8));
        assertFalse(cbr.contains(-8, 8));

        assertTrue(cbr.contains(16, -1));
        assertFalse(cbr.contains(15, -1));
        assertFalse(cbr.contains(16, 0));

        assertTrue(cbr.contains(16, -16));
        assertFalse(cbr.contains(15, -16));
        assertFalse(cbr.contains(16, -17));

        assertTrue(cbr.contains(31, -1));
        assertFalse(cbr.contains(32, -1));
        assertFalse(cbr.contains(31, 0));

        assertTrue(cbr.contains(31, -16));
        assertFalse(cbr.contains(32, -16));
        assertFalse(cbr.contains(31, -17));
    }

    public void testContains_IntPointXZ() {
        BlockAlignedBoundingRectangle cbr = new BlockAlignedBoundingRectangle(0, 0, 16);
        assertTrue(cbr.contains(new IntPointXZ(8, 8)));
        assertTrue(cbr.contains(new IntPointXZ(0, 0)));
        assertTrue(cbr.contains(new IntPointXZ(15, 15)));
        assertFalse(cbr.contains(new IntPointXZ(-1, 7)));
        assertFalse(cbr.contains(new IntPointXZ(16, 7)));
        assertFalse(cbr.contains(new IntPointXZ(7, -1)));
        assertFalse(cbr.contains(new IntPointXZ(7, 16)));
    }

    public void testContains_double() {
        BlockAlignedBoundingRectangle cbr = new BlockAlignedBoundingRectangle(16, -16, 16);
        assertTrue(cbr.contains(16 + 7.5, -7.5));
        assertFalse(cbr.contains(-8.5, 8.5));

        assertTrue(cbr.contains(16.0, -1e-14));
        assertTrue(cbr.contains(16.0, -16.0));
        assertTrue(cbr.contains(32 - 1e-14, -1e-14));
        assertTrue(cbr.contains(32 - 1e-14, -16.0));

        assertFalse(cbr.contains(16 - 1e-14, -7.5));  // off left
        assertFalse(cbr.contains(32.0, -7.5));  // off right
        assertFalse(cbr.contains(16 + 7.5, 0.0));  // off top
        assertFalse(cbr.contains(16 + 7.5, -16.0 - 1e-14));  // off bottom
    }
}
