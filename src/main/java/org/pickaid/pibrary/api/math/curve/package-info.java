/**
 * Scalar curve helpers for shaping one numeric value into another.
 *
 * <p>Use this package when a plain linear value is not expressive enough. A
 * curve can accelerate, decelerate, smooth, or split a value into several
 * phases. Common uses include damage falloff, UI timing, animation-like number
 * shaping, and any system where a normalized input should feel different from a
 * straight line.</p>
 *
 * <p>The package starts with four small building blocks:</p>
 * <ul>
 *     <li>{@link org.pickaid.pibrary.api.math.curve.PiCurve} is the basic
 *     contract.</li>
 *     <li>{@link org.pickaid.pibrary.api.math.curve.PiCurves} provides common
 *     built-in shapes.</li>
 *     <li>{@link org.pickaid.pibrary.api.math.curve.PiPiecewiseCurve} combines
 *     several ranges into one curve.</li>
 *     <li>{@link org.pickaid.pibrary.api.math.curve.PiCurveSamples} turns a
 *     curve into a list of sample values.</li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre>{@code
 * PiCurve chargeCurve = PiCurves.easeOutQuad();
 * double shapedCharge = chargeCurve.sample(0.75);
 *
 * List<Double> preview = PiCurveSamples.sample(PiCurves.smoothstep(), 5);
 * }</pre>
 */
@org.jetbrains.annotations.ApiStatus.Experimental
package org.pickaid.pibrary.api.math.curve;
