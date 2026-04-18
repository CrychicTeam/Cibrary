package org.pickaid.pibrary.api.core;

import java.util.Optional;

/**
 * Minimal typed service registry used across Pibrary core subsystems.
 */
public interface PibraryServiceRegistry {
    /**
     * Registers or replaces a service for the given key. Passing {@code null} removes it.
     *
     * @param key typed service key
     * @param service service instance or {@code null} to remove
     * @param <T> service contract type
     */
    <T> void register(PibraryServiceKey<T> key, T service);

    /**
     * Resolves a service if present in the current registry or one of its parents.
     *
     * @param key typed service key
     * @param <T> service contract type
     * @return the resolved service, if any
     */
    <T> Optional<T> find(PibraryServiceKey<T> key);

    /**
     * Creates a child registry. Implementations that do not support scoping may throw.
     *
     * @return child registry view
     */
    default PibraryServiceRegistry child() {
        throw new UnsupportedOperationException("This service registry does not support scoped children");
    }

    /**
     * Resolves a service or fails if it is missing.
     *
     * @param key typed service key
     * @param <T> service contract type
     * @return the resolved service
     */
    default <T> T require(PibraryServiceKey<T> key) {
        return find(key).orElseThrow(() -> new IllegalStateException("Missing service: " + key.id()));
    }
}
