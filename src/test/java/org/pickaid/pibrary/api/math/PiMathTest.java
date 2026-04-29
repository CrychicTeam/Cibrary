package org.pickaid.pibrary.api.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PiMathTest {
    @Test
    void remapTurnsOneRangeIntoAnother() {
        assertEquals(50.0D, PiMath.remap(0.0D, 200.0D, 0.0D, 100.0D, 100.0D));
    }

    @Test
    void wrapDegreesUsesShortestSignedRange() {
        assertEquals(-170.0D, PiMath.wrapDegrees(190.0D));
        assertEquals(20.0D, PiMath.shortestAngleDegrees(350.0D, 10.0D));
    }

    @Test
    void approachNeverOvershoots() {
        assertEquals(3.0D, PiMath.approach(0.0D, 10.0D, 3.0D));
        assertEquals(10.0D, PiMath.approach(8.0D, 10.0D, 3.0D));
        assertEquals(7.0D, PiMath.approach(10.0D, 0.0D, 3.0D));
    }

    @Test
    void invalidNumbersAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> PiMath.requireFinite(Double.NaN, "value"));
        assertThrows(IllegalArgumentException.class, () -> PiMath.clamp(1.0D, 2.0D, 0.0D));
        assertThrows(IllegalArgumentException.class, () -> PiMath.inverseLerp(1.0D, 1.0D, 1.0D));
    }
}
