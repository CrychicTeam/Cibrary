package org.pickaid.pibrary.api.presentation;

/**
 * Builds a projection value for a specific render tick.
 *
 * @param <V> projection type
 */
@FunctionalInterface
public interface PiPresentationFactory<V> {
    /**
     * Creates a presentation value.
     *
     * @param partialTick client partial tick
     * @return presentation value
     */
    V create(float partialTick);
}
