package org.pickaid.pibrary.runtime.registrate.registry;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.pickaid.pibrary.api.registrate.registry.PiRegistryHandle;

public final class PiRegistryCatalog {
    private static final Map<ResourceKey<?>, Object> ENTRIES = new ConcurrentHashMap<>();

    private PiRegistryCatalog() {
    }

    public static synchronized <T> PiRegistryHandle<T> register(PiRegistryHandle<T> handle) {
        Object previous = ENTRIES.putIfAbsent(handle.registryKey(), handle);
        if (previous != null) {
            throw new IllegalStateException("Duplicate custom registry: " + handle.registryKey().location());
        }
        return handle;
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<PiRegistryHandle<T>> find(ResourceKey<Registry<T>> key) {
        return Optional.ofNullable((PiRegistryHandle<T>) ENTRIES.get(key));
    }

    public static <T> PiRegistryHandle<T> require(ResourceKey<Registry<T>> key) {
        return find(key).orElseThrow(() -> new IllegalStateException("Missing custom registry: " + key.location()));
    }

    static synchronized void clearForTests() {
        ENTRIES.clear();
    }
}
