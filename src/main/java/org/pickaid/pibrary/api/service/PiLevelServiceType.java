package org.pickaid.pibrary.api.service;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public interface PiLevelServiceType<T extends PiStateLevelService<?>> {
    ResourceLocation id();

    Class<T> serviceType();

    boolean isRegistered();

    Optional<T> find(ServerLevel level);

    default T get(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        return find(level).orElseThrow(() ->
                new IllegalStateException("Missing Pi level service " + serviceType().getName() + " on " + level.dimension().location()));
    }
}
