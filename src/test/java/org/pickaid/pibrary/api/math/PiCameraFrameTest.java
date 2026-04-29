package org.pickaid.pibrary.api.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class PiCameraFrameTest {
    @Test
    void projectsForwardPointToViewportCenter() {
        PiCameraFrame frame = frame();

        PiProjectedPoint point = frame.project(new Vec3(0.0D, 0.0D, 10.0D));

        assertTrue(point.insideViewport());
        assertEquals(400.0D, point.screenX(), 1.0E-6D);
        assertEquals(300.0D, point.screenY(), 1.0E-6D);
    }

    @Test
    void projectsRightPointToRightHalfOfViewport() {
        PiProjectedPoint point = frame().project(new Vec3(5.0D, 0.0D, 10.0D));

        assertTrue(point.screenX() > 400.0D);
        assertEquals(300.0D, point.screenY(), 1.0E-6D);
    }

    @Test
    void behindPointCanBePinnedToTopEdge() {
        PiViewport viewport = new PiViewport(800, 600);
        PiProjectedPoint point = frame().project(new Vec3(2.0D, 0.0D, -10.0D));

        assertFalse(point.inFront());
        assertEquals(12.0D, point.edgeClamped(viewport, 12.0D, true).y(), 1.0E-6D);
    }

    private static PiCameraFrame frame() {
        return new PiCameraFrame(
                Vec3.ZERO,
                new Vec3(0.0D, 0.0D, 1.0D),
                new Vec3(0.0D, 1.0D, 0.0D),
                90.0D,
                0.05D,
                1000.0D,
                new PiViewport(800, 600)
        );
    }
}
