package org.pickaid.pibrary.runtime.registrate.config;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.registrate.config.PiCreativeTabRegistration;

public final class PiCreativeTabRegistry {
    private static final Map<ResourceLocation, PiCreativeTabRegistration> ENTRIES = new LinkedHashMap<>();

    private PiCreativeTabRegistry() {
    }

    public static synchronized PiCreativeTabRegistration register(PiCreativeTabRegistration entry) {
        PiCreativeTabRegistration existing = ENTRIES.putIfAbsent(entry.id(), entry);
        if (existing != null) {
            throw new IllegalStateException("Duplicate creative tab: " + entry.id());
        }
        return entry;
    }

    public static synchronized PiCreativeTabRegistration require(ResourceLocation id) {
        PiCreativeTabRegistration entry = ENTRIES.get(id);
        if (entry == null) {
            throw new IllegalStateException("Missing creative tab: " + id);
        }
        return entry;
    }

    public static synchronized void clearForTests() {
        ENTRIES.clear();
    }
}
