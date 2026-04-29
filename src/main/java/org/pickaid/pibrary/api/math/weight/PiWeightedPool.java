package org.pickaid.pibrary.api.math.weight;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;

/**
 * A reusable weighted choice table.
 *
 * <p>Use this class when you already know the available choices and want to
 * pick one of them repeatedly. Common use cases are:</p>
 * <ul>
 *     <li>a loot table for one chest or enemy drop roll;</li>
 *     <li>a weighted behavior chooser for one AI tick;</li>
 *     <li>a deterministic test fixture where you want to assert which choice a
 *     specific normalized input lands on.</li>
 * </ul>
 *
 * <p>The input to {@link #select(double)} is not "a weight". It is a normalized
 * position from {@code 0.0} to {@code 1.0} across the whole pool. That design
 * keeps the pool deterministic and testable: gameplay code can pass a random
 * unit double, while tests can pass a fixed value and assert the exact result.</p>
 *
 * @param <T> the type of value stored in the pool
 */
public final class PiWeightedPool<T> implements PiWeightedSelector<T> {
    private final List<PiWeightedEntry<T>> entries;
    private final double totalWeight;

    /**
     * Creates a pool from the provided entries.
     *
     * <p>The input array must not be {@code null}. Each entry must already be
     * valid, which means the entry value is non-null and the weight is a finite
     * positive number. Invalid entries fail fast before the pool is created.</p>
     *
     * <p>An empty call, {@code PiWeightedPool.of()}, is allowed and produces a
     * pool that always returns {@link Optional#empty()} from
     * {@link #select(double)}.</p>
     *
     * @param entries the weighted entries to store in the pool
     * @param <T> the value type
     * @return a new immutable pool
     * @throws NullPointerException if {@code entries} is {@code null}
     * @throws NullPointerException if any entry in {@code entries} is
     *     {@code null}
     * @throws IllegalArgumentException if any entry has an invalid weight
     */
    private PiWeightedPool(List<PiWeightedEntry<T>> entries) {
        this.entries = List.copyOf(entries);
        double sum = 0.0;
        for (PiWeightedEntry<T> entry : this.entries) {
            sum += entry.weight();
        }
        this.totalWeight = sum;
    }

    /**
     * Creates a pool from zero or more weighted entries.
     *
     * <p>Use this when one call site wants to declare the whole choice table in
     * place. This is the normal entry point for a small loot pool or weighted
     * behavior table. An empty call is valid and creates a pool that always
     * returns {@link Optional#empty()}.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * PiWeightedPool<String> pool = PiWeightedPool.of(
     *     PiWeightedEntry.of("common", 8.0),
     *     PiWeightedEntry.of("rare", 2.0)
     * );
     * }</pre>
     *
     * @param entries the entries to include in the pool
     * @param <T> the value type
     * @return a new weighted pool
     * @throws NullPointerException if {@code entries} is {@code null} or any
     *     entry is {@code null}
     * @throws IllegalArgumentException if any entry has an invalid weight
     */
    @SafeVarargs
    public static <T> PiWeightedPool<T> of(PiWeightedEntry<T>... entries) {
        Objects.requireNonNull(entries, "entries");
        return new PiWeightedPool<>(List.of(entries));
    }

    /**
     * Creates a Pi pool from a Mojang simple weighted list.
     *
     * <p>Use this when data or APIs already hand you a
     * {@link SimpleWeightedRandomList} but the rest of your code wants the Pi
     * helpers such as deterministic {@link #select(double)} calls or
     * double-based total weight inspection.</p>
     *
     * @param vanilla the vanilla weighted list to convert
     * @param <T> the value type
     * @return a Pi pool with the same values and integer weights
     * @throws NullPointerException if {@code vanilla} is {@code null}
     */
    public static <T> PiWeightedPool<T> fromVanilla(SimpleWeightedRandomList<T> vanilla) {
        Objects.requireNonNull(vanilla, "vanilla");
        return new PiWeightedPool<>(vanilla.unwrap().stream().map(PiWeightedEntry::fromVanilla).toList());
    }

    /**
     * Returns the sum of all entry weights in this pool.
     *
     * <p>Use this mainly for debugging, metrics, or when another algorithm
     * wants to reason about the pool's raw summed weight. Normal callers can
     * usually ignore it and just call {@link #select(double)}.</p>
     *
     * @return the total weight of the pool
     */
    public double totalWeight() {
        return totalWeight;
    }

    /**
     * Selects one value using a normalized unit value.
     *
     * <p>The {@code unit} value must be between {@code 0.0} and {@code 1.0}.
     * The method multiplies that unit by the pool's total weight, then walks
     * the entries in order until the cumulative weight covers the threshold.</p>
     *
     * <p>Example: with weights {@code 1.0}, {@code 3.0}, and {@code 6.0}, the
     * total is {@code 10.0}. A unit of {@code 0.20} becomes a threshold of
     * {@code 2.0}, which lands in the second entry.</p>
     *
     * <p>Use this as the final "pick one" call. If you have a random source,
     * feed it one unit double. If you are writing a test, feed it a known
     * number such as {@code 0.20} and assert the chosen value directly.</p>
     *
     * @param unit a normalized selection position between {@code 0.0} and
     *     {@code 1.0}, inclusive
     * @return the selected value, or {@link Optional#empty()} if this pool has
     *     no entries
     * @throws IllegalArgumentException if {@code unit} is not finite or is
     *     outside the inclusive range {@code 0.0} to {@code 1.0}
     */
    @Override
    public Optional<T> select(double unit) {
        if (!Double.isFinite(unit) || unit < 0.0 || unit > 1.0) {
            throw new IllegalArgumentException("unit must be finite and between 0 and 1");
        }
        if (entries.isEmpty()) {
            return Optional.empty();
        }

        double threshold = unit * totalWeight;
        double cumulative = 0.0;
        for (int index = 0; index < entries.size(); index++) {
            PiWeightedEntry<T> entry = entries.get(index);
            cumulative += entry.weight();
            if (threshold <= cumulative || index == entries.size() - 1) {
                return Optional.of(entry.value());
            }
        }
        return Optional.empty();
    }

    public Optional<T> select(RandomSource random) {
        Objects.requireNonNull(random, "random");
        if (entries.isEmpty()) {
            return Optional.empty();
        }
        return select(random.nextDouble());
    }

    /**
     * Converts this pool into Mojang's {@link SimpleWeightedRandomList}.
     *
     * <p>Use this when a downstream vanilla or Forge API expects Mojang's
     * weighted list type. Conversion is lossless only for whole-number weights.
     * Fractional Pi weights are valid inside Pi math, but they cannot be
     * expressed in vanilla's integer-weight format and will fail fast.</p>
     *
     * @return a vanilla weighted list containing the same values
     * @throws IllegalStateException if any entry weight is fractional or larger
     *     than {@link Integer#MAX_VALUE}
     */
    public SimpleWeightedRandomList<T> toVanilla() {
        if (entries.isEmpty()) {
            return SimpleWeightedRandomList.empty();
        }

        SimpleWeightedRandomList.Builder<T> builder = SimpleWeightedRandomList.builder();
        for (PiWeightedEntry<T> entry : entries) {
            builder.add(entry.value(), entry.toVanillaWeight());
        }
        return builder.build();
    }
}
