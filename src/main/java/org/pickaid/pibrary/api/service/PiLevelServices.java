package org.pickaid.pibrary.api.service;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.runtime.level.PiActiveLevelServiceRegistry;

/**
 * Helpers for registering and resolving level-scoped services.
 */
public final class PiLevelServices {
    private PiLevelServices() {
    }

    public static <T extends PiStateLevelService<?>> PiLevelServiceRegistrar<T> host(Class<T> serviceType) {
        return new PiLevelServiceRegistrar<>(serviceType);
    }

    public static <T extends PiStateLevelService<?>> PiLevelServiceType<T> type(Class<T> serviceType) {
        return PiActiveLevelServiceRegistry.require(serviceType);
    }

    public static <T extends PiStateLevelService<?>> Optional<T> find(@Nullable ServerLevel level, Class<T> serviceType) {
        return level == null
                ? Optional.empty()
                : PiActiveLevelServiceRegistry.find(serviceType).flatMap(type -> type.find(level));
    }

    public static <T extends PiStateLevelService<?>> T require(ServerLevel level, Class<T> serviceType) {
        return type(serviceType).get(level);
    }
}
