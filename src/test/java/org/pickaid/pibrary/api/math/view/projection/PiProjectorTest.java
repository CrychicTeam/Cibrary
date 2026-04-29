package org.pickaid.pibrary.api.math.view.projection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.math.geometry.PiAabbs;
import org.pickaid.pibrary.api.math.view.camera.PiCameraFrame;
import org.pickaid.pibrary.api.math.view.camera.PiCameraLens;
import org.pickaid.pibrary.api.math.view.camera.PiCameraPose;
import org.pickaid.pibrary.api.math.view.camera.PiViewport;
import org.pickaid.pibrary.api.math.view.transform.PiSpaceTransforms;
import org.pickaid.pibrary.api.math.view.visibility.PiVisibility;
import org.pickaid.pibrary.api.math.view.visibility.PiVisibilityResult;

class PiProjectorTest {
    private static final PiCameraFrame FRAME = new PiCameraFrame(
        new PiCameraPose(Vec3.ZERO, new Vec3(0.0D, 0.0D, 1.0D), new Vec3(0.0D, 1.0D, 0.0D)),
        new PiCameraLens(90.0D, 0.1D, 128.0D),
        new PiViewport(100, 100)
    );

    @Test
    void pointInFrontProjectsToScreenCenter() {
        PiProjectedPoint point = PiProjector.projectPoint(FRAME, new Vec3(0.0D, 0.0D, 10.0D));

        assertEquals(50.0D, point.screenX());
        assertEquals(50.0D, point.screenY());
        assertEquals(10.0D, point.depth());
        assertTrue(point.inFront());
        assertTrue(point.onScreen());
        assertEquals(PiVisibilityResult.VISIBLE, PiVisibility.classify(point));
    }

    @Test
    void pointBehindCameraIsRejected() {
        PiProjectedPoint point = PiProjector.projectPoint(FRAME, new Vec3(0.0D, 0.0D, -5.0D));

        assertFalse(point.inFront());
        assertFalse(point.onScreen());
        assertEquals(PiVisibilityResult.BEHIND_CAMERA, PiVisibility.classify(point));
    }

    @Test
    void pointInFrontButOutsideViewportIsOffscreen() {
        PiProjectedPoint point = PiProjector.projectPoint(FRAME, new Vec3(20.0D, 0.0D, 10.0D));

        assertEquals(10.0D, point.depth());
        assertTrue(point.inFront());
        assertFalse(point.onScreen());
        assertEquals(PiVisibilityResult.OFFSCREEN, PiVisibility.classify(point));
    }

    @Test
    void pointPastFarPlaneDoesNotCountAsOnScreen() {
        PiProjectedPoint point = PiProjector.projectPoint(FRAME, new Vec3(0.0D, 0.0D, 200.0D));

        assertTrue(point.inFront());
        assertFalse(point.onScreen());
        assertEquals(PiVisibilityResult.OFFSCREEN, PiVisibility.classify(point));
    }

    @Test
    void localPointCanProjectThroughExplicitTransform() {
        PiProjectedPoint point = PiProjector.projectPoint(
            FRAME,
            PiSpaceTransforms.offset(new Vec3(0.0D, 0.0D, 10.0D)),
            Vec3.ZERO
        );

        assertTrue(point.onScreen());
        assertEquals(50.0D, point.screenX());
    }

    @Test
    void boundsProjectionBuildsVisibleScreenRectangle() {
        AABB box = PiAabbs.around(new Vec3(0.0D, 0.0D, 10.0D), 1.0D);
        PiProjectedBounds bounds = PiProjector.projectBounds(FRAME, box);

        assertTrue(bounds.projectable());
        assertTrue(bounds.visible());
        assertTrue(bounds.maxX() > bounds.minX());
        assertTrue(bounds.maxY() > bounds.minY());
        assertEquals(PiVisibilityResult.VISIBLE, PiVisibility.classify(bounds));
    }

    @Test
    void boundsBehindCameraAreNotProjectable() {
        AABB box = PiAabbs.around(new Vec3(0.0D, 0.0D, -10.0D), 1.0D);
        PiProjectedBounds bounds = PiProjector.projectBounds(FRAME, box);

        assertFalse(bounds.projectable());
        assertFalse(bounds.visible());
        assertEquals(PiVisibilityResult.NOT_PROJECTABLE, PiVisibility.classify(bounds));
    }

    @Test
    void invalidProjectionArgumentsFailFast() {
        assertThrows(NullPointerException.class, () -> PiProjector.projectPoint(null, Vec3.ZERO));
        assertThrows(NullPointerException.class, () -> PiProjector.projectPoint(FRAME, null));
        assertThrows(NullPointerException.class, () -> PiProjector.projectPoint(FRAME, null, Vec3.ZERO));
        assertThrows(NullPointerException.class, () -> PiProjector.projectPoint(FRAME, PiSpaceTransforms.identity(), null));
        assertThrows(NullPointerException.class, () -> PiProjector.projectBounds(FRAME, null));
        assertThrows(NullPointerException.class, () -> PiVisibility.classify((PiProjectedPoint) null));
        assertThrows(NullPointerException.class, () -> PiVisibility.classify((PiProjectedBounds) null));
    }
}
