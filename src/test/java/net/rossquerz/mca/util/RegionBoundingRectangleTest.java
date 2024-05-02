package net.rossquerz.mca.util;

import junit.framework.TestCase;

public class RegionBoundingRectangleTest extends TestCase {

    public void testContainsBlock() {
        RegionBoundingRectangle rbr = new RegionBoundingRectangle(1, -1);  // [512..1024), [-512..0) | [32..64), [-32..0)

        assertEquals(512, rbr.getMinBlockX());
        assertEquals(-512, rbr.getMinBlockZ());
        assertEquals(1024, rbr.getMaxBlockX());
        assertEquals(0, rbr.getMaxBlockZ());
        assertEquals(1, rbr.getWidthRegionXZ());
        assertEquals(32, rbr.getWidthChunkXZ());
        assertEquals(512, rbr.getWidthBlockXZ());

        assertTrue(rbr.containsBlock(512, -512));
        assertTrue(rbr.containsBlock(1023, -1));
        assertFalse(rbr.containsBlock(511, -512));
        assertFalse(rbr.containsBlock(1023, 0));

        rbr = new RegionBoundingRectangle(0, 0, 10);  // [512..1024), [-512..0) | [32..64), [-32..0)

        assertEquals(0, rbr.getMinBlockX());
        assertEquals(0, rbr.getMinBlockZ());
        assertEquals(5120, rbr.getMaxBlockX());
        assertEquals(5120, rbr.getMaxBlockZ());
        assertEquals(10, rbr.getWidthRegionXZ());
        assertEquals(10 * 32, rbr.getWidthChunkXZ());
        assertEquals(10 * 512, rbr.getWidthBlockXZ());
    }

    public void testContainsChunk() {
        RegionBoundingRectangle rbr = new RegionBoundingRectangle(1, -1);  // [512..1024), [-512..0) | [32..64), [-32..0)
        assertTrue(rbr.containsChunk(32, -32));
        assertTrue(rbr.containsChunk(63, -1));
        assertFalse(rbr.containsChunk(31, -32));
        assertFalse(rbr.containsChunk(63, 0));
    }

    public void testContainsRegion() {
        RegionBoundingRectangle rbr = new RegionBoundingRectangle(0, 0, 5);  // [512..1024), [-512..0) | [32..64), [-32..0)
        assertTrue(rbr.containsRegion(0, 0));
        assertTrue(rbr.containsRegion(4, 4));
        assertFalse(rbr.containsRegion(-1, 2));
        assertFalse(rbr.containsRegion(2, 5));
    }

    public void testForChunk() {
        RegionBoundingRectangle rbr = RegionBoundingRectangle.forChunk(42, -7);
        assertEquals(1, rbr.getMinRegionX());
        assertEquals(-1, rbr.getMinRegionZ());
        assertEquals(1, rbr.getWidthRegionXZ());
        assertEquals(1024, rbr.getMaxBlockX());
        assertEquals(512, rbr.getMinBlockX());
        assertEquals(0, rbr.getMaxBlockZ());
        assertEquals(-512, rbr.getMinBlockZ());

        rbr = RegionBoundingRectangle.forChunk(32, -32);
        assertEquals(1, rbr.getMinRegionX());
        assertEquals(-1, rbr.getMinRegionZ());
        assertEquals(1, rbr.getWidthRegionXZ());

        rbr = RegionBoundingRectangle.forChunk(63, -1);
        assertEquals(1, rbr.getMinRegionX());
        assertEquals(-1, rbr.getMinRegionZ());
        assertEquals(1, rbr.getWidthRegionXZ());
    }

    public void testWORLD_REGION_BOUNDS() {
        assertEquals(-58594, RegionBoundingRectangle.MAX_WORLD_REGION_BOUNDS.getMinRegionX());
        assertEquals(-58594, RegionBoundingRectangle.MAX_WORLD_REGION_BOUNDS.getMinRegionZ());
        assertEquals(58594, RegionBoundingRectangle.MAX_WORLD_REGION_BOUNDS.getMaxRegionX());
        assertEquals(58594, RegionBoundingRectangle.MAX_WORLD_REGION_BOUNDS.getMaxRegionZ());
        assertTrue(RegionBoundingRectangle.MAX_WORLD_REGION_BOUNDS.containsRegion(-58594, -58594));
        assertTrue(RegionBoundingRectangle.MAX_WORLD_REGION_BOUNDS.containsRegion(58593, 58593));
        assertFalse(RegionBoundingRectangle.MAX_WORLD_REGION_BOUNDS.containsRegion(58594, 58594));
    }
}
