package org.pickaid.pibrary.api.service;

import java.util.Objects;
import org.pickaid.pibrary.runtime.chunk.PiActiveChunkServiceRegistry;

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
        return PiActiveChunkServiceRegistry.register(serviceType);
    }
}
