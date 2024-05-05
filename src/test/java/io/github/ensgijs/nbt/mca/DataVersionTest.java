package io.github.ensgijs.nbt.mca;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class DataVersionTest extends McaTestCase {
    private static final Pattern ALLOWED_ENUM_DESCRIPTION_PATTERN = Pattern.compile("^(?:FINAL|\\d{2}w\\d{2}[a-z]|CT\\d+[a-z]?|(?:XS|PRE|RC)\\d+|)");

    public void testEnumNamesMatchVersionInformation() {
        for (DataVersion dv : DataVersion.values()) {
            if (dv.id() != 0) {
                StringBuilder sb = new StringBuilder("JAVA_1_");
                sb.append(dv.minor()).append('_');
                if (dv.isFullRelease()) {
                    sb.append(dv.patch());
                } else {
                    if (dv.patch() > 0) sb.append(dv.patch()).append('_');
                    sb.append(dv.getBuildDescription().toUpperCase());
                }
                assertEquals(sb.toString(), dv.name());
                assertTrue("Build description of " + dv.name() + " does not follow convention!",
                        ALLOWED_ENUM_DESCRIPTION_PATTERN.matcher(dv.getBuildDescription()).matches());
            }
        }
    }

    public void testEnumDataVersionCollisions() {
        Set<Integer> seen = new HashSet<>();
        for (DataVersion dv : DataVersion.values()) {
            if (dv.id() != 0) {
                assertTrue("duplicate data version " + dv.id(), seen.add(dv.id()));
            }
        }
    }

    public void testEnumDataVersionsIncreasingOrder() {
        int last = 0;
        for (DataVersion dv : DataVersion.values()) {
            if (dv.id() != 0) {
                assertTrue(dv.toString() + " is out of order", dv.id() >= last);
                last = dv.id();
            }
        }
    }

    public void testBestForNegativeValue() {
        assertEquals(DataVersion.UNKNOWN, DataVersion.bestFor(-42));
    }

    public void testBestForExactFirst() {
        assertEquals(DataVersion.UNKNOWN, DataVersion.bestFor(0));
    }

    public void testBestForExactArbitrary() {
        assertEquals(DataVersion.JAVA_1_15_0, DataVersion.bestFor(2225));
    }

    public void testBestForBetween() {
        assertEquals(DataVersion.JAVA_1_9_15W32A, DataVersion.bestFor(150));
    }

    public void testBestForExactLast() {
        final DataVersion last = DataVersion.values()[DataVersion.values().length - 1];
        assertEquals(last, DataVersion.bestFor(last.id()));
    }

    public void testBestForAfterLast() {
        final DataVersion last = DataVersion.values()[DataVersion.values().length - 1];
        assertEquals(last, DataVersion.bestFor(last.id() + 123));
    }

    public void testToString() {
        assertEquals("2724 (1.17)", DataVersion.JAVA_1_17_0.toString());
        assertEquals("2730 (1.17.1)", DataVersion.JAVA_1_17_1.toString());
        assertEquals("UNKNOWN", DataVersion.UNKNOWN.toString());
        assertEquals("2529 (1.16 20w17a)", DataVersion.JAVA_1_16_20W17A.toString());
        assertEquals("2864 (1.18.1 RC3)", DataVersion.JAVA_1_18_1_RC3.toString());
    }
    
    public void testIsCrossedByTransition() {
        assertFalse(DataVersion.JAVA_1_15_19W36A.isCrossedByTransition(DataVersion.JAVA_1_15_19W36A.id(), DataVersion.JAVA_1_15_19W36A.id()));
        assertFalse(DataVersion.JAVA_1_15_19W36A.isCrossedByTransition(DataVersion.JAVA_1_15_0.id(), DataVersion.JAVA_1_15_1.id()));
        assertFalse(DataVersion.JAVA_1_15_19W36A.isCrossedByTransition(DataVersion.JAVA_1_14_3.id(), DataVersion.JAVA_1_14_4.id()));

        assertFalse(DataVersion.JAVA_1_15_19W36A.isCrossedByTransition(DataVersion.JAVA_1_15_19W36A.id(), DataVersion.JAVA_1_15_19W36A.id() + 1));
        assertFalse(DataVersion.JAVA_1_15_19W36A.isCrossedByTransition(DataVersion.JAVA_1_15_19W36A.id() + 1, DataVersion.JAVA_1_15_19W36A.id()));

        assertTrue(DataVersion.JAVA_1_15_19W36A.isCrossedByTransition(DataVersion.JAVA_1_15_19W36A.id() - 1, DataVersion.JAVA_1_15_19W36A.id()));
        assertTrue(DataVersion.JAVA_1_15_19W36A.isCrossedByTransition(DataVersion.JAVA_1_15_19W36A.id(), DataVersion.JAVA_1_15_19W36A.id() - 1));

        assertTrue(DataVersion.JAVA_1_15_19W36A.isCrossedByTransition(DataVersion.JAVA_1_15_19W36A.id() - 1, DataVersion.JAVA_1_15_19W36A.id() + 1));
        assertTrue(DataVersion.JAVA_1_15_19W36A.isCrossedByTransition(DataVersion.JAVA_1_15_19W36A.id() + 1, DataVersion.JAVA_1_15_19W36A.id() - 1));

        assertTrue(DataVersion.JAVA_1_15_19W36A.isCrossedByTransition(DataVersion.JAVA_1_14_4.id(), DataVersion.JAVA_1_16_0.id()));
        assertTrue(DataVersion.JAVA_1_15_19W36A.isCrossedByTransition(DataVersion.JAVA_1_16_0.id(), DataVersion.JAVA_1_14_4.id()));
    }

    public void testThrowUnsupportedVersionChangeIfCrossed() {
        assertThrowsException(() -> DataVersion.JAVA_1_15_19W36A.throwUnsupportedVersionChangeIfCrossed(DataVersion.JAVA_1_14_4.id(), DataVersion.JAVA_1_16_0.id()),
                UnsupportedVersionChangeException.class);
        assertThrowsNoException(() -> DataVersion.JAVA_1_15_19W36A.throwUnsupportedVersionChangeIfCrossed(DataVersion.JAVA_1_15_19W36A.id(), DataVersion.JAVA_1_15_19W36A.id()));
    }

    public void testPrevious() {
        assertSame(DataVersion.JAVA_1_9_1_PRE2, DataVersion.JAVA_1_9_1_PRE3.previous());
        assertNull(DataVersion.values()[0].previous());
    }

    public void testNext() {
        assertSame(DataVersion.JAVA_1_9_1, DataVersion.JAVA_1_9_1_PRE3.next());
        assertNull(DataVersion.values()[DataVersion.values().length - 1].next());
    }
}
