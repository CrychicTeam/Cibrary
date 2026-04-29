package org.pickaid.pibrary.api.util;

import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Resource id helpers for code that builds many related paths.
 *
 * <p>The helpers normalize separators but do not silently lowercase input.
 * Uppercase paths should fail through {@link ResourceLocation} validation, so
 * bad ids are found close to the registration code.</p>
 */
public final class PiIds {
    private PiIds() {
    }

    public static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(requirePart(namespace, "namespace"), requirePath(path));
    }

    public static <T> ResourceKey<T> key(ResourceKey<? extends net.minecraft.core.Registry<T>> registry, String namespace, String path) {
        Objects.requireNonNull(registry, "registry");
        return ResourceKey.create(registry, id(namespace, path));
    }

    public static String path(String first, String... rest) {
        StringBuilder builder = new StringBuilder(cleanPart(first, "first"));
        Objects.requireNonNull(rest, "rest");
        for (int i = 0; i < rest.length; i++) {
            builder.append('/').append(cleanPart(rest[i], "rest[" + i + "]"));
        }
        return builder.toString();
    }

    public static String texturePath(String folder, String name) {
        String result = path("textures", folder, name);
        return result.endsWith(".png") ? result : result + ".png";
    }

    public static ResourceLocation blockTexture(String namespace, String name) {
        return id(namespace, texturePath("block", name));
    }

    public static ResourceLocation itemTexture(String namespace, String name) {
        return id(namespace, texturePath("item", name));
    }

    public static ResourceLocation guiTexture(String namespace, String name) {
        return id(namespace, texturePath("gui", name));
    }

    public static ResourceLocation prefix(ResourceLocation id, String prefix) {
        Objects.requireNonNull(id, "id");
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), requirePath(prefix + id.getPath()));
    }

    public static ResourceLocation suffix(ResourceLocation id, String suffix) {
        Objects.requireNonNull(id, "id");
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), requirePath(id.getPath() + requirePart(suffix, "suffix")));
    }

    public static ResourceLocation child(ResourceLocation parent, String child) {
        Objects.requireNonNull(parent, "parent");
        return ResourceLocation.fromNamespaceAndPath(parent.getNamespace(), path(parent.getPath(), child));
    }

    private static String requirePath(String path) {
        String result = Objects.requireNonNull(path, "path").trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        return result;
    }

    private static String requirePart(String part, String name) {
        String result = Objects.requireNonNull(part, name).trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return result;
    }

    private static String cleanPart(String part, String name) {
        String result = requirePart(part, name);
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException(name + " must contain a path segment");
        }
        return result;
    }
}
