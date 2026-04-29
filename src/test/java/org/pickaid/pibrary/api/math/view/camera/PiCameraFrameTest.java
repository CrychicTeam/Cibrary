package org.pickaid.pibrary.api.math.view.camera;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.pickaid.pibrary.api.math.view.transform.PiSpaceTransform;
import org.pickaid.pibrary.api.math.view.transform.PiSpaceTransforms;

class PiCameraFrameTest {
    @Test
    void poseBuildsStableBasisVectors() {
        PiCameraPose pose = new PiCameraPose(Vec3.ZERO, new Vec3(0.0D, 0.0D, 2.0D), new Vec3(0.0D, 4.0D, 0.0D));

        assertEquals(new Vec3(0.0D, 0.0D, 1.0D), pose.forward());
        assertEquals(new Vec3(0.0D, 1.0D, 0.0D), pose.up());
        assertEquals(new Vec3(1.0D, 0.0D, 0.0D), pose.right());
    }

    @Test
    void fixedCameraSourceReturnsStableFrame() {
        PiCameraFrame frame = new PiCameraFrame(
            new PiCameraPose(Vec3.ZERO, new Vec3(0.0D, 0.0D, 1.0D), new Vec3(0.0D, 1.0D, 0.0D)),
            new PiCameraLens(90.0D, 0.1D, 128.0D),
            new PiViewport(1920, 1080)
        );

        assertSame(frame, PiCameraSource.fixed(frame).capture(0.25F));
        assertEquals(1920.0D / 1080.0D, frame.viewport().aspectRatio());
    }

    @Test
    void frameBuildsMatrixFirstProjectionState() {
        PiCameraFrame frame = new PiCameraFrame(
            new PiCameraPose(Vec3.ZERO, new Vec3(0.0D, 0.0D, 1.0D), new Vec3(0.0D, 1.0D, 0.0D)),
            new PiCameraLens(90.0D, 0.1D, 128.0D),
            new PiViewport(100, 100)
        );

        Matrix4f view = frame.viewMatrix();
        Vector4f viewSpace = view.transform(new Vector4f(0.0F, 0.0F, 10.0F, 1.0F));
        assertEquals(0.0D, viewSpace.x(), 1.0E-6D);
        assertEquals(0.0D, viewSpace.y(), 1.0E-6D);
        assertEquals(-10.0D, viewSpace.z(), 1.0E-6D);

        Vector4f clipSpace = frame.viewProjectionMatrix().transform(new Vector4f(0.0F, 0.0F, 10.0F, 1.0F));
        assertEquals(0.0D, clipSpace.x(), 1.0E-6D);
        assertEquals(0.0D, clipSpace.y(), 1.0E-6D);
        assertEquals(10.0D, clipSpace.w(), 1.0E-6D);
    }

    @Test
    void gameplayViewBuildsFrameFromVanillaStyleInputs() {
        PiCameraFrame frame = PiCameraFrame.gameplayView(
            new Vec3(3.0D, 4.0D, 5.0D),
            new Vector3f(0.0F, 0.0F, 1.0F),
            new Vector3f(0.0F, 1.0F, 0.0F),
            70.0D,
            192.0D,
            320,
            180
        );

        assertEquals(new Vec3(3.0D, 4.0D, 5.0D), frame.pose().position());
        assertEquals(new Vec3(0.0D, 0.0D, 1.0D), frame.pose().forward());
        assertEquals(new Vec3(0.0D, 1.0D, 0.0D), frame.pose().up());
        assertEquals(70.0D, frame.lens().verticalFovDegrees());
        assertEquals(0.05D, frame.lens().nearPlane());
        assertEquals(192.0D, frame.lens().farPlane());
        assertEquals(320, frame.viewport().width());
        assertEquals(180, frame.viewport().height());
    }

    @Test
    void minecraftDefaultLensUsesExpectedNearPlane() {
        PiCameraLens lens = PiCameraLens.minecraftDefault(90.0D, 256.0D);

        assertEquals(90.0D, lens.verticalFovDegrees());
        assertEquals(0.05D, lens.nearPlane());
        assertEquals(256.0D, lens.farPlane());
    }

    @Test
    void spaceTransformsComposeExplicitly() {
        PiSpaceTransform transform = PiSpaceTransforms.offset(new Vec3(2.0D, 0.0D, 0.0D))
            .then(PiSpaceTransforms.offset(new Vec3(0.0D, 3.0D, 0.0D)));

        assertEquals(new Vec3(2.0D, 3.0D, 0.0D), transform.toWorld(Vec3.ZERO));
        assertSame(Vec3.ZERO, PiSpaceTransforms.identity().toWorld(Vec3.ZERO));
    }

    @Test
    void invalidCameraArgumentsFailFast() {
        assertThrows(NullPointerException.class, () ->
            new PiCameraPose(null, new Vec3(0.0D, 0.0D, 1.0D), new Vec3(0.0D, 1.0D, 0.0D)));
        assertThrows(IllegalArgumentException.class, () ->
            new PiCameraPose(Vec3.ZERO, Vec3.ZERO, new Vec3(0.0D, 1.0D, 0.0D)));
        assertThrows(IllegalArgumentException.class, () ->
            new PiCameraPose(Vec3.ZERO, new Vec3(0.0D, 0.0D, 1.0D), new Vec3(0.0D, 0.0D, 2.0D)));
        assertThrows(IllegalArgumentException.class, () ->
            new PiCameraLens(180.0D, 0.1D, 16.0D));
        assertThrows(IllegalArgumentException.class, () ->
            new PiCameraLens(90.0D, 0.0D, 16.0D));
        assertThrows(IllegalArgumentException.class, () ->
            new PiViewport(0, 1080));
        assertThrows(NullPointerException.class, () ->
            PiCameraFrame.fromMatrices(
                new PiCameraPose(Vec3.ZERO, new Vec3(0.0D, 0.0D, 1.0D), new Vec3(0.0D, 1.0D, 0.0D)),
                new PiCameraLens(90.0D, 0.1D, 16.0D),
                new PiViewport(100, 100),
                null,
                new Matrix4f()
            ));
        assertThrows(NullPointerException.class, () -> PiCameraSource.fixed(null));
        assertThrows(NullPointerException.class, () -> PiSpaceTransforms.offset(null));
        assertThrows(NullPointerException.class, () -> PiSpaceTransforms.identity().then(null));
    }
}
