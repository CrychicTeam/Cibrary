package org.pickaid.pibrary.runtime.registrate.registry;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.pickaid.pibrary.api.registrate.registry.PiRegistryHandle;

public final class PiRegistryCatalog {
    private static final Map<ResourceKey<?>, Object> ENTRIES = new LinkedHashMap<>();

    private PiRegistryCatalog() {
    }

    public static <T> PiRegistryHandle<T> register(PiRegistryHandle<T> handle) {
        Object previous = ENTRIES.putIfAbsent(handle.registryKey(), handle);
        if (previous != null) {
            throw new IllegalStateException("Duplicate custom registry: " + handle.registryKey().location());
        }
        return handle;
    }

    @SuppressWarnings("unchecked")
    public static <T> PiRegistryHandle<T> require(ResourceKey<Registry<T>> key) {
        return (PiRegistryHandle<T>) ENTRIES.get(key);
    }

    public static void clearForTests() {
        ENTRIES.clear();
    }
}
