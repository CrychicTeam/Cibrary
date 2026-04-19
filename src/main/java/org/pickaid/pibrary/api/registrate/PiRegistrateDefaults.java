package org.pickaid.pibrary.api.registrate;

import net.minecraft.resources.ResourceLocation;

public final class PiRegistrateDefaults {
    private PiRegistrateDefaults() {
    }

    public static String requirePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        return path;
    }

    public static ResourceLocation id(String modId, String path) {
        return ResourceLocation.fromNamespaceAndPath(modId, requirePath(path));
    }
}
