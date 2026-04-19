package org.pickaid.pibrary.api.registry;

/**
 * Legacy internal request model kept only for bridge migration.
 * New public registry entry points live under {@code api.registrate.registry}.
 *
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
