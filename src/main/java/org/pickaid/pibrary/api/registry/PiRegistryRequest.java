package org.pickaid.pibrary.api.registry;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record PiRegistryRequest<T>(
        ResourceKey<? extends Registry<T>> registryKey,
        ResourceLocation id,
        Supplier<? extends T> factory,
        PiRegistryPhase phase
) {
    public PiRegistryRequest {
        Objects.requireNonNull(registryKey, "registryKey");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(factory, "factory");
        Objects.requireNonNull(phase, "phase");
    }
}
