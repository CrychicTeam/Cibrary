package org.pickaid.pibrary.api.service;

import java.util.Objects;
import org.pickaid.pibrary.runtime.service.PiActiveLivingServiceRegistry;

public final class PiLivingServiceRegistrar<T extends PiStateLivingEntityService<?>> {
    private final Class<T> serviceType;

    public PiLivingServiceRegistrar(Class<T> serviceType) {
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
    }

    public PiLivingServiceType<T> register() {
        return PiActiveLivingServiceRegistry.register(serviceType);
    }
}
