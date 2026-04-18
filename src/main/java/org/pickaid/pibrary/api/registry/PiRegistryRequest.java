package org.pickaid.pibrary.api.registry;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Immutable registration request for a future registry helper runtime.
 *
 * @param registryKey target registry key
 * @param id registered entry id
 * @param factory factory creating the value
 * @param phase requested registration phase
 * @param <T> registered value type
 */
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
