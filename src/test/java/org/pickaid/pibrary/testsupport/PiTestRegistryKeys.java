package org.pickaid.pibrary.testsupport;

import java.lang.reflect.Constructor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class PiTestRegistryKeys {
    private PiTestRegistryKeys() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> ResourceKey<Registry<T>> registry(String path) {
        try {
            Constructor<ResourceKey> constructor =
                    ResourceKey.class.getDeclaredConstructor(ResourceLocation.class, ResourceLocation.class);
            constructor.setAccessible(true);
            return (ResourceKey<Registry<T>>) constructor.newInstance(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "root"),
                    ResourceLocation.fromNamespaceAndPath("test", path));
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Failed to create lightweight registry key for unit test", exception);
        }
    }
}
