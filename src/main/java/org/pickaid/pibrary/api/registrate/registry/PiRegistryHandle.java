package org.pickaid.pibrary.api.registrate.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface PiRegistryHandle<T> {
    ResourceKey<Registry<T>> registryKey();

    Codec<T> codec();

    ResourceKey<T> defaultKey();

    default ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(registryKey().location().getNamespace(), path);
    }

    default ResourceKey<T> key(String path) {
        return ResourceKey.create(registryKey(), id(path));
    }
}
