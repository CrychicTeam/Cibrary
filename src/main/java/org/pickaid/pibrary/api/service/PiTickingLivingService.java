package org.pickaid.pibrary.api.service;

/**
 * Optional lifecycle hook for services that need per-tick updates.
 */
public interface PiTickingLivingService {
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
