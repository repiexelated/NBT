package net.querz.mca;

import junit.framework.TestCase;

import java.util.regex.Pattern;

import static net.querz.mca.DataVersion.*;

public class DataVersionTest extends TestCase {
    private static final Pattern ALLOWED_ENUM_DESCRIPTION_PATTERN = Pattern.compile("^(?:FINAL|\\d{2}w\\d{2}[a-z]|(?:CT|XS|PRE|RC)\\d+|)");
    public void testEnumNamesMatchVersionInformation() {
        for (DataVersion dv : values()) {
            if (dv.id() != 0) {
                StringBuilder sb = new StringBuilder("JAVA_1_");
                sb.append(dv.minor()).append('_');
                if (dv.isFullRelease()) {
                    sb.append(dv.patch());
                } else {
                    sb.append(dv.getBuildDescription().toUpperCase());
                }
                assertEquals(sb.toString(), dv.name());
                assertTrue("Build description of " + dv.name() + " does not follow convention!",
                        ALLOWED_ENUM_DESCRIPTION_PATTERN.matcher(dv.getBuildDescription()).matches());
            }
        }
    }

    public void testBestForNegativeValue() {
        assertEquals(UNKNOWN, bestFor(-42));
    }

    public void testBestForExactFirst() {
        assertEquals(UNKNOWN, bestFor(0));
    }

    public void testBestForExactArbitrary() {
        assertEquals(JAVA_1_15_0, bestFor(2225));
    }

    public void testBestForBetween() {
        assertEquals(JAVA_1_10_2, bestFor(JAVA_1_11_0.id() - 1));
        assertEquals(JAVA_1_11_0, bestFor(JAVA_1_11_0.id() + 1));
    }

    public void testBestForExactLast() {
        final DataVersion last = values()[values().length - 1];
        assertEquals(last, bestFor(last.id()));
    }

    public void testBestForAfterLast() {
        final DataVersion last = values()[values().length - 1];
        assertEquals(last, bestFor(last.id() + 123));
    }

    public void testToString() {
        assertEquals("2724 (1.17)", JAVA_1_17_0.toString());
        assertEquals("2730 (1.17.1)", JAVA_1_17_1.toString());
        assertEquals("UNKNOWN", UNKNOWN.toString());
        assertEquals("2529 (1.16 20w17a)", JAVA_1_16_20W17A.toString());
    }
    
    public void testIsCrossedByTransition() {
        assertFalse(JAVA_1_15_19W36A.isCrossedByTransition(JAVA_1_15_19W36A.id(), JAVA_1_15_19W36A.id()));
        assertFalse(JAVA_1_15_19W36A.isCrossedByTransition(JAVA_1_15_0.id(), JAVA_1_15_1.id()));
        assertFalse(JAVA_1_15_19W36A.isCrossedByTransition(JAVA_1_14_3.id(), JAVA_1_14_4.id()));

        assertFalse(JAVA_1_15_19W36A.isCrossedByTransition(JAVA_1_15_19W36A.id(), JAVA_1_15_19W36A.id() + 1));
        assertFalse(JAVA_1_15_19W36A.isCrossedByTransition(JAVA_1_15_19W36A.id() + 1, JAVA_1_15_19W36A.id()));

        assertTrue(JAVA_1_15_19W36A.isCrossedByTransition(JAVA_1_15_19W36A.id() - 1, JAVA_1_15_19W36A.id()));
        assertTrue(JAVA_1_15_19W36A.isCrossedByTransition(JAVA_1_15_19W36A.id(), JAVA_1_15_19W36A.id() - 1));

        assertTrue(JAVA_1_15_19W36A.isCrossedByTransition(JAVA_1_15_19W36A.id() - 1, JAVA_1_15_19W36A.id() + 1));
        assertTrue(JAVA_1_15_19W36A.isCrossedByTransition(JAVA_1_15_19W36A.id() + 1, JAVA_1_15_19W36A.id() - 1));

        assertTrue(JAVA_1_15_19W36A.isCrossedByTransition(JAVA_1_14_4.id(), JAVA_1_16_0.id()));
        assertTrue(JAVA_1_15_19W36A.isCrossedByTransition(JAVA_1_16_0.id(), JAVA_1_14_4.id()));
    }
}
