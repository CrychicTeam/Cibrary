package org.pickaid.pibrary.api.service;

import java.util.Objects;

/**
 * Registration handle for chunk services.
 *
 * @param <T> service type
 */
public final class PiChunkServiceRegistrar<T extends PiStateChunkService<?>> {
    private final Class<T> serviceType;

    public PiChunkServiceRegistrar(Class<T> serviceType) {
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
    }

    /**
     * Registers the chunk service type and returns its typed handle.
     *
     * @return registered chunk service type
     */
    public PiChunkServiceType<T> register() {
        throw new UnsupportedOperationException(
                "Chunk service registration for " + serviceType.getName() + " is deferred until Task 3");
    }
}
