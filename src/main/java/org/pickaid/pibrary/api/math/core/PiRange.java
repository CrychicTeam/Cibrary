package org.pickaid.pibrary.api.math.core;

import java.util.Objects;

/**
 * One reusable numeric interval.
 *
 * <p>Use this class when one subsystem keeps talking about the same bounds
 * again and again. Typical examples are:</p>
 * <ul>
 *     <li>health {@code 0..20};</li>
 *     <li>stamina {@code 0..100};</li>
 *     <li>normalized progress {@code 0..1};</li>
 *     <li>a legal tuning window such as "allowed pitch is -45..45".</li>
 * </ul>
 *
 * <p>Once a {@code PiRange} exists, it is already validated, so later code can
 * clamp, normalize, and test containment without re-checking that the bounds
 * make sense.</p>
 *
 * <p>The range is inclusive at both ends for containment and clamping.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * PiRange staminaRange = new PiRange(0.0, 100.0);
 * double stamina01 = staminaRange.normalizeClamped(currentStamina);
 * }</pre>
 */
public final class PiRange {
    private final double min;
    private final double max;

    /**
     * Creates a validated range.
     *
     * <p>The lower bound must be strictly smaller than the upper bound. Equal
     * bounds are rejected because they would create a zero-width range, and
     * most operations in this class rely on the range having a real span.</p>
     *
     * @param min the inclusive lower bound
     * @param max the inclusive upper bound
     * @throws IllegalArgumentException if {@code min} is greater than or equal
     *     to {@code max}
     */
    public PiRange(double min, double max) {
        if (min >= max) {
            throw new IllegalArgumentException("min must be less than max");
        }
        this.min = min;
        this.max = max;
    }

    /**
     * Returns the inclusive lower bound.
     *
     * @return the smallest value considered inside this range
     */
    public double min() {
        return min;
    }

    /**
     * Returns the inclusive upper bound.
     *
     * @return the largest value considered inside this range
     */
    public double max() {
        return max;
    }

    /**
     * Tests whether a value is inside this range.
     *
     * <p>The check is inclusive, so the exact endpoints also return
     * {@code true}.</p>
     *
     * @param value the value to test
     * @return {@code true} if {@code value} is between {@link #min()} and
     *     {@link #max()}, inclusive
     */
    public boolean contains(double value) {
        return value >= min && value <= max;
    }

    /**
     * Restricts a value to this range.
     *
     * <p>Use this when the range itself is the important idea in your code. It
     * says "clamp to the stamina range" or "clamp to the legal pitch range"
     * more clearly than passing raw numbers around every time.</p>
     *
     * @param value the value to clamp
     * @return {@code value} if it is inside the range, otherwise the nearest
     *     bound
     */
    public double clamp(double value) {
        return PiScalars.clamp(value, min, max);
    }

    /**
     * Returns the numeric width of this range.
     *
     * <p>For a range from {@code -2.0} to {@code 6.0}, the span is
     * {@code 8.0}.</p>
     *
     * @return {@code max - min}
     */
    public double span() {
        return max - min;
    }

    /**
     * Returns the midpoint of this range.
     *
     * <p>Use this when the center itself is meaningful: neutral pitch,
     * midpoint difficulty, average legal value, or the anchor for a symmetric
     * expansion.</p>
     *
     * @return the numeric midpoint between {@link #min()} and {@link #max()}
     */
    public double center() {
        return (min + max) * 0.5D;
    }

    /**
     * Converts a value into this range's normalized coordinate system.
     *
     * <p>Use this when you need a range-relative answer such as "how far
     * through this stamina bar are we?" or "how far through this legal pitch
     * window are we?" This method does not clamp, so it also exposes underflow
     * and overflow relative to the range.</p>
     *
     * <p>Use this method when the out-of-range information matters. If you want
     * to force the result back into the normalized interval, use
     * {@link #normalizeClamped(double)} instead.</p>
     *
     * @param value the value to normalize
     * @return the normalized position of {@code value} relative to this range
     */
    public double normalize(double value) {
        return PiScalars.inverseLerp(min, max, value);
    }

    /**
     * Normalizes a value and then clamps the result to the unit interval.
     *
     * <p>This is the safest choice for UI bars, interpolation factors, fill
     * amounts, and similar cases where everything below the range should behave
     * like {@code 0.0} and everything above the range should behave like
     * {@code 1.0}.</p>
     *
     * @param value the value to normalize
     * @return a normalized result between {@code 0.0} and {@code 1.0},
     *     inclusive
     */
    public double normalizeClamped(double value) {
        return PiScalars.clamp(normalize(value), 0.0, 1.0);
    }

    /**
     * Rebuilds a value inside this range from a normalized unit.
     *
     * <p>Use this as the inverse of {@link #normalize(double)} when a caller
     * already thinks in {@code 0..1} but the destination system uses this
     * range's actual units.</p>
     *
     * @param unit the normalized position to sample
     * @return the value at that position inside this range
     */
    public double lerp(double unit) {
        return PiScalars.lerp(min, max, unit);
    }

    /**
     * Grows this range outward on both sides by the same amount.
     *
     * <p>Use this when one legal window needs a tolerance margin, for example
     * widening a selection band, aiming band, or validation window without
     * changing its center.</p>
     *
     * @param amount how much to subtract from {@link #min()} and add to
     *     {@link #max()}
     * @return a new, wider range
     * @throws IllegalArgumentException if {@code amount} is negative or not
     *     finite
     */
    public PiRange expand(double amount) {
        if (!Double.isFinite(amount) || amount < 0.0D) {
            throw new IllegalArgumentException("amount must be finite and >= 0");
        }
        return new PiRange(min - amount, max + amount);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PiRange range)) {
            return false;
        }
        return Double.compare(this.min, range.min) == 0 && Double.compare(this.max, range.max) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @Override
    public String toString() {
        return "PiRange[" + min + ", " + max + "]";
    }
}
