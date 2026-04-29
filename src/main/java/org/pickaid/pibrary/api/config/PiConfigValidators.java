package org.pickaid.pibrary.api.config;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Common validators for gameplay config values.
 */
public final class PiConfigValidators {
    private PiConfigValidators() {
    }

    public static <T> PiConfigValidator<T> alwaysValid() {
        return PiConfigValidator.alwaysValid();
    }

    public static <T> PiConfigValidator<T> notNull(String message) {
        return value -> value == null ? Optional.of(requireMessage(message)) : Optional.empty();
    }

    public static PiConfigValidator<String> notBlank(String message) {
        return value -> value == null || value.isBlank() ? Optional.of(requireMessage(message)) : Optional.empty();
    }

    public static PiConfigValidator<Integer> intRange(int minInclusive, int maxInclusive) {
        if (minInclusive > maxInclusive) {
            throw new IllegalArgumentException("minInclusive must be <= maxInclusive");
        }
        return value -> {
            if (value == null || value < minInclusive || value > maxInclusive) {
                return Optional.of("must be inside [" + minInclusive + ", " + maxInclusive + "]");
            }
            return Optional.empty();
        };
    }

    public static PiConfigValidator<Double> doubleRange(double minInclusive, double maxInclusive) {
        if (!Double.isFinite(minInclusive) || !Double.isFinite(maxInclusive) || minInclusive > maxInclusive) {
            throw new IllegalArgumentException("double range bounds must be finite and ordered");
        }
        return value -> {
            if (value == null || !Double.isFinite(value) || value < minInclusive || value > maxInclusive) {
                return Optional.of("must be finite and inside [" + minInclusive + ", " + maxInclusive + "]");
            }
            return Optional.empty();
        };
    }

    @SafeVarargs
    public static <T> PiConfigValidator<T> all(PiConfigValidator<T>... validators) {
        Objects.requireNonNull(validators, "validators");
        return all(List.of(validators));
    }

    public static <T> PiConfigValidator<T> all(List<PiConfigValidator<T>> validators) {
        Objects.requireNonNull(validators, "validators");
        PiConfigValidator<T> result = PiConfigValidator.alwaysValid();
        for (PiConfigValidator<T> validator : validators) {
            result = result.and(Objects.requireNonNull(validator, "validator"));
        }
        return result;
    }

    private static String requireMessage(String message) {
        String cleanMessage = Objects.requireNonNull(message, "message").trim();
        if (cleanMessage.isEmpty()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        return cleanMessage;
    }
}
