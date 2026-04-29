package org.pickaid.pibrary.api.math.weight;

import java.util.Objects;
import net.minecraft.util.random.WeightedEntry;

/**
 * One choice inside a weighted pool.
 *
 * <p>Use this when one possible outcome should appear with some relative
 * frequency compared with the other outcomes in the same pool. Common cases
 * are:</p>
 * <ul>
 *     <li>loot choices such as common, rare, and legendary rewards;</li>
 *     <li>AI behavior choices such as attack, dodge, or retreat;</li>
 *     <li>ambient event choices such as small, medium, or large spawn waves.</li>
 * </ul>
 *
 * <p>The weight is not a standalone percentage. It only means something
 * relative to the other entries beside it. An entry with weight {@code 3.0} is
 * chosen three times as often as an entry with weight {@code 1.0} in the same
 * pool.</p>
 *
 * @param value the value stored in this entry and returned when it is selected
 * @param weight the positive relative weight for this entry
 * @param <T> the type of value stored in the entry
 */
public record PiWeightedEntry<T>(T value, double weight) {
    /**
     * Creates a weighted entry.
     *
     * <p>The value must not be {@code null}. The weight must be a finite number
     * greater than {@code 0.0}. Invalid inputs fail fast with an exception so
     * bad data does not reach the pool.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * PiWeightedEntry<String> rare = PiWeightedEntry.of("rare", 2.0);
     * }</pre>
     *
     * @param value the value to return when this entry is selected
     * @param weight the positive relative weight for this value
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code weight} is not finite or is
     *     less than or equal to {@code 0.0}
     */
    public PiWeightedEntry {
        Objects.requireNonNull(value, "value");
        if (!Double.isFinite(weight) || weight <= 0.0) {
            throw new IllegalArgumentException("weight must be finite and greater than 0");
        }
    }

    /**
     * Creates a weighted entry.
     *
     * <p>This is a convenience factory for callers that prefer a named creator
     * over the record constructor. It performs the same validation as the
     * canonical constructor.</p>
     *
     * @param value the value to store
     * @param weight the positive relative weight to assign
     * @param <T> the value type
     * @return a validated weighted entry
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code weight} is not finite or is
     *     less than or equal to {@code 0.0}
     */
    public static <T> PiWeightedEntry<T> of(T value, double weight) {
        return new PiWeightedEntry<>(value, weight);
    }

    /**
     * Creates one Pi entry from one vanilla weighted wrapper.
     *
     * <p>Use this when gameplay code already receives a Mojang weighted entry,
     * for example from loot-style data or another vanilla-shaped table, and
     * the rest of your logic wants to stay in the Pi math layer.</p>
     *
     * @param wrapper the vanilla weighted wrapper
     * @param <T> the wrapped value type
     * @return a Pi entry with the same data and numeric weight
     * @throws NullPointerException if {@code wrapper} is {@code null}
     */
    public static <T> PiWeightedEntry<T> fromVanilla(WeightedEntry.Wrapper<T> wrapper) {
        Objects.requireNonNull(wrapper, "wrapper");
        return new PiWeightedEntry<>(wrapper.getData(), wrapper.getWeight().asInt());
    }

    /**
     * Converts this entry into a Mojang weighted wrapper.
     *
     * <p>Vanilla weighted lists only accept whole-number weights. Because this
     * Pi entry allows doubles, conversion succeeds only when the weight is a
     * whole number between {@code 1} and {@link Integer#MAX_VALUE}.</p>
     *
     * @return a vanilla weighted wrapper with the same value and weight
     * @throws IllegalStateException if the weight cannot be represented as a
     *     vanilla integer weight
     */
    public WeightedEntry.Wrapper<T> toVanillaWrapper() {
        return WeightedEntry.wrap(value, toVanillaWeight());
    }

    /**
     * Returns this entry's weight as a vanilla-compatible integer.
     *
     * @return the weight as a positive whole number
     * @throws IllegalStateException if the weight has a fractional part or is
     *     larger than {@link Integer#MAX_VALUE}
     */
    public int toVanillaWeight() {
        if (weight > Integer.MAX_VALUE || weight != Math.rint(weight)) {
            throw new IllegalStateException("weight must be a whole number between 1 and Integer.MAX_VALUE for vanilla conversion");
        }
        return (int) weight;
    }
}
