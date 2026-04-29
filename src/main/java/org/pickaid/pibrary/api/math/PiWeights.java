package org.pickaid.pibrary.api.math;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;
import java.util.function.ToDoubleFunction;

/**
 * Weighted selection helpers.
 *
 * <p>Use this for loot-like choices, effect variation, animation variant
 * selection, terrain template selection, or any other "pick one candidate by
 * weight" task. It deliberately does not own registries or datapacks; those
 * higher-level systems can feed already-resolved entries into this helper.</p>
 */
public final class PiWeights {
    private PiWeights() {
    }

    /**
     * Picks one entry from a weighted list.
     *
     * <p>Weights must be finite and non-negative. Entries with zero weight stay
     * in the list but are never selected unless every entry is zero, in which
     * case the result is empty.</p>
     *
     * @param entries candidate entries
     * @param weight weight extractor
     * @param random random source
     * @param <T> entry type
     * @return selected entry, or empty when total weight is zero
     */
    public static <T> Optional<T> choose(List<T> entries, ToDoubleFunction<? super T> weight, RandomGenerator random) {
        Objects.requireNonNull(entries, "entries");
        Objects.requireNonNull(weight, "weight");
        Objects.requireNonNull(random, "random");

        double total = total(entries, weight);
        if (total <= 0.0D) {
            return Optional.empty();
        }

        double cursor = random.nextDouble(total);
        for (T entry : entries) {
            double value = checkedWeight(weight.applyAsDouble(entry));
            if (value <= 0.0D) {
                continue;
            }
            cursor -= value;
            if (cursor < 0.0D) {
                return Optional.of(entry);
            }
        }

        return entries.stream()
                .filter(entry -> checkedWeight(weight.applyAsDouble(entry)) > 0.0D)
                .reduce((left, right) -> right);
    }

    /**
     * Computes the total weight.
     *
     * @param entries candidate entries
     * @param weight weight extractor
     * @param <T> entry type
     * @return sum of weights
     */
    public static <T> double total(List<T> entries, ToDoubleFunction<? super T> weight) {
        Objects.requireNonNull(entries, "entries");
        Objects.requireNonNull(weight, "weight");
        double total = 0.0D;
        for (T entry : entries) {
            total += checkedWeight(weight.applyAsDouble(entry));
        }
        return total;
    }

    /**
     * Converts one weight into a normalized share of the total.
     *
     * @param weight single weight
     * @param total total weight
     * @return normalized share, or zero when total is zero
     */
    public static double normalized(double weight, double total) {
        double checked = checkedWeight(weight);
        double checkedTotal = checkedWeight(total);
        if (checkedTotal <= 0.0D) {
            return 0.0D;
        }
        return checked / checkedTotal;
    }

    private static double checkedWeight(double weight) {
        if (!Double.isFinite(weight) || weight < 0.0D) {
            throw new IllegalArgumentException("weight must be finite and >= 0");
        }
        return weight;
    }
}
