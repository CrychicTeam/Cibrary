package org.pickaid.pibrary.api.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PiCurvesTest {
    @Test
    void smoothCurvesClampInput() {
        assertEquals(0.0D, PiCurves.SMOOTHSTEP.sample(-1.0D));
        assertEquals(1.0D, PiCurves.SMOOTHERSTEP.sample(2.0D));
    }

    @Test
    void easeInOutIsSymmetric() {
        PiCurve curve = PiCurves.easeInOut(2.0D);

        assertEquals(0.125D, curve.sample(0.25D), 1.0E-6D);
        assertEquals(0.875D, curve.sample(0.75D), 1.0E-6D);
    }

    @Test
    void bezierUsesYControlValues() {
        PiCurve curve = PiCurves.cubicBezierY(0.0D, 1.0D);

        assertEquals(0.5D, curve.sample(0.5D), 1.0E-6D);
    }

    @Test
    void invalidExponentIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> PiCurves.easeIn(0.0D));
    }
}
