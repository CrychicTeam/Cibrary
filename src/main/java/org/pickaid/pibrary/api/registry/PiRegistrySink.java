package org.pickaid.pibrary.api.registry;

/**
 * Legacy internal request model kept only for bridge migration.
 * New public registry entry points live under {@code api.registrate.registry}.
 *
 * Sink receiving registry requests from contributors.
 */
public interface PiRegistrySink {
    /**
     * Registers one immutable registry request.
     *
     * @param request registry request
     * @param <T> registered value type
     */
    <T> void register(PiRegistryRequest<T> request);
}
