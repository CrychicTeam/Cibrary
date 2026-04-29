package org.pickaid.pibrary.api.core;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default hierarchical implementation backing the global Pibrary scope graph.
 */
public final class PibraryScopes implements PibraryScope {
    private static final PibraryScopes ROOT = new PibraryScopes(null);

    private final PibraryScopes parent;
    private final Map<PibraryScopeKey<?>, Object> values = new ConcurrentHashMap<>();

    private PibraryScopes(PibraryScopes parent) {
        this.parent = parent;
    }

    /**
     * Returns the shared root scope used by static accessors.
     *
     * @return global root scope
     */
    public static PibraryScopes root() {
        return ROOT;
    }

    /**
     * Creates an isolated root scope with no parent.
     *
     * @return standalone scope
     */
    public static PibraryScopes create() {
        return new PibraryScopes(null);
    }

    /**
     * Registers a value on the shared root scope.
     *
     * @param key typed scope key
     * @param value value instance or {@code null} to remove
     * @param <T> value contract type
     */
    public static <T> void install(PibraryScopeKey<T> key, T value) {
        ROOT.register(key, value);
    }

    /**
     * Resolves a value from the shared root scope.
     *
     * @param key typed scope key
     * @param <T> value contract type
     * @return resolved value, if present
     */
    public static <T> Optional<T> findGlobal(PibraryScopeKey<T> key) {
        return ROOT.find(key);
    }

    /**
     * Resolves a value from the shared root scope or fails if absent.
     *
     * @param key typed scope key
     * @param <T> value contract type
     * @return resolved value
     */
    public static <T> T requireGlobal(PibraryScopeKey<T> key) {
        return ROOT.require(key);
    }

    @Override
    public <T> void register(PibraryScopeKey<T> key, T value) {
        if (value == null) {
            values.remove(key);
            return;
        }

        if (!key.type().isInstance(value)) {
            throw new IllegalArgumentException("Scoped value " + value + " is not a " + key.type().getName());
        }

        values.put(key, value);
    }

    @Override
    public <T> Optional<T> find(PibraryScopeKey<T> key) {
        Object value = values.get(key);
        if (value != null) {
            return Optional.of(key.type().cast(value));
        }
        return parent == null ? Optional.empty() : parent.find(key);
    }

    @Override
    public PibraryScopes child() {
        return new PibraryScopes(this);
    }
}
