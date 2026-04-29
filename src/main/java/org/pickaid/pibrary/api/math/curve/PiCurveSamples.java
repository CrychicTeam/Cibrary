package org.pickaid.pibrary.api.math.curve;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.pickaid.pibrary.api.math.core.PiSamples;

/**
 * Helpers for turning curves into concrete sample tables.
 *
 * <p>Use this class when you do not want to evaluate a curve ad hoc every
 * time, or when you need to inspect the shape directly. Common cases are:</p>
 * <ul>
 *     <li>building a preview list for a config screen or debug overlay;</li>
 *     <li>precomputing a few checkpoints for charge tiers or staged effects;</li>
 *     <li>writing tests that assert the rough shape of a curve at known
 *     positions.</li>
 * </ul>
 */
public final class PiCurveSamples {
    private PiCurveSamples() {
    }

    /**
     * Samples a curve at evenly spaced normalized positions.
     *
     * <p>The returned list always includes the start and end of the usual unit
     * interval. With {@code steps = 3}, the sampled inputs are {@code 0.0},
     * {@code 0.5}, and {@code 1.0}. That makes the method useful for "show me
     * the shape" tasks such as UI previews and quick balance checks.</p>
     *
     * @param curve the curve to sample
     * @param steps the total number of sample points; must be at least
     *     {@code 2}
     * @return an immutable list of sampled curve outputs
     * @throws NullPointerException if {@code curve} is {@code null}
     * @throws IllegalArgumentException if {@code steps} is less than
     *     {@code 2}
     */
    public static List<Double> sample(PiCurve curve, int steps) {
        Objects.requireNonNull(curve, "curve");

        ArrayList<Double> values = new ArrayList<>(steps);
        for (int index = 0; index < steps; index++) {
            values.add(curve.sample(PiSamples.normalizedStep(index, steps)));
        }
        return List.copyOf(values);
    }
}
