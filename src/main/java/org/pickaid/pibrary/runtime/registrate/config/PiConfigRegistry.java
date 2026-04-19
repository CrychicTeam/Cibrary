package org.pickaid.pibrary.runtime.registrate.config;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.config.PiConfigEntry;

public final class PiConfigRegistry {
    private static final Map<ResourceLocation, PiConfigEntry<?>> ENTRIES = new LinkedHashMap<>();

    private PiConfigRegistry() {
    }

    public static synchronized <T> PiConfigEntry<T> register(PiConfigEntry<T> entry) {
        PiConfigEntry<?> existing = ENTRIES.putIfAbsent(entry.id(), entry);
        if (existing != null) {
            throw new IllegalStateException("Duplicate config entry: " + entry.id());
        }
        return entry;
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T> PiConfigEntry<T> require(ResourceLocation id) {
        PiConfigEntry<?> entry = ENTRIES.get(id);
        if (entry == null) {
            throw new IllegalStateException("Missing config entry: " + id);
        }
        return (PiConfigEntry<T>) entry;
    }

    public static synchronized void clearForTests() {
        ENTRIES.clear();
    }
}
