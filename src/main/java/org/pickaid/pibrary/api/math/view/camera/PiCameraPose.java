package org.pickaid.pibrary.api.math.view.camera;

import java.util.Objects;
import net.minecraft.world.phys.Vec3;

/**
 * One reusable camera orientation snapshot.
 *
 * <p>Use this when you already know where a camera is and which way it is
 * looking, and you want a stable basis for projection or camera-relative math.
 * Common cases are:</p>
 * <ul>
 *     <li>the normal gameplay camera for HUD indicators or debug overlays;</li>
 *     <li>a custom cinematic or entity-follow camera;</li>
 *     <li>any world-to-screen pipeline that needs forward, up, and right
 *     vectors that can be trusted.</li>
 * </ul>
 *
 * <p>The constructor normalizes and repairs the basis so callers can safely
 * rely on the stored vectors being unit length and mutually orthogonal.</p>
 *
 * @param position the camera position in world space
 * @param forward the viewing direction; must be non-zero
 * @param up the approximate upward direction; must be non-zero and not
 *     parallel to {@code forward}
 */
public record PiCameraPose(Vec3 position, Vec3 forward, Vec3 up) {
    /**
     * Creates a validated, normalized camera pose.
     *
     * <p>The constructor accepts raw direction vectors, then normalizes them
     * into a stable orthonormal basis. This means you can pass "good enough"
     * input vectors from a render hook or gameplay camera source, and the pose
     * will clean them into a reliable basis.</p>
     *
     * @param position the camera position
     * @param forward the raw viewing direction
     * @param up the raw upward direction
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code forward} or {@code up} is
     *     zero, or if they are parallel
     */
    public PiCameraPose {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(forward, "forward");
        Objects.requireNonNull(up, "up");
        if (forward.lengthSqr() == 0.0D || up.lengthSqr() == 0.0D) {
            throw new IllegalArgumentException("forward and up must be non-zero");
        }

        forward = forward.normalize();
        Vec3 provisionalUp = up.normalize();
        if (Math.abs(forward.dot(provisionalUp)) > 0.999D) {
            throw new IllegalArgumentException("forward and up must not be parallel");
        }

        Vec3 right = provisionalUp.cross(forward).normalize();
        up = forward.cross(right).normalize();
    }

    /**
     * Returns the camera's right vector derived from {@link #forward()} and
     * {@link #up()}.
     *
     * @return the normalized right-hand basis vector
     */
    public Vec3 right() {
        return this.up.cross(this.forward).normalize();
    }
}
