package org.pickaid.pibrary.api.math.weight;

import java.util.Optional;

/**
 * A simple contract for choosing one value from a weighted collection.
 *
 * <p>Use this when you have several possible outcomes and their relative
 * likelihood matters. Common cases are:</p>
 * <ul>
 *     <li>choosing one loot tier from weighted entries;</li>
 *     <li>choosing one mob trait or behavior variant;</li>
 *     <li>choosing one structure or effect flavor for a generated result.</li>
 * </ul>
 *
 * <p>Implementations take a normalized {@code unit} value and map it into the
 * pool of weighted entries. The result is an {@link Optional}: empty when the
 * selector has no entries, or present with the chosen value when selection
 * succeeds.</p>
 *
 * <p>The key design point is determinism. A random caller can pass a random
 * unit double. A test or replay system can pass a fixed value such as
 * {@code 0.20} and expect the same answer every time.</p>
 *
 * @param <T> the type returned by the selector
 */
public interface PiWeightedSelector<T> {
    /**
     * Selects one value using a normalized unit value.
     *
     * <p>The {@code unit} argument is not a weight and not necessarily a raw
     * RNG output. It is a normalized position between {@code 0.0} and
     * {@code 1.0}. In gameplay code you will often pass a random double in that
     * range. In tests, data replay, or deterministic generation, you can pass a
     * fixed number and get a stable result.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * Optional<String> pick = selector.select(0.25);
     * }</pre>
     *
     * @param unit a normalized selection value between {@code 0.0} and
     *     {@code 1.0}, inclusive
     * @return the selected value, or {@link Optional#empty()} if no entries are
     *     available
     * @throws IllegalArgumentException if {@code unit} is not finite or is
     *     outside the inclusive range {@code 0.0} to {@code 1.0}
     */
    Optional<T> select(double unit);
}
