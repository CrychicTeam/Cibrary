package org.pickaid.pibrary.api.registry;

/**
 * Functional contributor that emits registration requests.
 */
@FunctionalInterface
public interface PiRegistryContributor {
    /**
     * Contributes registrations into the provided sink.
     *
     * @param sink registry sink
     */
    void contribute(PiRegistrySink sink);
}
