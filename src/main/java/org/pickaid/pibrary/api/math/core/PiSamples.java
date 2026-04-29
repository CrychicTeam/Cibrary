package org.pickaid.pibrary.api.math.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Helpers for building evenly spaced sample points.
 *
 * <p>Use this class when you want "give me N evenly spaced positions" instead
 * of hand-writing loops. Typical cases are preview tables, lookup tables,
 * sampled curves, and stepping across a range at fixed density.</p>
 *
 * <p>All helpers include both ends of the interval. That means the first sample
 * is exactly at the start and the last sample is exactly at the end.</p>
 */
public final class PiSamples {
    private PiSamples() {
    }

    /**
     * Returns one normalized step from an evenly divided unit interval.
     *
     * <p>Think of {@code steps} as the total number of sample points, not the
     * number of gaps. For example, {@code steps = 3} produces the points
     * {@code 0.0}, {@code 0.5}, and {@code 1.0}. The valid index range is
     * therefore {@code 0} through {@code steps - 1}.</p>
     *
     * <p>Use this when you are writing your own sample loop and want the
     * spacing rule to match the rest of Pibrary math. A common case is "sample
     * this curve 16 times including both ends".</p>
     *
     * @param index the zero-based index of the requested sample point
     * @param steps the total number of sample points; must be at least
     *     {@code 2}
     * @return the normalized position for that sample point
     * @throws IllegalArgumentException if {@code steps} is less than {@code 2}
     * @throws IllegalArgumentException if {@code index} is outside the inclusive
     *     range {@code 0} to {@code steps - 1}
     */
    public static double normalizedStep(int index, int steps) {
        if (steps < 2) {
            throw new IllegalArgumentException("steps must be at least 2");
        }
        if (index < 0 || index >= steps) {
            throw new IllegalArgumentException("index must be between 0 and steps - 1");
        }
        return (double) index / (steps - 1);
    }

    /**
     * Creates an immutable list of evenly spaced values across a range.
     *
     * <p>The returned list always includes the exact minimum and maximum of the
     * supplied range. For example, sampling {@code [-2.0, 2.0]} with
     * {@code 3} samples returns {@code [-2.0, 0.0, 2.0]}.</p>
     *
     * <p>Use this when you need concrete values instead of normalized
     * positions. For example, if you want 5 preview heights from a jump arc or
     * 9 scan distances across a reach window, this method gives you the points
     * directly.</p>
     *
     * @param range the range to sample
     * @param samples the total number of sample points; must be at least
     *     {@code 2}
     * @return an immutable list of evenly spaced values
     * @throws NullPointerException if {@code range} is {@code null}
     * @throws IllegalArgumentException if {@code samples} is less than
     *     {@code 2}
     */
    public static List<Double> evenlySpaced(PiRange range, int samples) {
        if (samples < 2) {
            throw new IllegalArgumentException("samples must be at least 2");
        }
        List<Double> values = new ArrayList<>(samples);
        for (int index = 0; index < samples; index++) {
            values.add(range.min() + normalizedStep(index, samples) * range.span());
        }
        return List.copyOf(values);
    }
}
