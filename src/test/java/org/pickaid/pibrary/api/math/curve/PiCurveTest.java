package org.pickaid.pibrary.api.math.curve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.math.core.PiRange;

class PiCurveTest {
    @Test
    void builtInCurvesSamplePredictably() {
        assertEquals(0.5D, PiCurves.linear().sample(0.5D));
        assertEquals(0.25D, PiCurves.easeInQuad().sample(0.5D));
        assertEquals(0.75D, PiCurves.easeOutQuad().sample(0.5D));
        assertEquals(0.5D, PiCurves.easeInOutQuad().sample(0.5D));
        assertEquals(0.125D, PiCurves.easeInCubic().sample(0.5D));
        assertEquals(0.875D, PiCurves.easeOutCubic().sample(0.5D));
        assertEquals(0.5D, PiCurves.easeInOutCubic().sample(0.5D));
        assertEquals(0.5D, PiCurves.smoothstep().sample(0.5D));
        assertEquals(0.5D, PiCurves.smootherstep().sample(0.5D));
    }

    @Test
    void piecewiseCurveNormalizesInputWithinSegmentsAndUsesEdgeSegmentsOutsideCoverage() {
        PiPiecewiseCurve curve = PiPiecewiseCurve.of(List.of(
            new PiPiecewiseCurve.Segment(new PiRange(0.0D, 0.5D), PiCurves.linear()),
            new PiPiecewiseCurve.Segment(new PiRange(0.5D, 1.0D), PiCurves.easeOutQuad())
        ));

        assertEquals(0.5D, curve.sample(0.25D));
        assertEquals(0.75D, curve.sample(0.75D));
        assertEquals(-0.5D, curve.sample(-0.25D));
        assertEquals(1.0D, curve.sample(1.0D));
    }

    @Test
    void piecewiseCurveUsesEarlierSegmentAtSharedBoundary() {
        PiPiecewiseCurve curve = PiPiecewiseCurve.of(List.of(
            new PiPiecewiseCurve.Segment(new PiRange(0.0D, 0.5D), PiCurves.linear()),
            new PiPiecewiseCurve.Segment(new PiRange(0.5D, 1.0D), PiCurves.easeInQuad())
        ));

        assertEquals(1.0D, curve.sample(0.5D));
    }

    @Test
    void piecewiseCurveRejectsEmptyOrBrokenSegmentLayout() {
        assertThrows(IllegalArgumentException.class, () -> PiPiecewiseCurve.of(List.of()));
        assertThrows(IllegalArgumentException.class, () -> PiPiecewiseCurve.of(List.of(
            new PiPiecewiseCurve.Segment(new PiRange(0.0D, 0.6D), PiCurves.linear()),
            new PiPiecewiseCurve.Segment(new PiRange(0.5D, 1.0D), PiCurves.easeOutQuad())
        )));
        assertThrows(IllegalArgumentException.class, () -> PiPiecewiseCurve.of(List.of(
            new PiPiecewiseCurve.Segment(new PiRange(0.0D, 0.4D), PiCurves.linear()),
            new PiPiecewiseCurve.Segment(new PiRange(0.5D, 1.0D), PiCurves.easeOutQuad())
        )));
    }

    @Test
    void curveSamplesCreateNormalizedTable() {
        assertIterableEquals(List.of(0.0D, 0.5D, 1.0D), PiCurveSamples.sample(PiCurves.linear(), 3));
    }

    @Test
    void curveSamplesRejectInvalidStepCounts() {
        assertThrows(IllegalArgumentException.class, () -> PiCurveSamples.sample(PiCurves.linear(), 1));
    }
}
