package org.pickaid.pibrary.api.math;

/**
 * One-dimensional curve used by animation, weighting, camera blend, UI motion,
 * and gameplay falloff code.
 *
 * <p>The input is normally progress in {@code [0, 1]}. Curves are still allowed
 * to accept values outside that range when a caller intentionally wants
 * extrapolation. Use {@link #sampleClamped(double)} for the common safe path.</p>
 */
@FunctionalInterface
public interface PiCurve {
    /**
     * Samples the curve.
     *
     * @param t input progress
     * @return curve value
     */
    double sample(double t);

    /**
     * Samples the curve after clamping the input to {@code [0, 1]}.
     *
     * @param t raw progress
     * @return curve value
     */
    default double sampleClamped(double t) {
        return sample(PiMath.clamp01(t));
    }
}
