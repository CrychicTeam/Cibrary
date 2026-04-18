package org.pickaid.pibrary.api.service;

import java.util.Objects;
import org.pickaid.pibrary.runtime.level.PiActiveLevelServiceRegistry;

public final class PiLevelServiceRegistrar<T extends PiStateLevelService<?>> {
    private final Class<T> serviceType;

    public PiLevelServiceRegistrar(Class<T> serviceType) {
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
    }

    public PiLevelServiceType<T> register() {
        return PiActiveLevelServiceRegistry.register(serviceType);
    }
}
