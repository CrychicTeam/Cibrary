package org.pickaid.pibrary.api.math.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class PiScalarsTest {
    @Test
    void coreScalarHelpersClampLerpRemapAndCompareValues() {
        assertEquals(5.0, PiScalars.clamp(7.5, 0.0, 5.0));
        assertEquals(0.0, PiScalars.clamp01(-0.5));
        assertEquals(0.75, PiScalars.clamp01(0.75));
        assertEquals(1.0, PiScalars.clamp01(1.5));
        assertEquals(12.0, PiScalars.lerp(10.0, 18.0, 0.25));
        assertEquals(0.25, PiScalars.inverseLerp(10.0, 18.0, 12.0));
        assertEquals(150.0, PiScalars.remap(0.5, 0.0, 1.0, 100.0, 200.0));
        assertEquals(6.5, PiScalars.approach(5.0, 10.0, 1.5));
        assertEquals(8.5, PiScalars.approach(10.0, 5.0, 1.5));
        assertEquals(10.0, PiScalars.approach(5.0, 10.0, 10.0));
        assertEquals(0.5, PiScalars.positiveModulo(-0.5, 1.0));
        assertEquals(170.0, PiScalars.wrap(530.0, -180.0, 180.0));
        assertTrue(PiScalars.nearZero(0.0005, 0.001));
        assertTrue(PiScalars.epsilonEquals(4.0, 4.0008, 0.001));
        assertFalse(PiScalars.epsilonEquals(4.0, 4.01, 0.001));
    }

    @Test
    void rangeSupportsContainmentClampSpanAndNormalization() {
        PiRange range = new PiRange(-2.0, 6.0);

        assertTrue(range.contains(-2.0));
        assertTrue(range.contains(2.0));
        assertFalse(range.contains(6.5));
        assertEquals(-2.0, range.clamp(-3.0));
        assertEquals(8.0, range.span());
        assertEquals(2.0, range.center());
        assertEquals(0.5, range.normalize(2.0));
        assertEquals(0.0, range.normalizeClamped(-10.0));
        assertEquals(1.0, range.normalizeClamped(100.0));
        assertEquals(2.0, range.lerp(0.5));
        assertEquals(new PiRange(-3.0, 7.0), range.expand(1.0));
        assertEquals(new PiRange(-12.0, 16.0), range.expand(10.0));
    }

    @Test
    void samplesSupportNormalizedStepAndEvenSpacing() {
        assertEquals(0.5, PiSamples.normalizedStep(1, 3));
        assertIterableEquals(List.of(-2.0, 0.0, 2.0), PiSamples.evenlySpaced(new PiRange(-2.0, 2.0), 3));
    }

    @Test
    void invalidArgumentsFailFast() {
        assertThrows(IllegalArgumentException.class, () -> PiScalars.clamp(1.0, 5.0, 4.0));
        assertThrows(IllegalArgumentException.class, () -> PiScalars.inverseLerp(1.0, 1.0, 0.5));
        assertThrows(IllegalArgumentException.class, () -> PiScalars.approach(1.0, 2.0, -0.1));
        assertThrows(IllegalArgumentException.class, () -> PiScalars.positiveModulo(1.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> PiScalars.wrap(1.0, 5.0, 5.0));
        assertThrows(IllegalArgumentException.class, () -> PiScalars.nearZero(0.1, -0.001));
        assertThrows(IllegalArgumentException.class, () -> PiScalars.epsilonEquals(4.0, 4.0, -0.001));
        assertThrows(IllegalArgumentException.class, () -> new PiRange(6.0, -2.0));
        assertThrows(IllegalArgumentException.class, () -> new PiRange(-2.0, 6.0).expand(-4.1));
        assertThrows(IllegalArgumentException.class, () -> PiSamples.normalizedStep(0, 0));
        assertThrows(IllegalArgumentException.class, () -> PiSamples.normalizedStep(4, 3));
        assertThrows(IllegalArgumentException.class, () -> PiSamples.evenlySpaced(new PiRange(-2.0, 2.0), 1));
        assertThrows(IllegalArgumentException.class, () -> PiSamples.evenlySpaced(new PiRange(-2.0, 2.0), 0));
    }
}
