package org.pickaid.pibrary.runtime.presentation;

import java.util.Objects;
import org.pickaid.pibrary.api.presentation.PiPresentationFactory;
import org.pickaid.pibrary.api.presentation.PiPresentationKey;

/**
 * Immutable pairing of a presentation key and factory.
 *
 * @param key presentation key
 * @param factory presentation factory
 * @param <V> projection value type
 */
public record PiPresentationRegistration<V>(
        PiPresentationKey<V> key,
        PiPresentationFactory<V> factory
) {
    public PiPresentationRegistration {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(factory, "factory");
    }
}
