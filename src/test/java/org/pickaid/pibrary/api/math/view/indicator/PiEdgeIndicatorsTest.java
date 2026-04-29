package org.pickaid.pibrary.api.math.view.indicator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.math.view.camera.PiCameraFrame;
import org.pickaid.pibrary.api.math.view.camera.PiCameraLens;
import org.pickaid.pibrary.api.math.view.camera.PiCameraPose;
import org.pickaid.pibrary.api.math.view.camera.PiViewport;
import org.pickaid.pibrary.api.math.view.visibility.PiVisibilityResult;

class PiEdgeIndicatorsTest {
    private static final PiCameraFrame FRAME = new PiCameraFrame(
        new PiCameraPose(Vec3.ZERO, new Vec3(0.0D, 0.0D, 1.0D), new Vec3(0.0D, 1.0D, 0.0D)),
        new PiCameraLens(90.0D, 0.1D, 128.0D),
        new PiViewport(100, 100)
    );

    @Test
    void visiblePointDoesNotProduceEdgeIndicator() {
        Optional<PiEdgeIndicator> indicator = PiEdgeIndicators.fromWorldPoint(FRAME, new Vec3(0.0D, 0.0D, 10.0D), 10.0D);

        assertTrue(indicator.isEmpty());
    }

    @Test
    void offscreenPointClampsToViewportEdge() {
        PiEdgeIndicator indicator = PiEdgeIndicators.fromWorldPoint(FRAME, new Vec3(20.0D, 0.0D, 10.0D), 10.0D).orElseThrow();

        assertEquals(PiVisibilityResult.OFFSCREEN, indicator.source());
        assertEquals(90.0D, indicator.screenX());
        assertEquals(50.0D, indicator.screenY());
        assertEquals(0.0D, indicator.angleRadians());
    }

    @Test
    void behindCameraPointStillProducesDirectionalEdgeIndicator() {
        PiEdgeIndicator indicator = PiEdgeIndicators.fromWorldPoint(FRAME, new Vec3(10.0D, 0.0D, -10.0D), 10.0D).orElseThrow();

        assertEquals(PiVisibilityResult.BEHIND_CAMERA, indicator.source());
        assertEquals(90.0D, indicator.screenX());
        assertEquals(50.0D, indicator.screenY());
        assertEquals(0.0D, indicator.angleRadians());
    }

    @Test
    void directlyBehindPointFallsBackToTopEdge() {
        PiEdgeIndicator indicator = PiEdgeIndicators.fromWorldPoint(FRAME, new Vec3(0.0D, 0.0D, -10.0D), 10.0D).orElseThrow();

        assertEquals(PiVisibilityResult.BEHIND_CAMERA, indicator.source());
        assertEquals(50.0D, indicator.screenX());
        assertEquals(10.0D, indicator.screenY());
        assertEquals(-Math.PI * 0.5D, indicator.angleRadians());
    }

    @Test
    void marginMustStayInsideViewport() {
        assertThrows(NullPointerException.class, () -> PiEdgeIndicators.fromWorldPoint(null, Vec3.ZERO, 10.0D));
        assertThrows(NullPointerException.class, () -> PiEdgeIndicators.fromWorldPoint(FRAME, null, 10.0D));
        assertThrows(IllegalArgumentException.class, () -> PiEdgeIndicators.fromWorldPoint(FRAME, Vec3.ZERO, -0.1D));
        assertThrows(IllegalArgumentException.class, () -> PiEdgeIndicators.fromWorldPoint(FRAME, Vec3.ZERO, 50.0D));
        assertThrows(NullPointerException.class, () -> PiEdgeIndicators.clamp(FRAME, null, 10.0D));
        assertThrows(IllegalArgumentException.class, () -> PiEdgeIndicators.clamp(FRAME, new Vec3(0.0D, 0.0D, 0.0D), Double.POSITIVE_INFINITY));
    }

    @Test
    void clampReturnsInteriorEdgePoint() {
        Vec3 clamped = PiEdgeIndicators.clamp(FRAME, new Vec3(3.0D, -1.0D, 0.0D), 10.0D);

        assertEquals(90.0D, clamped.x);
        assertEquals(36.666666666666664D, clamped.y);
        assertEquals(0.0D, clamped.z);
        assertFalse(Double.isNaN(clamped.x));
        assertFalse(Double.isNaN(clamped.y));
    }
}
