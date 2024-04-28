package net.rossquerz.mca.util;

import junit.framework.TestCase;

public class RegionBoundingRectangleTest extends TestCase {

    public void testContainsBlock() {
        RegionBoundingRectangle rbr = new RegionBoundingRectangle(1, -1);  // [512..1024), [-512..0) | [32..64), [-32..0)
        assertTrue(rbr.contains(512, -512));
        assertTrue(rbr.contains(1023, -1));
        assertFalse(rbr.contains(511, -512));
        assertFalse(rbr.contains(1023, 0));
    }

    public void testContainsChunk() {
        RegionBoundingRectangle rbr = new RegionBoundingRectangle(1, -1);  // [512..1024), [-512..0) | [32..64), [-32..0)
        assertTrue(rbr.containsChunk(32, -32));
        assertTrue(rbr.containsChunk(63, -1));
        assertFalse(rbr.containsChunk(31, -32));
        assertFalse(rbr.containsChunk(63, 0));
    }

    public void testForChunk() {
        RegionBoundingRectangle rbr = RegionBoundingRectangle.forChunk(42, -7);
        assertEquals(1, rbr.getRegionX());
        assertEquals(-1, rbr.getRegionZ());
        assertEquals(1024, rbr.getMaxX());
        assertEquals(512, rbr.getMinX());
        assertEquals(0, rbr.getMaxZ());
        assertEquals(-512, rbr.getMinZ());

        rbr = RegionBoundingRectangle.forChunk(32, -32);
        assertEquals(1, rbr.getRegionX());
        assertEquals(-1, rbr.getRegionZ());

        rbr = RegionBoundingRectangle.forChunk(63, -1);
        assertEquals(1, rbr.getRegionX());
        assertEquals(-1, rbr.getRegionZ());
    }
}
