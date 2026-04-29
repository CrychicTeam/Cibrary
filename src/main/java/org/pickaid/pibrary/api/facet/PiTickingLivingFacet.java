package org.pickaid.pibrary.api.facet;

/**
 * Optional lifecycle hook for facets that need per-tick updates.
 */
public interface PiTickingLivingFacet {
    /**
     * Called each server tick.
     */
    void serverTick();

    /**
     * Called each client tick.
     */
    default void clientTick() {
    }
}
