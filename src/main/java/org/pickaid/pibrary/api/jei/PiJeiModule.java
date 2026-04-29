package org.pickaid.pibrary.api.jei;

/**
 * Functional module that contributes JEI-neutral specs.
 */
@FunctionalInterface
public interface PiJeiModule {
    /**
     * Contributes specs into the provided bridge.
     *
     * @param bridge JEI-neutral bridge
     */
    void contribute(PiJeiBridge bridge);
}
