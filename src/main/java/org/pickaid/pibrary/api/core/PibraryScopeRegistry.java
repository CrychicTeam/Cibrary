package org.pickaid.pibrary.api.core;

import java.util.Optional;

/**
 * Minimal typed scope registry used across Pibrary core subsystems.
 */
public interface PibraryScopeRegistry {
    /**
     * Registers or replaces a scoped value for the given key. Passing {@code null} removes it.
     *
     * @param key typed scope key
     * @param value value instance or {@code null} to remove
     * @param <T> value contract type
     */
    <T> void register(PibraryScopeKey<T> key, T value);

    /**
     * Resolves a value if present in the current registry or one of its parents.
     *
     * @param key typed scope key
     * @param <T> value contract type
     * @return the resolved value, if any
     */
    <T> Optional<T> find(PibraryScopeKey<T> key);

    /**
     * Creates a child registry. Implementations that do not support scoping may throw.
     *
     * @return child registry view
     */
    default PibraryScopeRegistry child() {
        throw new UnsupportedOperationException("This scope registry does not support scoped children");
    }

    /**
     * Resolves a value or fails if it is missing.
     *
     * @param key typed scope key
     * @param <T> value contract type
     * @return the resolved value
     */
    default <T> T require(PibraryScopeKey<T> key) {
        return find(key).orElseThrow(() -> new IllegalStateException("Missing scoped value: " + key.id()));
    }
}
