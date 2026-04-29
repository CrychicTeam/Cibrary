package org.pickaid.pibrary.runtime.presentation;

import org.pickaid.pibrary.api.presentation.PiPresentationFactory;

/**
 * Cached projection slot that recomputes lazily when marked dirty.
 * The provided partial tick is only applied when recomputation happens.
 *
 * @param <V> projection value type
 */
final class PiPresentationEntry<V> {
    private boolean dirty = true;
    private V value;

    PiPresentationEntry() {
    }

    synchronized V resolve(float partialTick, PiPresentationFactory<V> factory) {
        if (dirty) {
            value = factory.create(partialTick);
            dirty = false;
        }
        return value;
    }

    synchronized void invalidate() {
        dirty = true;
        value = null;
    }
}
