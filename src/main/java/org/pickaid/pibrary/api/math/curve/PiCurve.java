package org.pickaid.pibrary.api.math.curve;

/**
 * One scalar shaping function.
 *
 * <p>Use this when a straight line is not enough. Common use cases are:</p>
 * <ul>
 *     <li>charge-up bars that should start slow or end slow;</li>
 *     <li>damage or effect falloff over a distance or progress value;</li>
 *     <li>spawn scaling, animation timing, or easing for presentation values.</li>
 * </ul>
 *
 * <p>Most callers use a normalized input from {@code 0.0} to {@code 1.0}, but
 * the contract does not require that. A curve may also extrapolate when the
 * caller intentionally supplies values outside that interval.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * PiCurve chargeCurve = PiCurves.easeInQuad();
 * double shapedCharge = chargeCurve.sample(rawCharge01);
 * }</pre>
 */
@FunctionalInterface
public interface PiCurve {
    /**
     * Samples this curve at the given input value.
     *
     * <p>The exact meaning of {@code input} depends on the caller. In many
     * cases it is normalized progress, but it can also be distance, strength,
     * time, or any other scalar coordinate.</p>
     *
     * <p>This interface does not clamp automatically. Clamp before calling when
     * your use case demands a hard range.</p>
     *
     * @param input the scalar position to evaluate
     * @return the shaped output value for that input
     */
    double sample(double input);
}
