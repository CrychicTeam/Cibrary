package org.pickaid.pibrary.api.math;

import java.util.Objects;
import net.minecraft.world.phys.Vec3;

/**
 * A lightweight orthonormal basis built from Minecraft {@link Vec3} axes.
 *
 * <p>This is not a replacement vector type. It is a convenience wrapper for the
 * common "forward/up/right" frame used by targeting cones, area previews,
 * camera-facing markers, and projectile spread.</p>
 *
 * @param forward local forward axis
 * @param up local up axis
 * @param right local right axis
 */
public record PiOrientation(Vec3 forward, Vec3 up, Vec3 right) {
    public PiOrientation {
        Objects.requireNonNull(forward, "forward");
        Objects.requireNonNull(up, "up");
        Objects.requireNonNull(right, "right");
    }

    /**
     * Builds an orientation from forward and world-up hints.
     *
     * @param forward forward direction
     * @param upHint preferred up direction
     * @return stable orientation
     */
    public static PiOrientation of(Vec3 forward, Vec3 upHint) {
        Vec3 f = PiVectors.safeNormalize(forward, new Vec3(0.0D, 0.0D, 1.0D));
        Vec3 projectedUp = PiVectors.rejectFrom(upHint, f);
        Vec3 up = PiVectors.safeNormalize(projectedUp, fallbackUpFor(f));
        Vec3 right = up.cross(f).normalize();
        return new PiOrientation(f, up, right);
    }

    /**
     * Builds an orientation from forward and the normal Minecraft world-up
     * direction.
     *
     * @param forward forward direction
     * @return stable orientation
     */
    public static PiOrientation fromForward(Vec3 forward) {
        return of(forward, new Vec3(0.0D, 1.0D, 0.0D));
    }

    /**
     * Converts local coordinates into a world-space offset.
     *
     * <p>Example: {@code offset(0, 0, 4)} means "four blocks forward";
     * {@code offset(1, 0, 0)} means "one block right".</p>
     *
     * @param rightAmount amount along local right
     * @param upAmount amount along local up
     * @param forwardAmount amount along local forward
     * @return world-space offset
     */
    public Vec3 offset(double rightAmount, double upAmount, double forwardAmount) {
        return right.scale(rightAmount).add(up.scale(upAmount)).add(forward.scale(forwardAmount));
    }

    /**
     * Rotates the forward vector horizontally around the local up axis.
     *
     * @param radians rotation amount
     * @return new orientation
     */
    public PiOrientation yaw(double radians) {
        return of(PiVectors.rotateAroundAxis(forward, up, radians), up);
    }

    /**
     * Rotates the forward vector vertically around the local right axis.
     *
     * @param radians rotation amount
     * @return new orientation
     */
    public PiOrientation pitch(double radians) {
        Vec3 newForward = PiVectors.rotateAroundAxis(forward, right, radians);
        Vec3 newUp = PiVectors.rotateAroundAxis(up, right, radians);
        return of(newForward, newUp);
    }

    private static Vec3 fallbackUpFor(Vec3 forward) {
        double vertical = Math.abs(forward.dot(new Vec3(0.0D, 1.0D, 0.0D)));
        return vertical > 0.99D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
    }
}
