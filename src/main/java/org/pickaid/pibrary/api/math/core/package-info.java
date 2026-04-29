/**
 * Small, pure number helpers for scalar math.
 *
 * <p>Use this package when your problem is still "just numbers". A value has
 * to stay inside a limit, two doubles need fuzzy comparison, a number must be
 * remapped into another range, or a range needs evenly spaced sample points.
 * Those are the jobs of {@code core}.</p>
 *
 * <p>The package is intentionally small and easy to compose:</p>
 * <ul>
 *     <li>{@link org.pickaid.pibrary.api.math.core.PiScalars} contains static
 *     helpers such as clamp, inverse lerp, and epsilon checks.</li>
 *     <li>{@link org.pickaid.pibrary.api.math.core.PiRange} represents one
 *     validated inclusive range and exposes common operations on it.</li>
 *     <li>{@link org.pickaid.pibrary.api.math.core.PiSamples} generates
 *     normalized steps and evenly spaced numeric samples.</li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre>{@code
 * PiRange difficulty = new PiRange(0.0, 100.0);
 * double normalized = difficulty.normalizeClamped(65.0);
 * List<Double> checkpoints = PiSamples.evenlySpaced(difficulty, 5);
 * }</pre>
 */
@org.jetbrains.annotations.ApiStatus.Experimental
package org.pickaid.pibrary.api.math.core;
