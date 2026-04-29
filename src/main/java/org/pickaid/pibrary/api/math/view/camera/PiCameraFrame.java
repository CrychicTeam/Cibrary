package org.pickaid.pibrary.api.math.view.camera;

import java.util.Objects;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import net.minecraft.world.phys.Vec3;

/**
 * One complete snapshot of camera state used by projection code.
 *
 * <p>Use this when you want one stable object that describes "what the camera
 * looked like for this render moment". That keeps later math simple and avoids
 * passing loose pose, FOV, and viewport parameters through every call.</p>
 *
 * <p>Typical use is: capture one frame near the start of a render path, then
 * reuse that exact frame for every marker, label, or debug box you project
 * during that pass.</p>
 *
 * <p>The pose, lens, and viewport are still the human-readable inputs, but the
 * canonical projection path is matrix-first: a validated frame also carries the
 * derived view matrix, projection matrix, and combined view-projection matrix
 * that match the way Minecraft actually renders the world.</p>
 *
 * @param pose the normalized camera pose
 * @param lens the lens settings for projection
 * @param viewport the output viewport size
 * @param viewMatrix the world-to-camera transform
 * @param projectionMatrix the camera-to-clip transform
 * @param viewProjectionMatrix the combined projection * view transform
 */
public record PiCameraFrame(
        PiCameraPose pose,
        PiCameraLens lens,
        PiViewport viewport,
        Matrix4f viewMatrix,
        Matrix4f projectionMatrix,
        Matrix4f viewProjectionMatrix
) {
    /**
     * Creates a gameplay-oriented frame directly from the values vanilla
     * already exposes on the current camera and window.
     *
     * <p>This is the compact path for the common "project a world point into
     * the HUD" case. In normal Minecraft client code the arguments usually come
     * from:</p>
     * <pre>{@code
     * Minecraft minecraft = Minecraft.getInstance();
     * Camera camera = minecraft.gameRenderer.getMainCamera();
     *
     * PiCameraFrame frame = PiCameraFrame.gameplayView(
     *     camera.getPosition(),
     *     camera.getLookVector(),
     *     camera.getUpVector(),
     *     minecraft.options.fov().get(),
     *     minecraft.gameRenderer.getDepthFar(),
     *     minecraft.getWindow().getGuiScaledWidth(),
     *     minecraft.getWindow().getGuiScaledHeight()
     * );
     * }</pre>
     *
     * @param position the camera position in world space
     * @param forward the vanilla-style look vector
     * @param up the vanilla-style up vector
     * @param verticalFovDegrees the gameplay vertical field of view in degrees
     * @param farPlane the far clip distance
     * @param viewportWidth the output viewport width
     * @param viewportHeight the output viewport height
     * @return a complete gameplay camera frame
     * @throws NullPointerException if {@code position}, {@code forward}, or
     *     {@code up} is {@code null}
     * @throws IllegalArgumentException if the inputs do not describe a valid
     *     camera or viewport
     */
    public static PiCameraFrame gameplayView(
            Vec3 position,
            Vector3fc forward,
            Vector3fc up,
            double verticalFovDegrees,
            double farPlane,
            int viewportWidth,
            int viewportHeight
    ) {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(forward, "forward");
        Objects.requireNonNull(up, "up");
        return new PiCameraFrame(
                new PiCameraPose(position, new Vec3(forward.x(), forward.y(), forward.z()), new Vec3(up.x(), up.y(), up.z())),
                PiCameraLens.minecraftDefault(verticalFovDegrees, farPlane),
                new PiViewport(viewportWidth, viewportHeight)
        );
    }

    /**
     * Creates a validated frame and derives its matrices from pose, lens, and
     * viewport.
     *
     * @param pose the camera pose
     * @param lens the camera lens settings
     * @param viewport the viewport information
     */
    public PiCameraFrame(PiCameraPose pose, PiCameraLens lens, PiViewport viewport) {
        this(
                pose,
                lens,
                viewport,
                createViewMatrix(pose),
                createProjectionMatrix(lens, viewport),
                createViewProjectionMatrix(createProjectionMatrix(lens, viewport), createViewMatrix(pose))
        );
    }

    /**
     * Creates a validated camera frame.
     *
     * @param pose the camera pose
     * @param lens the camera lens settings
     * @param viewport the viewport information
     * @param viewMatrix the world-to-camera transform
     * @param projectionMatrix the camera-to-clip transform
     * @param viewProjectionMatrix the combined projection * view transform
     * @throws NullPointerException if any argument is {@code null}
     */
    public PiCameraFrame {
        Objects.requireNonNull(pose, "pose");
        Objects.requireNonNull(lens, "lens");
        Objects.requireNonNull(viewport, "viewport");
        Objects.requireNonNull(viewMatrix, "viewMatrix");
        Objects.requireNonNull(projectionMatrix, "projectionMatrix");
        Objects.requireNonNull(viewProjectionMatrix, "viewProjectionMatrix");
        viewMatrix = new Matrix4f(viewMatrix);
        projectionMatrix = new Matrix4f(projectionMatrix);
        viewProjectionMatrix = createViewProjectionMatrix(projectionMatrix, viewMatrix);
    }

    /**
     * Creates a validated frame from already-known matrices.
     *
     * <p>Use this when the caller already sampled real render matrices from a
     * client render path and wants to preserve those exact values.</p>
     *
     * @param pose the camera pose
     * @param lens the lens settings
     * @param viewport the projection target size
     * @param viewMatrix the world-to-camera transform
     * @param projectionMatrix the camera-to-clip transform
     * @return a complete camera frame
     */
    public static PiCameraFrame fromMatrices(
            PiCameraPose pose,
            PiCameraLens lens,
            PiViewport viewport,
            Matrix4f viewMatrix,
            Matrix4f projectionMatrix
    ) {
        Objects.requireNonNull(viewMatrix, "viewMatrix");
        Objects.requireNonNull(projectionMatrix, "projectionMatrix");
        return new PiCameraFrame(
                pose,
                lens,
                viewport,
                viewMatrix,
                projectionMatrix,
                createViewProjectionMatrix(projectionMatrix, viewMatrix)
        );
    }

    @Override
    public Matrix4f viewMatrix() {
        return new Matrix4f(this.viewMatrix);
    }

    @Override
    public Matrix4f projectionMatrix() {
        return new Matrix4f(this.projectionMatrix);
    }

    @Override
    public Matrix4f viewProjectionMatrix() {
        return new Matrix4f(this.viewProjectionMatrix);
    }

    private static Matrix4f createViewMatrix(PiCameraPose pose) {
        PiCameraPose validatedPose = Objects.requireNonNull(pose, "pose");
        var right = validatedPose.right();
        var up = validatedPose.up();
        var forward = validatedPose.forward();
        var position = validatedPose.position();

        double rightX = right.x;
        double rightY = right.y;
        double rightZ = right.z;
        double upX = up.x;
        double upY = up.y;
        double upZ = up.z;
        double forwardX = forward.x;
        double forwardY = forward.y;
        double forwardZ = forward.z;

        Matrix4f view = new Matrix4f();
        view.identity();
        view.m00((float) rightX);
        view.m10((float) rightY);
        view.m20((float) rightZ);
        view.m30((float) -position.dot(right));

        view.m01((float) upX);
        view.m11((float) upY);
        view.m21((float) upZ);
        view.m31((float) -position.dot(up));

        view.m02((float) -forwardX);
        view.m12((float) -forwardY);
        view.m22((float) -forwardZ);
        view.m32((float) position.dot(forward));
        return view;
    }

    private static Matrix4f createProjectionMatrix(PiCameraLens lens, PiViewport viewport) {
        PiCameraLens validatedLens = Objects.requireNonNull(lens, "lens");
        PiViewport validatedViewport = Objects.requireNonNull(viewport, "viewport");
        return new Matrix4f().setPerspective(
                (float) Math.toRadians(validatedLens.verticalFovDegrees()),
                (float) validatedViewport.aspectRatio(),
                (float) validatedLens.nearPlane(),
                (float) validatedLens.farPlane()
        );
    }

    private static Matrix4f createViewProjectionMatrix(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        return new Matrix4f(projectionMatrix).mul(viewMatrix);
    }
}
