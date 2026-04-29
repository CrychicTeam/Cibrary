package org.pickaid.pibrary.api.math;

import java.util.Comparator;
import java.util.Objects;
import net.minecraft.world.phys.Vec3;

/**
 * Helpers for Minecraft's {@link Vec3}.
 *
 * <p>Pibrary does not define its own replacement vector type. Gameplay code,
 * targeting code, and render code should continue to pass Minecraft's
 * {@code Vec3}. This class only fills common gaps: safe normalization,
 * projection, signed angles, and axis rotation.</p>
 */
public final class PiVectors {
    private PiVectors() {
    }

    /**
     * Checks whether every component is finite.
     *
     * @param vector vector to check
     * @return {@code true} when x, y, and z are not NaN or infinite
     */
    public static boolean isFinite(Vec3 vector) {
        Objects.requireNonNull(vector, "vector");
        return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
    }

    /**
     * Returns {@code true} when the vector length is close enough to zero that
     * direction-based math would be unstable.
     *
     * @param vector vector to check
     * @return whether the vector is near zero
     */
    public static boolean isNearZero(Vec3 vector) {
        Objects.requireNonNull(vector, "vector");
        return vector.lengthSqr() <= PiMath.EPSILON * PiMath.EPSILON;
    }

    /**
     * Normalizes a vector, returning a fallback when the input is zero or not
     * finite.
     *
     * <p>Use this at API boundaries. Raw {@code Vec3.normalize()} returns zero
     * for tiny vectors, which is fine internally but often not what a targeting
     * or camera API wants.</p>
     *
     * @param vector vector to normalize
     * @param fallback fallback direction
     * @return normalized vector or normalized fallback
     */
    public static Vec3 safeNormalize(Vec3 vector, Vec3 fallback) {
        Objects.requireNonNull(vector, "vector");
        Objects.requireNonNull(fallback, "fallback");
        if (!isFinite(vector) || isNearZero(vector)) {
            return fallback.normalize();
        }
        return vector.normalize();
    }

    /**
     * Projects {@code vector} onto {@code axis}.
     *
     * <p>If {@code axis} is not normalized, this method still works. If the axis
     * is zero, a zero vector is returned.</p>
     *
     * @param vector source vector
     * @param axis target axis
     * @return component of vector along axis
     */
    public static Vec3 projectOnto(Vec3 vector, Vec3 axis) {
        Objects.requireNonNull(vector, "vector");
        Objects.requireNonNull(axis, "axis");
        double axisLengthSqr = axis.lengthSqr();
        if (axisLengthSqr <= PiMath.EPSILON * PiMath.EPSILON) {
            return Vec3.ZERO;
        }
        return axis.scale(vector.dot(axis) / axisLengthSqr);
    }

    /**
     * Removes the component of {@code vector} that points along {@code axis}.
     *
     * @param vector source vector
     * @param axis rejected axis
     * @return vector component perpendicular to axis
     */
    public static Vec3 rejectFrom(Vec3 vector, Vec3 axis) {
        return vector.subtract(projectOnto(vector, axis));
    }

    /**
     * Computes the unsigned angle between two vectors.
     *
     * @param first first vector
     * @param second second vector
     * @return angle in radians, from {@code 0} to {@code PI}
     */
    public static double angleRadians(Vec3 first, Vec3 second) {
        Vec3 a = safeNormalize(first, Vec3.ZERO);
        Vec3 b = safeNormalize(second, Vec3.ZERO);
        if (isNearZero(a) || isNearZero(b)) {
            return 0.0D;
        }
        return Math.acos(PiMath.clamp(a.dot(b), -1.0D, 1.0D));
    }

    /**
     * Computes a signed angle around a plane normal.
     *
     * <p>Example: in a horizontal targeting plane, pass {@code Vec3(0, 1, 0)}
     * as {@code normal}. A positive result means {@code second} is to the
     * positive rotational side of {@code first} around that normal.</p>
     *
     * @param first first vector
     * @param second second vector
     * @param normal plane normal controlling the sign
     * @return signed angle in radians
     */
    public static double signedAngleRadians(Vec3 first, Vec3 second, Vec3 normal) {
        double unsigned = angleRadians(first, second);
        double sign = Math.signum(first.cross(second).dot(normal));
        return sign == 0.0D ? 0.0D : unsigned * sign;
    }

    /**
     * Rotates a vector around an axis using Rodrigues' rotation formula.
     *
     * @param vector vector to rotate
     * @param axis rotation axis
     * @param radians rotation amount in radians
     * @return rotated vector
     */
    public static Vec3 rotateAroundAxis(Vec3 vector, Vec3 axis, double radians) {
        Objects.requireNonNull(vector, "vector");
        Vec3 k = safeNormalize(axis, Vec3.ZERO);
        if (isNearZero(k)) {
            return vector;
        }
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return vector.scale(cos)
                .add(k.cross(vector).scale(sin))
                .add(k.scale(k.dot(vector) * (1.0D - cos)));
    }

    /**
     * Builds a comparator that orders vectors by squared distance to a target.
     *
     * @param target target position
     * @return nearest-first comparator
     */
    public static Comparator<Vec3> nearestTo(Vec3 target) {
        Objects.requireNonNull(target, "target");
        return Comparator.comparingDouble(vector -> vector.distanceToSqr(target));
    }
}
