package org.pickaid.pibrary.api.presentation;

import java.util.Objects;

/**
 * Unique key for one projection contract.
 *
 * @param surface projection surface
 * @param type projection type
 * @param scope replication scope
 * @param <V> projection value type
 */
public record PiPresentationKey<V>(
        PiPresentationSurface surface,
        Class<V> type,
        PiPresentationScope scope
) {
    public PiPresentationKey {
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(scope, "scope");
    }
}
