package org.pickaid.pibrary.runtime.registrate.registry;

import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.registrate.registry.PiDatapackRegistryHandle;

public record PiDatapackRegistryDefinition<T>(
        ResourceKey<Registry<T>> registryKey,
        Codec<T> codec,
        Codec<T> networkCodec,
        ResourceKey<T> defaultKey,
        boolean syncToClient
) implements PiDatapackRegistryHandle<T> {
    public PiDatapackRegistryDefinition {
        Objects.requireNonNull(registryKey, "registryKey");
        Objects.requireNonNull(codec, "codec");
        Objects.requireNonNull(networkCodec, "networkCodec");
    }

    public static <T> PiDatapackRegistryDefinition<T> of(
            ResourceLocation id, Codec<T> codec, Codec<T> networkCodec, boolean syncToClient) {
        return new PiDatapackRegistryDefinition<>(ResourceKey.createRegistryKey(id), codec, networkCodec, null, syncToClient);
    }
}
