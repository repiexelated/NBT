package net.querz.mca.util;


import junit.framework.TestCase;

public class ChunkBoundingRectangleTest extends TestCase {

    public void testRelocate_int() {
        ChunkBoundingRectangle cbr = new ChunkBoundingRectangle(1, -1);  // [16..32), [-16..0)

        assertEquals(16, cbr.relocateX(0));
        assertEquals(16 + 3, cbr.relocateX(3));
        assertEquals(16 + 7, cbr.relocateX(16 + 7));
        assertEquals(16 + 15, cbr.relocateX(-1));

        assertEquals(-1, cbr.relocateZ(16 * 53 - 1));
        assertEquals(-15, cbr.relocateZ(16 * -53 - 15));
        assertEquals(-16 + 3, cbr.relocateZ(3));
        assertEquals(-16 + 7, cbr.relocateZ(16 + 7));
        assertEquals(-16 + 15, cbr.relocateZ(-1));
    }

    public void testRelocate_double() {
        ChunkBoundingRectangle cbr = new ChunkBoundingRectangle(0, 1);  // [0..16), [16..32)

        assertEquals(16 - 1e-6, cbr.relocateX(-1e-6), 1e-10);
        assertEquals(16 - 0.5, cbr.relocateX(41 * 16 -0.5), 1e-10);
        assertEquals(16 - 0.5, cbr.relocateX(-41 * 16 -0.5), 1e-10);

        assertEquals(16 + 1e-6, cbr.relocateZ(1e-6), 1e-10);
        assertEquals(16 + 1e-6, cbr.relocateZ(41 * 16 + 1e-6), 1e-10);
        assertEquals(16 + 0.5, cbr.relocateZ(41 * 16 + 0.5), 1e-10);
        assertEquals(16 + 6.789, cbr.relocateZ(-41 * 16 + 6.789), 1e-10);
    }
}
