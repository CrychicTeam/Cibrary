/**
 * Weighted selection helpers for picking one value from several choices.
 *
 * <p>Use this package when some outcomes should happen more often than others.
 * Each entry stores one value and one positive weight. The weight is not a
 * percentage by itself. It is only meaningful relative to the other weights in
 * the same pool.</p>
 *
 * <p>Example: weights {@code 2.0}, {@code 3.0}, and {@code 5.0} mean the third
 * entry is selected more often than the first or second entry, because it owns
 * the largest share of the total weight.</p>
 *
 * <p>The public API is intentionally small:</p>
 * <ul>
 *     <li>{@link org.pickaid.pibrary.api.math.weight.PiWeightedEntry} stores a
 *     value with one validated weight.</li>
 *     <li>{@link org.pickaid.pibrary.api.math.weight.PiWeightedPool} stores an
 *     immutable ordered list of entries and performs selection.</li>
 *     <li>{@link org.pickaid.pibrary.api.math.weight.PiWeightedSelector}
 *     exposes the common selection contract.</li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre>{@code
 * PiWeightedPool<String> pool = PiWeightedPool.of(
 *     PiWeightedEntry.of("common", 8.0),
 *     PiWeightedEntry.of("rare", 2.0)
 * );
 *
 * Optional<String> deterministicPick = pool.select(0.75);
 * }</pre>
 *
 * <p>The same API works for deterministic tests and for random selection. In a
 * game system, a caller can pass a unit double from a random source. In a
 * test, a caller can pass a fixed value and assert the exact result.</p>
 *
 * <p>This package also now bridges cleanly to Mojang's
 * {@code SimpleWeightedRandomList}: Pi code can import vanilla weighted data,
 * work on it with deterministic unit-based selection, and export it back when
 * a downstream vanilla API expects the original type.</p>
 */
@org.jetbrains.annotations.ApiStatus.Experimental
package org.pickaid.pibrary.api.math.weight;
