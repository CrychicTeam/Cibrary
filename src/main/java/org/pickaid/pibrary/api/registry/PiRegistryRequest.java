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
 * @param group report group name
 * @param <T> registered value type
 */
public record PiRegistryRequest<T>(
        ResourceKey<? extends Registry<T>> registryKey,
        ResourceLocation id,
        Supplier<? extends T> factory,
        PiRegistryPhase phase,
        String group
) {
    public static final String DEFAULT_GROUP = "default";

    public PiRegistryRequest(
            ResourceKey<? extends Registry<T>> registryKey,
            ResourceLocation id,
            Supplier<? extends T> factory,
            PiRegistryPhase phase
    ) {
        this(registryKey, id, factory, phase, DEFAULT_GROUP);
    }

    public PiRegistryRequest {
        Objects.requireNonNull(registryKey, "registryKey");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(factory, "factory");
        Objects.requireNonNull(phase, "phase");
        Objects.requireNonNull(group, "group");
        if (group.isBlank()) {
            throw new IllegalArgumentException("group must not be blank");
        }
    }
}
