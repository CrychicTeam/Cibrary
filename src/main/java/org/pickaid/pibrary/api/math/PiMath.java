package org.pickaid.pibrary.api.math;

/**
 * Small numeric helpers used by gameplay, targeting, UI projection, animation,
 * and diagnostics code.
 *
 * <p>This class deliberately stays independent from Minecraft runtime objects.
 * Use it when the problem is pure numbers: interpolation, remapping, angle
 * wrapping, or moving one value toward another. Use {@link PiVectors} when the
 * problem is about {@code Vec3}, and use {@link PiAabbs} when the problem is
 * about {@code AABB}.</p>
 */
public final class PiMath {
    public static final double EPSILON = 1.0E-7D;

    private PiMath() {
    }

    /**
     * Throws when a value is NaN or infinite.
     *
     * <p>This is useful at public API boundaries. A NaN that enters targeting,
     * camera projection, or animation code usually poisons every later
     * calculation, so it is better to fail close to the caller.</p>
     *
     * @param value value to validate
     * @param name name used in the exception message
     * @return the original value
     */
    public static double requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
        return value;
    }

    /**
     * Clamps a value between {@code min} and {@code max}.
     *
     * @param value input value
     * @param min lower bound
     * @param max upper bound
     * @return value limited to the range
     */
    public static double clamp(double value, double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamps a value to the standard animation range {@code [0, 1]}.
     *
     * @param value input value
     * @return clamped value
     */
    public static double clamp01(double value) {
        return clamp(value, 0.0D, 1.0D);
    }

    /**
     * Linear interpolation.
     *
     * <p>{@code t = 0} returns {@code from}; {@code t = 1} returns
     * {@code to}. The method intentionally does not clamp {@code t}, so callers
     * may extrapolate when that is useful.</p>
     *
     * @param from start value
     * @param to end value
     * @param t interpolation factor
     * @return interpolated value
     */
    public static double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }

    /**
     * Inverse linear interpolation.
     *
     * <p>Example: {@code inverseLerp(10, 20, 15)} returns {@code 0.5}. This is
     * useful when turning a distance, cooldown, progress value, or health value
     * into a normalized animation parameter.</p>
     *
     * @param from range start
     * @param to range end
     * @param value value inside or outside the range
     * @return normalized position of value in the range
     */
    public static double inverseLerp(double from, double to, double value) {
        if (Math.abs(to - from) <= EPSILON) {
            throw new IllegalArgumentException("from and to must not be equal");
        }
        return (value - from) / (to - from);
    }

    /**
     * Remaps a value from one numeric range into another.
     *
     * @param inMin source range minimum
     * @param inMax source range maximum
     * @param outMin target range minimum
     * @param outMax target range maximum
     * @param value input value
     * @return value in the target range
     */
    public static double remap(double inMin, double inMax, double outMin, double outMax, double value) {
        return lerp(outMin, outMax, inverseLerp(inMin, inMax, value));
    }

    /**
     * Smooth interpolation curve with zero slope at both ends.
     *
     * <p>Use this for simple UI movement, camera blend weight, alpha fade, or
     * any other value that should not start and stop abruptly.</p>
     *
     * @param t raw progress
     * @return smoothed progress in {@code [0, 1]}
     */
    public static double smoothstep(double t) {
        double x = clamp01(t);
        return x * x * (3.0D - 2.0D * x);
    }

    /**
     * Smoother interpolation curve with zero first and second derivative at
     * both ends.
     *
     * @param t raw progress
     * @return smoothed progress in {@code [0, 1]}
     */
    public static double smootherstep(double t) {
        double x = clamp01(t);
        return x * x * x * (x * (x * 6.0D - 15.0D) + 10.0D);
    }

    /**
     * Wraps degrees into {@code [-180, 180)}.
     *
     * @param degrees raw degrees
     * @return wrapped degrees
     */
    public static double wrapDegrees(double degrees) {
        double wrapped = degrees % 360.0D;
        if (wrapped >= 180.0D) {
            wrapped -= 360.0D;
        }
        if (wrapped < -180.0D) {
            wrapped += 360.0D;
        }
        return wrapped;
    }

    /**
     * Returns the shortest signed angle from {@code fromDegrees} to
     * {@code toDegrees}.
     *
     * @param fromDegrees current angle
     * @param toDegrees target angle
     * @return signed shortest difference in degrees
     */
    public static double shortestAngleDegrees(double fromDegrees, double toDegrees) {
        return wrapDegrees(toDegrees - fromDegrees);
    }

    /**
     * Moves a value toward a target without overshooting.
     *
     * <p>This is useful for cooldown bars, simple camera recovery, UI spring
     * fallbacks, and gameplay meters that should move at a fixed rate.</p>
     *
     * @param current current value
     * @param target target value
     * @param maxStep largest absolute change
     * @return advanced value
     */
    public static double approach(double current, double target, double maxStep) {
        if (maxStep < 0.0D) {
            throw new IllegalArgumentException("maxStep must be >= 0");
        }
        double delta = target - current;
        if (Math.abs(delta) <= maxStep) {
            return target;
        }
        return current + Math.copySign(maxStep, delta);
    }
}
