package org.pickaid.pibrary.api.math.core;

/**
 * Static helpers for the first layer of math problems: plain numbers.
 *
 * <p>Open this class first when your problem is still "I have a double and I
 * need to keep it legal, normalize it, remap it, or compare it safely." This
 * is the class for:</p>
 * <ul>
 *     <li>health bars, mana bars, cooldown progress, and charge progress;</li>
 *     <li>difficulty scaling such as "map level 1..50 into damage 0.8..2.5";</li>
 *     <li>tolerant floating-point checks such as "treat this as zero" or
 *     "treat these two values as equal enough".</li>
 * </ul>
 *
 * <p>If the job already talks about boxes, directions, projection, or camera
 * state, this class is no longer the right entry point. In that case move to
 * {@code geometry} or {@code view} instead.</p>
 *
 * <p>Typical flow:</p>
 * <pre>{@code
 * double health01 = PiScalars.inverseLerp(0.0, 20.0, playerHealth);
 * double barWidth = PiScalars.remap(health01, 0.0, 1.0, 0.0, 182.0);
 * double clampedBarWidth = PiScalars.clamp(barWidth, 0.0, 182.0);
 * }</pre>
 */
public final class PiScalars {
    private PiScalars() {
    }

    /**
     * Restricts a value to an inclusive range.
     *
     * <p>If {@code value} is smaller than {@code min}, this method returns
     * {@code min}. If {@code value} is larger than {@code max}, it returns
     * {@code max}. Otherwise it returns the original value unchanged.</p>
     *
     * <p>Use this when a value is allowed to overshoot during calculation but
     * must be forced back into a legal range before you store or render it. A
     * common example is charge progress, UI alpha, or an interpolation factor
     * that must stay inside {@code 0.0..1.0}.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * double clampedCharge = PiScalars.clamp(rawCharge, 0.0, 1.0);
     * }</pre>
     *
     * @param value the value to limit
     * @param min the inclusive lower bound
     * @param max the inclusive upper bound
     * @return {@code value} if it is already inside the range, otherwise the
     *     nearest bound
     * @throws IllegalArgumentException if {@code min} is greater than
     *     {@code max}
     */
    public static double clamp(double value, double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    /**
     * Restricts a value to the unit interval.
     *
     * <p>Use this for progress, alpha, fill amount, and other values that
     * should never leave {@code 0.0..1.0}. It is just a named shortcut for the
     * most common clamp range in game code.</p>
     *
     * @param value the value to clamp
     * @return {@code value} forced into the inclusive range {@code 0.0..1.0}
     */
    public static double clamp01(double value) {
        return clamp(value, 0.0D, 1.0D);
    }

    /**
     * Linearly interpolates between two values.
     *
     * <p>Use this when a normalized progress value should blend between a start
     * and end number. Typical cases are bar widths, damage scaling, and
     * time-based movement of plain doubles.</p>
     *
     * @param start the value at {@code delta == 0.0}
     * @param end the value at {@code delta == 1.0}
     * @param delta the interpolation position
     * @return the blended value
     */
    public static double lerp(double start, double end, double delta) {
        return start + (end - start) * delta;
    }

    /**
     * Converts a value inside a line segment into a normalized position.
     *
     * <p>The result answers the question "how far is {@code value} from
     * {@code start} to {@code end}?" A result of {@code 0.0} means exactly at
     * {@code start}. A result of {@code 1.0} means exactly at {@code end}. A
     * result of {@code 0.5} means halfway between them.</p>
     *
     * <p>Use this when the original unit is not what you want to reason about.
     * For example, health might be stored as {@code 0..20}, but your HUD logic
     * wants {@code 0..1}. The method does not clamp, so it also tells you when
     * the value has gone below the start or above the end.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * double normalized = PiScalars.inverseLerp(10.0, 18.0, 12.0);
     * // normalized == 0.25
     * }</pre>
     *
     * @param start the point treated as normalized position {@code 0.0}
     * @param end the point treated as normalized position {@code 1.0}
     * @param value the value to convert
     * @return the normalized position of {@code value} on the line from
     *     {@code start} to {@code end}
     * @throws IllegalArgumentException if {@code start} and {@code end} are
     *     equal, because the segment would have zero length
     */
    public static double inverseLerp(double start, double end, double value) {
        if (start == end) {
            throw new IllegalArgumentException("start and end must differ");
        }
        return (value - start) / (end - start);
    }

    /**
     * Converts a value from one numeric range into another numeric range.
     *
     * <p>This method first normalizes {@code value} inside the source interval
     * {@code [fromMin, fromMax]}, then rebuilds that same relative position
     * inside the target interval {@code [toMin, toMax]}.</p>
     *
     * <p>Use this when two systems use different scales. For example, a level
     * system might work in {@code 1..50} while a damage multiplier works in
     * {@code 0.8..2.5}. The result is not clamped; if the source value goes
     * outside its source interval, the target value can also go outside the
     * target interval.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * double damageScale = PiScalars.remap(playerLevel, 1.0, 50.0, 0.8, 2.5);
     * }</pre>
     *
     * @param value the source value to remap
     * @param fromMin the start of the source interval
     * @param fromMax the end of the source interval
     * @param toMin the start of the target interval
     * @param toMax the end of the target interval
     * @return the remapped value in the target interval
     * @throws IllegalArgumentException if {@code fromMin} and {@code fromMax}
     *     are equal, because the source interval would have zero length
     */
    public static double remap(double value, double fromMin, double fromMax, double toMin, double toMax) {
        return toMin + inverseLerp(fromMin, fromMax, value) * (toMax - toMin);
    }

    /**
     * Moves one value toward a target by at most a fixed step.
     *
     * <p>Use this when a number should settle smoothly instead of snapping:
     * recoil recovery, UI smoothing, aim drift correction, or cooldown meters
     * that catch up gradually. If the remaining gap is smaller than
     * {@code maxDelta}, the method lands exactly on the target.</p>
     *
     * @param current the current value
     * @param target the value to move toward
     * @param maxDelta the maximum movement allowed in this call; must be finite
     *     and non-negative
     * @return the moved value, never overshooting {@code target}
     * @throws IllegalArgumentException if {@code maxDelta} is negative or not
     *     finite
     */
    public static double approach(double current, double target, double maxDelta) {
        if (!Double.isFinite(maxDelta) || maxDelta < 0.0D) {
            throw new IllegalArgumentException("maxDelta must be finite and >= 0");
        }
        double delta = target - current;
        if (Math.abs(delta) <= maxDelta) {
            return target;
        }
        return current + Math.copySign(maxDelta, delta);
    }

    /**
     * Computes a modulo result that always stays in the positive cycle.
     *
     * <p>Normal Java remainder can return negative values. This helper is for
     * angle wrapping, looped timelines, or repeating indexes where callers want
     * the result to stay inside {@code 0..modulus} instead of flipping
     * negative.</p>
     *
     * @param value the value to wrap
     * @param modulus the positive cycle size
     * @return a value in the half-open interval {@code [0, modulus)}
     * @throws IllegalArgumentException if {@code modulus} is not finite or not
     *     strictly positive
     */
    public static double positiveModulo(double value, double modulus) {
        if (!Double.isFinite(modulus) || modulus <= 0.0D) {
            throw new IllegalArgumentException("modulus must be finite and > 0");
        }
        double result = value % modulus;
        return result >= 0.0D ? result : result + modulus;
    }

    /**
     * Wraps a value into a custom numeric interval.
     *
     * <p>Use this when a value loops inside a range that is not necessarily
     * {@code 0..1}: yaw angles in {@code -180..180}, repeating timers in
     * {@code 0..20}, or cyclic phase windows in any other interval.</p>
     *
     * @param value the value to wrap
     * @param min the inclusive start of the interval
     * @param max the exclusive end of the interval
     * @return {@code value} wrapped into the half-open interval
     *     {@code [min, max)}
     * @throws IllegalArgumentException if {@code max <= min} or either bound is
     *     not finite
     */
    public static double wrap(double value, double min, double max) {
        if (!Double.isFinite(min) || !Double.isFinite(max) || max <= min) {
            throw new IllegalArgumentException("max must be finite and > min");
        }
        return min + positiveModulo(value - min, max - min);
    }

    /**
     * Tests whether a value is close enough to zero.
     *
     * <p>Use this when exact zero is too strict. Floating-point math often
     * leaves tiny leftovers such as {@code 0.0000001}. This helper lets you
     * treat those leftovers as zero when that is the practical intent.</p>
     *
     * @param value the value to test
     * @param epsilon the allowed distance from zero; must be zero or positive
     * @return {@code true} if {@code value} is between {@code -epsilon} and
     *     {@code epsilon}, inclusive
     * @throws IllegalArgumentException if {@code epsilon} is negative
     */
    public static boolean nearZero(double value, double epsilon) {
        if (epsilon < 0.0) {
            throw new IllegalArgumentException("epsilon must be non-negative");
        }
        return Math.abs(value) <= epsilon;
    }

    /**
     * Tests whether two doubles are close enough to treat as equal.
     *
     * <p>Use this when the values came from calculation instead of being
     * hand-written constants. For example, animation progress, movement
     * interpolation, and geometric math often need "close enough" equality
     * instead of exact bit-for-bit equality.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * if (PiScalars.epsilonEquals(progress, 1.0, 0.0001)) {
     *     finishAnimation();
     * }
     * }</pre>
     *
     * @param left the first value
     * @param right the second value
     * @param epsilon the maximum allowed absolute difference; must be zero or
     *     positive
     * @return {@code true} if the absolute difference is at most
     *     {@code epsilon}
     * @throws IllegalArgumentException if {@code epsilon} is negative
     */
    public static boolean epsilonEquals(double left, double right, double epsilon) {
        if (epsilon < 0.0) {
            throw new IllegalArgumentException("epsilon must be non-negative");
        }
        return Math.abs(left - right) <= epsilon;
    }
}
