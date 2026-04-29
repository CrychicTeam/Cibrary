package org.pickaid.pibrary.api.config;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates one decoded config value.
 *
 * @param <T> value type
 */
@FunctionalInterface
public interface PiConfigValidator<T> {
    Optional<String> validate(T value);

    default PiConfigValidator<T> and(PiConfigValidator<T> other) {
        Objects.requireNonNull(other, "other");
        return value -> {
            Optional<String> first = validate(value);
            return first.isPresent() ? first : other.validate(value);
        };
    }

    static <T> PiConfigValidator<T> alwaysValid() {
        return value -> Optional.empty();
    }
}
