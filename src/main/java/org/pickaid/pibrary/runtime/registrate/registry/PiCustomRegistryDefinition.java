package org.pickaid.pibrary.runtime.registrate.registry;

import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.registrate.registry.PiRegistryHandle;

public record PiCustomRegistryDefinition<T>(
        ResourceKey<Registry<T>> registryKey,
        Codec<T> codec,
        ResourceKey<T> defaultKey
) implements PiRegistryHandle<T> {
    public PiCustomRegistryDefinition {
        Objects.requireNonNull(registryKey, "registryKey");
        Objects.requireNonNull(codec, "codec");
    }

    public static <T> PiCustomRegistryDefinition<T> of(ResourceLocation id, Codec<T> codec, String defaultPath) {
        ResourceKey<Registry<T>> registryKey = ResourceKey.createRegistryKey(id);
        ResourceKey<T> defaultKey = defaultPath == null
                ? null
                : ResourceKey.create(registryKey, ResourceLocation.fromNamespaceAndPath(id.getNamespace(), defaultPath));
        return new PiCustomRegistryDefinition<>(registryKey, codec, defaultKey);
    }
}
